/*
 * Copyright 2023 HM Revenue & Customs
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
import models.OtherEligibilityFailReason._
import models.PartnerClaimingChildBenefit.{GettingPayments, NotGettingPayments, WaitingToHear}
import models.ReasonNotToSubmit._
import models.RelationshipStatus._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages.applicant._
import pages.child._
import pages.partner._
import pages.payments._
import services.BrmsService
import uk.gov.hmrc.domain.Nino
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
  private val applicantName = AdultName(None, "first", None, "last")
  private val currentUkAddress = UkAddress("line 1", None, "town", None, "AA11 1AA")
  private val phoneNumber = "07777 777777"
  private val applicantNationality = Gen.oneOf(Nationality.allNationalities).sample.value

  private val childName = ChildName("first", None, "last")
  private val biologicalSex = ChildBiologicalSex.Female
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = BirthCertificateSystemNumber("000000000")

  private val journeyModelProvider = new JourneyModelProvider(mockBrmsService)

  ".allRequiredDocuments" - {

    "must be a list of all documents required for all the children in the claim" in {

      val answers = UserAnswers("id")
        .set(ApplicantNinoKnownPage, false).success.value
        .set(ApplicantNamePage, applicantName).success.value
        .set(ApplicantHasPreviousFamilyNamePage, false).success.value
        .set(ApplicantDateOfBirthPage, now).success.value
        .set(ApplicantPhoneNumberPage, phoneNumber).success.value
        .set(ApplicantNationalityPage(Index(0)), applicantNationality).success.value
        .set(ApplicantIsHmfOrCivilServantPage, false).success.value
        .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk).success.value
        .set(ApplicantCurrentUkAddressPage, currentUkAddress).success.value
        .set(ApplicantLivedAtCurrentAddressOneYearPage, true).success.value
        .set(ApplicantIsHmfOrCivilServantPage, false).success.value
        .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
        .set(RelationshipStatusPage, Single).success.value
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
        .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
        .set(WantToBePaidPage, false).success.value
        .set(IncludeAdditionalInformationPage, false).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty

      data.value.allRequiredDocuments must contain theSameElementsInOrderAs List(
        RequiredDocument(ChildName("child 2 first", None, "child 2 last"), DocumentType.BirthCertificate),
        RequiredDocument(ChildName("child 2 first", None, "child 2 last"), DocumentType.TravelDocument),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.BirthCertificate),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.TravelDocument),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.AdoptionCertificate),
      )

      data.value.userAuthenticated mustBe false
    }
  }

  ".reasonsNotToSubmit" - {

    val nino = arbitrary[Nino].sample.value
    val designatoryDetails =
      DesignatoryDetails(
        Some(applicantName),
        None,
        Some(NPSAddress("1", None, None, None, None, None, None)),
        None,
        LocalDate.now
      )

    val basicAnswers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))
      .set(ApplicantNinoKnownPage, false).success.value
      .set(ApplicantNamePage, applicantName).success.value
      .set(ApplicantHasPreviousFamilyNamePage, false).success.value
      .set(ApplicantDateOfBirthPage, now).success.value
      .set(ApplicantPhoneNumberPage, phoneNumber).success.value
      .set(ApplicantNationalityPage(Index(0)), applicantNationality).success.value
      .set(ApplicantIsHmfOrCivilServantPage, false).success.value
      .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk).success.value
      .set(ApplicantCurrentUkAddressPage, currentUkAddress).success.value
      .set(ApplicantLivedAtCurrentAddressOneYearPage, true).success.value
      .set(ApplicantIsHmfOrCivilServantPage, false).success.value
      .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
      .set(RelationshipStatusPage, Single).success.value
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
      .set(WantToBePaidPage, false).success.value
      .set(IncludeAdditionalInformationPage, false).success.value

    "must contain `User Unauthenticated` when the user is not authenticated" in {

      val answers = basicAnswers.copy(nino = None, designatoryDetails = None)

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only UserUnauthenticated
      data.value.userAuthenticated mustBe false
    }

    "must contain `Child Over Six Months` when a child is over 6 months old" in {

      val answers = basicAnswers.set(ChildDateOfBirthPage(Index(0)), LocalDate.now.minusMonths(6).minusDays(1)).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only ChildOverSixMonths
      data.value.userAuthenticated mustBe true
    }

    "must contain `Documents Required` when any documents are required for a child" in {

      val answers = basicAnswers.set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.Other).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only DocumentsRequired
      data.value.userAuthenticated mustBe true
    }

    "must contain `Designatory Details Changed` when the user has given a different name" in {

      val answers = basicAnswers.set(DesignatoryNamePage, AdultName(None, "new first", None, "new last")).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only DesignatoryDetailsChanged
      data.value.userAuthenticated mustBe true
    }

    "must contain `Designatory Details Changed` when the user has given a different residential address" in {

      val answers =
        basicAnswers
          .set(DesignatoryAddressInUkPage, true).success.value
          .set(DesignatoryUkAddressPage, UkAddress("new line 1", None, "new line 2", None, "new postcode")).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only DesignatoryDetailsChanged
      data.value.userAuthenticated mustBe true
    }

    "must contain `Designatory Details Changed` when the user has given a different correspondence address" in {

      val answers =
        basicAnswers
          .set(CorrespondenceAddressInUkPage, true).success.value
          .set(CorrespondenceUkAddressPage, UkAddress("new line 1", None, "new line 2", None, "new postcode")).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only DesignatoryDetailsChanged
      data.value.userAuthenticated mustBe true
    }

    "must contain `Partner Nino Missing' if the applicant has a partner who is claiming Child Benefit, but does not supply their NINO" in {

      val claiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value
      val answers =
        basicAnswers
          .set(RelationshipStatusPage, RelationshipStatus.Married).success.value
          .set(WantToBePaidPage, false).success.value
          .set(PartnerNamePage, AdultName(None, "partner first", None, "partner last")).success.value
          .set(PartnerNinoKnownPage, false).success.value
          .set(PartnerDateOfBirthPage, now).success.value
          .set(PartnerNationalityPage(Index(0)), Gen.oneOf(Nationality.allNationalities).sample.value).success.value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerWorkedAbroadPage, false).success.value
          .set(PartnerReceivedBenefitsAbroadPage, false).success.value
          .set(PartnerClaimingChildBenefitPage, claiming).success.value
          .set(PartnerEldestChildNamePage, ChildName("first", None, "last")).success.value
          .set(PartnerEldestChildDateOfBirthPage, LocalDate.now).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only PartnerNinoMissing
      data.value.userAuthenticated mustBe true
    }

    "must contain `Additional Information Present` when some additional information is supplied" in {

      val answers =
        basicAnswers
          .set(IncludeAdditionalInformationPage, true).success.value
          .set(AdditionalInformationPage, "foo").success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.reasonsNotToSubmit must contain only AdditionalInformationPresent
      data.value.userAuthenticated mustBe true
    }
  }

  ".otherEligibilityFailureReasons" - {

    val nino = arbitrary[Nino].sample.value
    val country = Gen.oneOf(Country.internationalCountries).sample.value
    val designatoryDetails =
      DesignatoryDetails(
        Some(applicantName),
        None,
        Some(NPSAddress("1", None, None, None, None, None, None)),
        None,
        LocalDate.now
      )

    val basicAnswers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))
      .set(ApplicantNinoKnownPage, false).success.value
      .set(ApplicantNamePage, applicantName).success.value
      .set(ApplicantHasPreviousFamilyNamePage, false).success.value
      .set(ApplicantDateOfBirthPage, now).success.value
      .set(ApplicantPhoneNumberPage, phoneNumber).success.value
      .set(ApplicantNationalityPage(Index(0)), applicantNationality).success.value
      .set(ApplicantIsHmfOrCivilServantPage, false).success.value
      .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk).success.value
      .set(ApplicantCurrentUkAddressPage, currentUkAddress).success.value
      .set(ApplicantLivedAtCurrentAddressOneYearPage, true).success.value
      .set(ApplicantIsHmfOrCivilServantPage, false).success.value
      .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
      .set(RelationshipStatusPage, Single).success.value
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
      .set(WantToBePaidPage, false).success.value
      .set(IncludeAdditionalInformationPage, false).success.value

    "must include Applicant Worked Abroad when the applicant has worked abroad" in {

      val answers =
        basicAnswers
          .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad).success.value
          .set(ApplicantUsuallyLivesInUkPage, true).success.value
          .set(ApplicantArrivedInUkPage, LocalDate.now.minusYears(1)).success.value
          .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(ApplicantWorkedAbroadPage, true).success.value
          .set(CountryApplicantWorkedPage(Index(0)), country).success.value
          .set(ApplicantReceivedBenefitsAbroadPage, false).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.otherEligibilityFailureReasons must contain only ApplicantWorkedAbroad
    }

    "must include Applicant Received Benefits Abroad when the applicant has received benefits abroad" in {

      val answers =
        basicAnswers
          .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad).success.value
          .set(ApplicantUsuallyLivesInUkPage, true).success.value
          .set(ApplicantArrivedInUkPage, LocalDate.now.minusYears(1)).success.value
          .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(ApplicantWorkedAbroadPage, false).success.value
          .set(ApplicantReceivedBenefitsAbroadPage, true).success.value
          .set(CountryApplicantReceivedBenefitsPage(Index(0)), country).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.otherEligibilityFailureReasons must contain only ApplicantReceivedBenefitsAbroad
    }

    "must include Partner Worked Abroad when the applicant's partner has worked abroad" in {

      val answers =
        basicAnswers
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerNamePage, AdultName(None, "first", None, "last")).success.value
          .set(PartnerNinoKnownPage, false).success.value
          .set(PartnerDateOfBirthPage, now).success.value
          .set(PartnerNationalityPage(Index(0)), Nationality.allNationalities.head).success.value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerWorkedAbroadPage, true).success.value
          .set(CountryPartnerWorkedPage(Index(0)), country).success.value
          .set(PartnerReceivedBenefitsAbroadPage, false).success.value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming).success.value
          .set(WantToBePaidPage, false).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.otherEligibilityFailureReasons must contain only PartnerWorkedAbroad
    }

    "must include Partner Received Benefits Abroad when the applicant's partner has received benefits abroad" in {

      val answers =
        basicAnswers
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerNamePage, AdultName(None, "first", None, "last")).success.value
          .set(PartnerNinoKnownPage, false).success.value
          .set(PartnerDateOfBirthPage, now).success.value
          .set(PartnerNationalityPage(Index(0)), Nationality.allNationalities.head).success.value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerWorkedAbroadPage, false).success.value
          .set(PartnerReceivedBenefitsAbroadPage, true).success.value
          .set(CountryPartnerReceivedBenefitsPage(Index(0)), country).success.value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming).success.value
          .set(WantToBePaidPage, false).success.value

      val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

      errors mustBe empty
      data.value.otherEligibilityFailureReasons must contain only PartnerReceivedBenefitsAbroad
    }

    "must include Child recently Lived Elsewhere when a child came to live with the applicant less than 3 months ago" - {

      "and the previous guardian lived abroad" in {

        val answers =
          basicAnswers
            .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
            .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
            .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
            .set(PreviousGuardianAddressKnownPage(Index(0)), true).success.value
            .set(PreviousGuardianAddressInUkPage(Index(0)), false).success.value
            .set(PreviousGuardianInternationalAddressPage(Index(0)), arbitrary[InternationalAddress].sample.value).success.value
            .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
            .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3).plusDays(1)).success.value

        val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

        errors mustBe empty
        data.value.otherEligibilityFailureReasons must contain only ChildRecentlyLivedElsewhere
      }

      "and the previous guardian's address is unknown" in {

        val answers =
          basicAnswers
            .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
            .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
            .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
            .set(PreviousGuardianAddressKnownPage(Index(0)), false).success.value
            .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
            .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3).plusDays(1)).success.value

        val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

        errors mustBe empty
        data.value.otherEligibilityFailureReasons must contain only ChildRecentlyLivedElsewhere
      }
    }

    "must not include Child Recently Lived Elsewhere" - {

      "when a child came to live with the applicant less than three months ago, and the previous guardian lived in the UK" in {

        val answers =
          basicAnswers
            .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
            .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
            .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
            .set(PreviousGuardianAddressKnownPage(Index(0)), true).success.value
            .set(PreviousGuardianAddressInUkPage(Index(0)), true).success.value
            .set(PreviousGuardianUkAddressPage(Index(0)), arbitrary[UkAddress].sample.value).success.value
            .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
            .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3).plusDays(1)).success.value

        val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

        errors mustBe empty
        data.value.otherEligibilityFailureReasons mustBe empty
      }

      "when a child came to live with the applicant 3 months ago or more" in {

        val answers =
          basicAnswers
            .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
            .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
            .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
            .set(PreviousGuardianAddressKnownPage(Index(0)), true).success.value
            .set(PreviousGuardianAddressInUkPage(Index(0)), false).success.value
            .set(PreviousGuardianInternationalAddressPage(Index(0)), arbitrary[InternationalAddress].sample.value).success.value
            .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
            .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3)).success.value

        val (errors, data) = journeyModelProvider.buildFromUserAnswers(answers).futureValue.pad

        errors mustBe empty
        data.value.otherEligibilityFailureReasons mustBe empty
      }
    }
  }
}
