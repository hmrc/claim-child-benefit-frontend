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

package services

import config.FeatureFlags
import generators.ModelGenerators
import models.OtherEligibilityFailReason._
import models.PartnerClaimingChildBenefit.{GettingPayments, NotGettingPayments, WaitingToHear}
import models.ReasonNotToSubmit._
import models.RelationshipStatus.{Married, Single}
import models._
import org.mockito.{Mockito, MockitoSugar}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{BeforeAndAfterEach, EitherValues, OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.applicant._
import pages.child._
import pages.partner._
import pages.payments._
import queries.BankAccountInsightsResultQuery
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class JourneyModelServiceSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with EitherValues
    with OptionValues
    with ModelGenerators
    with MockitoSugar
    with BeforeAndAfterEach {
  
  private val mockFeatureFlags = mock[FeatureFlags]

  override def beforeEach(): Unit = {
    Mockito.reset(mockFeatureFlags)
    super.beforeEach()
  }
  
  private val service = new JourneyModelService(mockFeatureFlags)

  private val now = LocalDate.now
  private val adultName = AdultName(None, "first", None, "last")
  private val ukAddress = UkAddress("line 1", None, "town", None, "AA11 1AA")
  private val phoneNumber = "07777 777777"
  private val nationality = Gen.oneOf(Nationality.allNationalities).sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value

  private val childName = ChildName("first", None, "last")
  private val biologicalSex = ChildBiologicalSex.Female
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = BirthCertificateSystemNumber("000000000")
  private val npsAddress = NPSAddress("line 1", None, None, None, None, None, None)
  private val designatoryDetails = DesignatoryDetails(Some(adultName), None, Some(npsAddress), None, LocalDate.now)

  private val nino = arbitrary[Nino].sample.value

  private val basicAnswers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails), relationshipDetails = Some(RelationshipDetails(false)))
    .set(ApplicantNinoKnownPage, false).success.value
    .set(ApplicantNamePage, adultName).success.value
    .set(ApplicantHasPreviousFamilyNamePage, false).success.value
    .set(ApplicantDateOfBirthPage, now).success.value
    .set(ApplicantPhoneNumberPage, phoneNumber).success.value
    .set(ApplicantNationalityPage(Index(0)), nationality).success.value
    .set(ApplicantIsHmfOrCivilServantPage, false).success.value
    .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk).success.value
    .set(ApplicantCurrentUkAddressPage, ukAddress).success.value
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

  ".build" - {

    ".reasonsNotToSubmit" - {

      "must contain `User Unauthenticated` when the user is not authenticated" in {

        val answers = basicAnswers.copy(nino = None, designatoryDetails = None)

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.reasonsNotToSubmit must contain only UserUnauthenticated
        data.value.userAuthenticated mustBe false
      }
      
      "when the `submit older children to CBS` flag is set" - {
        
        "must be empty even when some children are more than 6 months old" in {
          
          when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(true)

          val answers = basicAnswers.set(ChildDateOfBirthPage(Index(0)), LocalDate.now.minusYears(10)).success.value

          val (errors, data) = service.build(answers).pad

          errors mustBe empty
          data.value.reasonsNotToSubmit mustBe empty
          data.value.userAuthenticated mustBe true
        }
      }
      
      "when the `submit older children to CBS flag is not set" - {

        "must be empty when no children are older than 6 months old" in {

          when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(false)

          val answers = basicAnswers.set(ChildDateOfBirthPage(Index(0)), LocalDate.now.minusMonths(6)).success.value

          val (errors, data) = service.build(answers).pad

          errors mustBe empty
          data.value.reasonsNotToSubmit mustBe empty
        }

        "must contain `Child Over Six Months` when a child is over 6 months old" in {

          when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(false)

          val answers = basicAnswers.set(ChildDateOfBirthPage(Index(0)), LocalDate.now.minusMonths(6).minusDays(1)).success.value

          val (errors, data) = service.build(answers).pad

          errors mustBe empty
          data.value.reasonsNotToSubmit must contain only ChildOverSixMonths
          data.value.userAuthenticated mustBe true
        }
      }

      "must contain `Documents Required` when any documents are required for a child" in {

        when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(false)

        val answers = basicAnswers.set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.OtherCountry).success.value

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.reasonsNotToSubmit must contain only DocumentsRequired
        data.value.userAuthenticated mustBe true
      }

      "must contain `Designatory Details Changed` when the user has given a different name" in {

        when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(false)

        val answers = basicAnswers.set(DesignatoryNamePage, AdultName(None, "new first", None, "new last")).success.value

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.reasonsNotToSubmit must contain only DesignatoryDetailsChanged
        data.value.userAuthenticated mustBe true
      }

      "must contain `Designatory Details Changed` when the user has given a different residential address" in {

        when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(false)

        val answers =
          basicAnswers
            .set(DesignatoryAddressInUkPage, true).success.value
            .set(DesignatoryUkAddressPage, UkAddress("new line 1", None, "new line 2", None, "new postcode")).success.value

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.reasonsNotToSubmit must contain only DesignatoryDetailsChanged
        data.value.userAuthenticated mustBe true
      }

      "must contain `Designatory Details Changed` when the user has given a different correspondence address" in {

        when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(false)

        val answers =
          basicAnswers
            .set(CorrespondenceAddressInUkPage, true).success.value
            .set(CorrespondenceUkAddressPage, UkAddress("new line 1", None, "new line 2", None, "new postcode")).success.value

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.reasonsNotToSubmit must contain only DesignatoryDetailsChanged
        data.value.userAuthenticated mustBe true
      }

      "must contain `Partner Nino Missing' if the applicant has a partner who is claiming Child Benefit, but does not supply their NINO" in {

        when(mockFeatureFlags.submitOlderChildrenToCbs).thenReturn(false)

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

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.reasonsNotToSubmit must contain only PartnerNinoMissing
        data.value.userAuthenticated mustBe true
      }
    }
    
    ".otherEligibilityFailureReasons" - {
      
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

        val (errors, data) = service.build(answers).pad

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

        val (errors, data) = service.build(answers).pad

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

        val (errors, data) = service.build(answers).pad

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

        val (errors, data) = service.build(answers).pad

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

          val (errors, data) = service.build(answers).pad

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

          val (errors, data) = service.build(answers).pad

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

          val (errors, data) = service.build(answers).pad

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

          val (errors, data) = service.build(answers).pad

          errors mustBe empty
          data.value.otherEligibilityFailureReasons mustBe empty
        }
      }

      "must include Child possibly recently cared for by Local Authority when a child came to live with the applicant less than 3 months ago" - {

        "and the previous guardian's address is the UK and may have been a local authority" in {

          val laAddress = UkAddress("Some Borough Council", None, "town", None, "AA11AA")
          val answers =
            basicAnswers
              .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
              .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
              .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
              .set(PreviousGuardianAddressKnownPage(Index(0)), true).success.value
              .set(PreviousGuardianAddressInUkPage(Index(0)), true).success.value
              .set(PreviousGuardianUkAddressPage(Index(0)), laAddress).success.value
              .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
              .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3).plusDays(1)).success.value

          val (errors, data) = service.build(answers).pad

          errors mustBe empty
          data.value.otherEligibilityFailureReasons must contain only ChildPossiblyRecentlyUnderLocalAuthorityCare
        }
      }

      "must not include Child possibly recently cared for by Local Authority" - {

        "when a child came to live with the applicant less than 3 months ago" - {

          "and the previous guardian's address is in the UK but was not a local authority" in {

            val nonLaAddress = UkAddress("line 1", None, "town", None, "AA11AA")
            val answers =
              basicAnswers
                .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
                .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
                .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
                .set(PreviousGuardianAddressKnownPage(Index(0)), true).success.value
                .set(PreviousGuardianAddressInUkPage(Index(0)), true).success.value
                .set(PreviousGuardianUkAddressPage(Index(0)), nonLaAddress).success.value
                .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
                .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3).plusDays(1)).success.value

            val (errors, data) = service.build(answers).pad

            errors mustBe empty
            data.value.otherEligibilityFailureReasons mustBe empty
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

            val (errors, data) = service.build(answers).pad

            errors mustBe empty
            data.value.otherEligibilityFailureReasons must not contain ChildPossiblyRecentlyUnderLocalAuthorityCare
          }

          "and the previous guardian's address is international" in {

            val address = InternationalAddress("Some Borough Council", None, "town", None, None, Country("ES", "Spain"))
            val answers =
              basicAnswers
                .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
                .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
                .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
                .set(PreviousGuardianAddressKnownPage(Index(0)), true).success.value
                .set(PreviousGuardianAddressInUkPage(Index(0)), false).success.value
                .set(PreviousGuardianInternationalAddressPage(Index(0)), address).success.value
                .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
                .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3).plusDays(1)).success.value

            val (errors, data) = service.build(answers).pad

            errors mustBe empty
            data.value.otherEligibilityFailureReasons must not contain ChildPossiblyRecentlyUnderLocalAuthorityCare
          }
        }

        "when a child came to live with the applicant more than 3 months ago and the previous guardian's address is the UK and may have been a local authority" in {

          val laAddress = UkAddress("Some Borough Council", None, "town", None, "AA11AA")
          val answers =
            basicAnswers
              .set(ChildLivedWithAnyoneElsePage(Index(0)), true).success.value
              .set(PreviousGuardianNameKnownPage(Index(0)), true).success.value
              .set(PreviousGuardianNamePage(Index(0)), AdultName(None, "first", None, "last")).success.value
              .set(PreviousGuardianAddressKnownPage(Index(0)), true).success.value
              .set(PreviousGuardianAddressInUkPage(Index(0)), true).success.value
              .set(PreviousGuardianUkAddressPage(Index(0)), laAddress).success.value
              .set(PreviousGuardianPhoneNumberKnownPage(Index(0)), false).success.value
              .set(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now.minusMonths(3)).success.value

          val (errors, data) = service.build(answers).pad

          errors mustBe empty
          data.value.otherEligibilityFailureReasons mustBe empty
        }
      }

      "must include Bank Account Risk when there is a risk score of 100" in {

        val bankDetails = arbitrary[BankAccountDetails].sample.value
        val answers =
          basicAnswers
            .set(WantToBePaidPage, true).success.value
            .set(ApplicantBenefitsPage, Benefits.qualifyingBenefits).success.value
            .set(ApplicantHasSuitableAccountPage, true).success.value
            .set(AccountTypePage, AccountType.SortCodeAccountNumber).success.value
            .set(BankAccountHolderPage, BankAccountHolder.Applicant).success.value
            .set(BankAccountDetailsPage, bankDetails).success.value
            .set(BankAccountInsightsResultQuery, BankAccountInsightsResponseModel("a", 100, "b")).success.value

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.otherEligibilityFailureReasons must contain only BankAccountInsightsRisk
      }

      "must not include Bank Account Risk when there is a risk score of less than 100" in {

        val bankDetails = arbitrary[BankAccountDetails].sample.value
        val answers =
          basicAnswers
            .set(WantToBePaidPage, true).success.value
            .set(ApplicantBenefitsPage, Benefits.qualifyingBenefits).success.value
            .set(ApplicantHasSuitableAccountPage, true).success.value
            .set(AccountTypePage, AccountType.SortCodeAccountNumber).success.value
            .set(BankAccountHolderPage, BankAccountHolder.Applicant).success.value
            .set(BankAccountDetailsPage, bankDetails).success.value
            .set(BankAccountInsightsResultQuery, BankAccountInsightsResponseModel("a", 99, "b")).success.value

        val (errors, data) = service.build(answers).pad

        errors mustBe empty
        data.value.otherEligibilityFailureReasons mustBe empty
      }
    }
  }
}
