/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import generators.ModelGenerators
import models.AdditionalInformation.NoInformation
import models.RelationshipStatus._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages.applicant._
import pages.child._
import pages.income._
import pages.payments._
import services.BrmsService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class JourneyModelSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with EitherValues
    with OptionValues
    with ModelGenerators
    with ScalaFutures
    with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val mockBrmsService = mock[BrmsService]
  when(mockBrmsService.matchChild(any())(any(), any())) thenReturn Future.successful(BirthRegistrationMatchingResult.Matched)

  private val now = LocalDate.now
  private val applicantName = AdultName("first", None, "last")
  private val currentUkAddress = UkAddress("line 1", None, "town", None, "AA11 1AA")
  private val phoneNumber = "07777 777777"
  private val applicantBenefits = Set[Benefits](Benefits.NoneOfTheAbove)
  private val applicantNationality = Gen.oneOf(Nationality.allNationalities).sample.value

  private val childName = ChildName("first", None, "last")
  private val biologicalSex = ChildBiologicalSex.Female
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = BirthCertificateSystemNumber("000000000")

  private val journeyModelProvider = new JourneyModelProvider(mockBrmsService)

  ".allRequiredDocuments" - {

    "must be a list of all documents required for all the children in the claim" in {

      val answers = UserAnswers("id")
        .set(AlwaysLivedInUkPage, true).success.value
        .set(ApplicantNamePage, applicantName).success.value
        .set(ApplicantHasPreviousFamilyNamePage, false).success.value
        .set(ApplicantNinoKnownPage, false).success.value
        .set(ApplicantDateOfBirthPage, now).success.value
        .set(ApplicantCurrentUkAddressPage, currentUkAddress).success.value
        .set(ApplicantLivedAtCurrentAddressOneYearPage, true).success.value
        .set(ApplicantPhoneNumberPage, phoneNumber).success.value
        .set(ApplicantNationalityPage, applicantNationality).success.value
        .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
        .set(RelationshipStatusPage, Single).success.value
        .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
        .set(ApplicantBenefitsPage, applicantBenefits).success.value
        .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
        .set(WantToBePaidPage, false).success.value
        .set(AdditionalInformationPage, NoInformation).success.value
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
        .set(ChildNamePage(Index(1)), ChildName("child 2 first", None, "child 2 last")).success.value
        .set(ChildHasPreviousNamePage(Index(1)), false).success.value
        .set(ChildBiologicalSexPage(Index(1)), biologicalSex).success.value
        .set(ChildDateOfBirthPage(Index(1)), now).success.value
        .set(ChildBirthRegistrationCountryPage(Index(1)), ChildBirthRegistrationCountry.Other).success.value
        .set(ApplicantRelationshipToChildPage(Index(1)), relationshipToChild).success.value
        .set(AdoptingThroughLocalAuthorityPage(Index(1)), false).success.value
        .set(AnyoneClaimedForChildBeforePage(Index(1)), false).success.value
        .set(ChildLivesWithApplicantPage(Index(1)), true).success.value
        .set(ChildLivedWithAnyoneElsePage(Index(1)), false).success.value
        .set(ChildNamePage(Index(2)), ChildName("child 3 first", None, "child 3 last")).success.value
        .set(ChildHasPreviousNamePage(Index(2)), false).success.value
        .set(ChildBiologicalSexPage(Index(2)), biologicalSex).success.value
        .set(ChildDateOfBirthPage(Index(2)), now).success.value
        .set(ChildBirthRegistrationCountryPage(Index(2)), ChildBirthRegistrationCountry.Other).success.value
        .set(ApplicantRelationshipToChildPage(Index(2)), ApplicantRelationshipToChild.AdoptedChild).success.value
        .set(AdoptingThroughLocalAuthorityPage(Index(2)), false).success.value
        .set(AnyoneClaimedForChildBeforePage(Index(2)), false).success.value
        .set(ChildLivesWithApplicantPage(Index(2)), true).success.value
        .set(ChildLivedWithAnyoneElsePage(Index(2)), false).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty

      data.value.allRequiredDocuments must contain theSameElementsInOrderAs List(
        RequiredDocument(ChildName("child 2 first", None, "child 2 last"), DocumentType.BirthCertificate),
        RequiredDocument(ChildName("child 2 first", None, "child 2 last"), DocumentType.TravelDocument),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.BirthCertificate),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.TravelDocument),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.AdoptionCertificate),
      )
    }
  }
}
