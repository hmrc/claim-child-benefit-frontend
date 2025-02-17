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

package services

import cats.data.NonEmptyList
import connectors.ClaimChildBenefitConnector
import generators.ModelGenerators
import models.journey.JourneyModel
import models.{AdditionalArchiveDetails, AdultName, CurrentlyReceivingChildBenefit, Done, RelationshipStatus, SupplementaryMetadata, journey}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Lang, Messages, MessagesApi, MessagesImpl}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import views.xml.xml.archive.ArchiveTemplate

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import java.util.UUID
import scala.concurrent.Future

class SupplementaryDataServiceSpec
    extends AnyFreeSpec with Matchers with OptionValues with MockitoSugar with ModelGenerators with ScalaFutures
    with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset[Any](
      mockClaimChildBenefitConnector,
      mockFop
    )
  }

  private val mockClaimChildBenefitConnector = mock[ClaimChildBenefitConnector]
  private val mockFop = mock[FopService]
  private val clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)

  private val request: RequestHeader = FakeRequest()

  private val nino = arbitrary[Nino].sample.value.value

  private val model = JourneyModel(
    applicant = journey.Applicant(
      name = arbitrary[AdultName].sample.value,
      previousFamilyNames = Nil,
      dateOfBirth = LocalDate.now,
      nationalInsuranceNumber = Some(nino),
      currentAddress = arbitrary[models.UkAddress].sample.value,
      previousAddress = None,
      telephoneNumber = "0777777777",
      nationalities =
        NonEmptyList(genUkCtaNationality.sample.value, Gen.listOf(arbitrary[models.Nationality]).sample.value),
      residency = journey.Residency.AlwaysLivedInUk,
      memberOfHMForcesOrCivilServantAbroad = false,
      currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
      changedDesignatoryDetails = Some(false),
      correspondenceAddress = None
    ),
    relationship = journey.Relationship(RelationshipStatus.Single, None, None),
    children = NonEmptyList(
      journey.Child(
        name = arbitrary[models.ChildName].sample.value,
        nameChangedByDeedPoll = None,
        previousNames = Nil,
        biologicalSex = Gen.oneOf(models.ChildBiologicalSex.values).sample.value,
        dateOfBirth = LocalDate.now,
        countryOfRegistration = Gen.oneOf(models.ChildBirthRegistrationCountry.values).sample.value,
        birthCertificateNumber = None,
        birthCertificateDetailsMatched = models.BirthRegistrationMatchingResult.NotAttempted,
        relationshipToApplicant = arbitrary[models.ApplicantRelationshipToChild].sample.value,
        adoptingThroughLocalAuthority = false,
        previousClaimant = None,
        guardian = None,
        previousGuardian = None,
        dateChildStartedLivingWithApplicant = None
      ),
      Nil
    ),
    benefits = None,
    paymentPreference = journey.PaymentPreference.DoNotPay(None),
    userAuthenticated = true,
    reasonsNotToSubmit = Nil,
    otherEligibilityFailureReasons = Nil
  )

  "submit" - {

    "when `features.dmsa-submission` is true" - {

      val app = GuiceApplicationBuilder()
        .overrides(
          bind[ClaimChildBenefitConnector].toInstance(mockClaimChildBenefitConnector),
          bind[FopService].toInstance(mockFop),
          bind[Clock].toInstance(clock)
        )
        .configure(
          "features.dmsa-submission" -> true
        )
        .build()

      val messagesApi = app.injector.instanceOf[MessagesApi]
      val service = app.injector.instanceOf[SupplementaryDataService]
      val uuid = UUID.randomUUID()

      val englishMessages: Messages = MessagesImpl(Lang("en"), messagesApi)

      "must create a PDF and submit as supplementary data" in {

        val expectedMetadata = SupplementaryMetadata(
          nino = nino,
          submissionDate = clock.instant(),
          correlationId = uuid.toString
        )

        val additionalDetails = AdditionalArchiveDetails(None)
        val template = app.injector.instanceOf[ArchiveTemplate]
        val expectedView = template(model, additionalDetails)(englishMessages)
        val expectedPdf = "hello".getBytes

        when(mockFop.render(any(), any())) `thenReturn` Future.successful(expectedPdf)
        when(mockClaimChildBenefitConnector.submitSupplementaryData(any(), any())(any()))
          .thenReturn(Future.successful(Done))

        service.submit(nino, model, uuid, additionalDetails)(request).futureValue

        verify(mockFop, times(1)).render(eqTo(expectedView.body), any())
        verify(mockClaimChildBenefitConnector, times(1))
          .submitSupplementaryData(eqTo(expectedPdf), eqTo(expectedMetadata))(any())
      }

      "must fail when the supplementary data call fails" in {

        val additionalDetails = AdditionalArchiveDetails(None)
        when(mockFop.render(any(), any())) `thenReturn` Future.successful("hello".getBytes)
        when(mockClaimChildBenefitConnector.submitSupplementaryData(any(), any())(any()))
          .thenReturn(Future.failed(new RuntimeException()))
        service.submit(nino, model, uuid, additionalDetails)(request).failed.futureValue
      }
    }

    "when `features.dmsa-submission` is false" - {

      "must not submit anything" in {

        val app = GuiceApplicationBuilder()
          .overrides(
            bind[ClaimChildBenefitConnector].toInstance(mockClaimChildBenefitConnector)
          )
          .configure(
            "features.dmsa-submission" -> false
          )
          .build()

        val service = app.injector.instanceOf[SupplementaryDataService]
        val uuid = UUID.randomUUID()
        val additionalDetails = AdditionalArchiveDetails(None)

        service.submit(nino, model, uuid, additionalDetails)(request).futureValue

        verify(mockClaimChildBenefitConnector, times(0)).submitSupplementaryData(any(), any())(any())
      }
    }
  }
}
