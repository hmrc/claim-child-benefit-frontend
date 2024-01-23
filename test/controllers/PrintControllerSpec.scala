/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import audit.AuditService
import base.SpecBase
import generators.ModelGenerators
import models._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.applicant._
import pages.child._
import pages.partner.RelationshipStatusPage
import pages.payments._
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{BrmsService, FopService, JourneyModelService}
import views.html.{PrintDocumentsRequiredView, PrintNoDocumentsRequiredView}
import views.xml.xml.download.PrintTemplate

import java.nio.charset.Charset
import java.time.LocalDate
import scala.concurrent.Future

class PrintControllerSpec extends SpecBase with ModelGenerators with MockitoSugar {

  private val now = LocalDate.now
  private val applicantName = AdultName(None, "first", None, "last")
  private val currentAddress = UkAddress("line 1", None, "town", None, "AA111AA")
  private val phoneNumber = "07777 777777"
  private val applicantBenefits = Set[Benefits](Benefits.NoneOfTheAbove)

  private val childName = ChildName("first", None, "last")
  private val biologicalSex = ChildBiologicalSex.Female
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = BirthCertificateSystemNumber("000000000")

  private val completeAnswers =
    UserAnswers("id")
      .set(ApplicantNamePage, applicantName).success.value
      .set(ApplicantHasPreviousFamilyNamePage, false).success.value
      .set(ApplicantNinoKnownPage, false).success.value
      .set(ApplicantDateOfBirthPage, now).success.value
      .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk).success.value
      .set(ApplicantCurrentUkAddressPage, currentAddress).success.value
      .set(ApplicantLivedAtCurrentAddressOneYearPage, true).success.value
      .set(ApplicantPhoneNumberPage, phoneNumber).success.value
      .set(ApplicantNationalityPage(Index(0)), Nationality.allNationalities.head).success.value
      .set(ApplicantIsHmfOrCivilServantPage, false).success.value
      .set(ChildNamePage(Index(0)), childName).success.value
      .set(ChildHasPreviousNamePage(Index(0)), false).success.value
      .set(ChildBiologicalSexPage(Index(0)), biologicalSex).success.value
      .set(ChildDateOfBirthPage(Index(0)), now).success.value
      .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.England).success.value
      .set(BirthCertificateHasSystemNumberPage(Index(0)), true).success.value
      .set(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber).success.value
      .set(ApplicantRelationshipToChildPage(Index(0)), relationshipToChild).success.value
      .set(AdoptingThroughLocalAuthorityPage(Index(0)), false).success.value
      .set(AnyoneClaimedForChildBeforePage(Index(0)), false).success.value
      .set(ChildLivesWithApplicantPage(Index(0)), true).success.value
      .set(ChildLivedWithAnyoneElsePage(Index(0)), false).success.value
      .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
      .set(ApplicantBenefitsPage, applicantBenefits).success.value
      .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
      .set(WantToBePaidPage, false).success.value
      .set(RelationshipStatusPage, RelationshipStatus.Single).success.value
      .set(AlwaysLivedInUkPage, true).success.value

  "Print Controller" - {

    "must return OK and the correct view for onPageLoad when user answers are complete and no documents are required" in {

      val mockBrmsService = mock[BrmsService]
      when(mockBrmsService.matchChild(any())(any(), any())) thenReturn Future.successful(BirthRegistrationMatchingResult.Matched)

      val application =
        applicationBuilder(userAnswers = Some(completeAnswers))
          .overrides(bind[BrmsService].toInstance(mockBrmsService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.PrintController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PrintNoDocumentsRequiredView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(false)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for onPageLoad when user answers are complete and some documents are required" in {

      val mockBrmsService = mock[BrmsService]
      when(mockBrmsService.matchChild(any())(any(), any())) thenReturn Future.successful(BirthRegistrationMatchingResult.Matched)

      val answers = completeAnswers.set(ApplicantRelationshipToChildPage(Index(0)), ApplicantRelationshipToChild.AdoptedChild).success.value

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[BrmsService].toInstance(mockBrmsService))
          .build()

      running(application) {
        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, routes.PrintController.onPageLoad.url)

        val result = route(application, request).value

        val view                = application.injector.instanceOf[PrintDocumentsRequiredView]
        val journeyModelService = application.injector.instanceOf[JourneyModelService]
        val journeyModel        = journeyModelService.build(answers).right.value

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(journeyModel)(request, messages(application)).toString
      }
    }

    "must redirect to journey recovery for onPageLoad when user answers are not complete" in {

      val mockBrmsService = mock[BrmsService]
      when(mockBrmsService.matchChild(any())(any(), any())) thenReturn Future.successful(BirthRegistrationMatchingResult.Matched)

      val incompleteAnswers = completeAnswers.remove(ApplicantNamePage).success.value

      val application =
        applicationBuilder(userAnswers = Some(incompleteAnswers))
          .overrides(bind[BrmsService].toInstance(mockBrmsService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.PrintController.onPageLoad.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return OK and an English-language PDF, and audit the download for onDownload when user answers are complete" in {

      val mockAuditService = mock[AuditService]
      val mockFop = mock[FopService]
      val mockBrmsService = mock[BrmsService]
      when(mockFop.render(any(), any())) thenReturn Future.successful("hello".getBytes)
      when(mockBrmsService.matchChild(any())(any(), any())) thenReturn Future.successful(BirthRegistrationMatchingResult.Matched)

      val application =
        applicationBuilder(userAnswers = Some(completeAnswers))
          .overrides(
            bind[FopService].toInstance(mockFop),
            bind[AuditService].toInstance(mockAuditService),
            bind[BrmsService].toInstance(mockBrmsService)
          )
          .build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val request = FakeRequest(GET, routes.PrintController.onDownload.url)

        val result = route(application, request).value

        val template = application.injector.instanceOf[PrintTemplate]
        val journeyModelService = application.injector.instanceOf[JourneyModelService]
        val messagesApi = application.injector.instanceOf[MessagesApi]

        val journeyModel = journeyModelService.build(completeAnswers)
        val englishMessages: Messages = MessagesImpl(Lang("en"), messagesApi)
        val expectedRenderedTemplate = template.render(journeyModel.right.value, englishMessages)

        status(result) mustEqual OK
        contentAsBytes(result).decodeString(Charset.defaultCharset()) mustEqual "hello"

        verify(mockAuditService, times(1)).auditDownload(any())(any())
        verify(mockFop, times(1)).render(eqTo(expectedRenderedTemplate.body), any())
      }
    }

    "must redirect to journey recovery and not audit the download for onDownload when user answers are not complete" in {

      val incompleteAnswers = completeAnswers.remove(ApplicantNamePage).success.value

      val mockAuditService = mock[AuditService]
      val mockFop = mock[FopService]
      val mockBrmsService = mock[BrmsService]
      when(mockBrmsService.matchChild(any())(any(), any())) thenReturn Future.successful(BirthRegistrationMatchingResult.Matched)
      when(mockFop.render(any(), any())) thenReturn Future.successful("hello".getBytes)

      val application =
        applicationBuilder(userAnswers = Some(incompleteAnswers))
          .overrides(
            bind[FopService].toInstance(mockFop),
            bind[AuditService].toInstance(mockAuditService),
            bind[BrmsService].toInstance(mockBrmsService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.PrintController.onDownload.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url

        verify(mockAuditService, never()).auditDownload(any())(any())
      }
    }
  }
}
