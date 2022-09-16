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

import cats.data.NonEmptyList
import generators.ModelGenerators
import models.RelationshipStatus._
import models.{ChildBirthRegistrationCountry => BirthCountry}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import pages._
import pages.applicant._
import pages.child._
import pages.income._
import pages.partner._
import pages.payments._
import queries.{AllChildPreviousNames, AllChildSummaries, AllPreviousFamilyNames}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class JourneyModelSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with EitherValues
    with OptionValues
    with ModelGenerators {

  private val now = LocalDate.now
  private val applicantName = AdultName("first", None, "last")
  private val applicantNino = arbitrary[Nino].sample.value
  private val currentAddress = UkAddress("line 1", None, "town", None, "AA11 1AA")
  private val previousAddress = UkAddress("line 1", None, "town", None, "BB22 2BB")
  private val phoneNumber = "07777 777777"
  private val bestTimes = Set[BestTimeToContact](BestTimeToContact.Morning)
  private val applicantBenefits = Set[Benefits](Benefits.NoneOfTheAbove)
  private val applicantNationality = "applicant nationality"
  private val applicantEmployment = Set[EmploymentStatus](EmploymentStatus.Employed)
  private val previousName1 = "previous name 1"
  private val previousName2 = "previous name 2"

  private val bankAccountDetails = BankAccountDetails("name on account", "bank name", "00000000", "000000", None)
  private val bankAccountHolder = BankAccountHolder.Applicant
  private val eldestChildName = ChildName("first", None, "last")

  private val partnerName = AdultName("partner first", None, "partner last")
  private val partnerNationality = "partner nationality"
  private val partnerEmployment = Set[EmploymentStatus](EmploymentStatus.Employed)
  private val partnerNino = arbitrary[Nino].sample.value
  private val partnerEldestChildName = ChildName("partner child first", None, "partner child last")

  private val childName = ChildName("first", None, "last")
  private val biologicalSex = ChildBiologicalSex.Female
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = "000000000"
  private val scottishBirthCertificateDetails = "0000000000"
  private val childPreviousName1 = ChildName("first 1", None, "last 1")
  private val childPreviousName2 = ChildName("first 2", None, "last 2")

  ".from" - {

    "must create a journey model" - {

      "from minimal answers when the applicant does not have a partner" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
          .set(ApplicantBenefitsPage, applicantBenefits).success.value
          .set(CurrentlyReceivingChildBenefitPage, false).success.value
          .set(WantToBePaidPage, false).success.value

        val expectedModel = JourneyModel(
          applicant = JourneyModel.Applicant(
            name = applicantName,
            previousFamilyNames = Nil,
            dateOfBirth = now,
            nationalInsuranceNumber = None,
            currentAddress = currentAddress,
            previousAddress = None,
            telephoneNumber = phoneNumber,
            bestTimeToContact = bestTimes,
            nationality = applicantNationality,
            employmentStatus = applicantEmployment,
            memberOfHMForcesOrCivilServantAbroad = false
          ),
          relationship = JourneyModel.Relationship(Single, None, None),
          children = NonEmptyList(
            JourneyModel.Child(
              name = childName,
              nameChangedByDeedPoll = None,
              previousNames = Nil,
              biologicalSex = biologicalSex,
              dateOfBirth = now,
              countryOfRegistration = ChildBirthRegistrationCountry.England,
              birthCertificateNumber = Some(systemNumber),
              relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
              adoptingThroughLocalAuthority = false,
              previousClaimant = None
            ), Nil
          ),
          benefits = applicantBenefits,
          paymentPreference = JourneyModel.PaymentPreference.DoNotPay
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value mustEqual expectedModel
      }

      "from minimal answers when the applicant has a partner" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .set(RelationshipStatusPage, Married).success.value
          .set(ApplicantOrPartnerIncomePage, Income.BelowLowerThreshold).success.value
          .set(ApplicantOrPartnerBenefitsPage, applicantBenefits).success.value
          .set(CurrentlyReceivingChildBenefitPage, false).success.value
          .set(WantToBePaidPage, false).success.value
          .set(PartnerNamePage, partnerName).success.value
          .set(PartnerNinoKnownPage, false).success.value
          .set(PartnerDateOfBirthPage, now).success.value
          .set(PartnerNationalityPage, partnerNationality).success.value
          .set(PartnerEmploymentStatusPage, partnerEmployment).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerClaimingChildBenefitPage, false).success.value
          .set(PartnerWaitingForEntitlementDecisionPage, false).success.value

        val expectedModel = JourneyModel(
          applicant = JourneyModel.Applicant(
            name = applicantName,
            previousFamilyNames = Nil,
            dateOfBirth = now,
            nationalInsuranceNumber = None,
            currentAddress = currentAddress,
            previousAddress = None,
            telephoneNumber = phoneNumber,
            bestTimeToContact = bestTimes,
            nationality = applicantNationality,
            employmentStatus = applicantEmployment,
            memberOfHMForcesOrCivilServantAbroad = false
          ),
          relationship = JourneyModel.Relationship(
            status = Married,
            since = None,
            partner = Some(JourneyModel.Partner(
              name = partnerName,
              dateOfBirth = now,
              nationality = partnerNationality,
              nationalInsuranceNumber = None,
              employmentStatus = partnerEmployment,
              memberOfHMForcesOrCivilServantAbroad = false,
              currentlyClaimingChildBenefit = false,
              waitingToHearAboutEntitlement = Some(false),
              eldestChild = None
            ))
          ),
          children = NonEmptyList(
            JourneyModel.Child(
              name = childName,
              nameChangedByDeedPoll = None,
              previousNames = Nil,
              biologicalSex = biologicalSex,
              dateOfBirth = now,
              countryOfRegistration = ChildBirthRegistrationCountry.England,
              birthCertificateNumber = Some(systemNumber),
              relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
              adoptingThroughLocalAuthority = false,
              previousClaimant = None
            ), Nil
          ),
          benefits = applicantBenefits,
          paymentPreference = JourneyModel.PaymentPreference.DoNotPay
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value mustEqual expectedModel
      }

      "when the applicant has multiple children" in {

        val childName2 = ChildName("first 2", None, "last 2")

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPartnerDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(ChildNamePage(Index(1)), childName2).success.value
          .set(ChildHasPreviousNamePage(Index(1)), false).success.value
          .set(ChildBiologicalSexPage(Index(1)), biologicalSex).success.value
          .set(ChildDateOfBirthPage(Index(1)), now).success.value
          .set(ChildBirthRegistrationCountryPage(Index(1)), ChildBirthRegistrationCountry.Scotland).success.value
          .set(ScottishBirthCertificateHasNumbersPage(Index(1)), true).success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value
          .set(ApplicantRelationshipToChildPage(Index(1)), relationshipToChild).success.value
          .set(AdoptingThroughLocalAuthorityPage(Index(1)), false).success.value
          .set(AnyoneClaimedForChildBeforePage(Index(1)), false).success.value

        val expectedModel = JourneyModel(
          applicant = JourneyModel.Applicant(
            name = applicantName,
            previousFamilyNames = Nil,
            dateOfBirth = now,
            nationalInsuranceNumber = None,
            currentAddress = currentAddress,
            previousAddress = None,
            telephoneNumber = phoneNumber,
            bestTimeToContact = bestTimes,
            nationality = applicantNationality,
            employmentStatus = applicantEmployment,
            memberOfHMForcesOrCivilServantAbroad = false
          ),
          relationship = JourneyModel.Relationship(
            status = Married,
            since = None,
            partner = Some(JourneyModel.Partner(
              name = partnerName,
              dateOfBirth = now,
              nationality = partnerNationality,
              nationalInsuranceNumber = None,
              employmentStatus = partnerEmployment,
              memberOfHMForcesOrCivilServantAbroad = false,
              currentlyClaimingChildBenefit = false,
              waitingToHearAboutEntitlement = Some(false),
              eldestChild = None
            ))
          ),
          children = NonEmptyList(
            JourneyModel.Child(
              name = childName,
              nameChangedByDeedPoll = None,
              previousNames = Nil,
              biologicalSex = biologicalSex,
              dateOfBirth = now,
              countryOfRegistration = ChildBirthRegistrationCountry.England,
              birthCertificateNumber = Some(systemNumber),
              relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
              adoptingThroughLocalAuthority = false,
              previousClaimant = None
            ), List(
              JourneyModel.Child(
                name = childName2,
                nameChangedByDeedPoll = None,
                previousNames = Nil,
                biologicalSex = biologicalSex,
                dateOfBirth = now,
                countryOfRegistration = ChildBirthRegistrationCountry.Scotland,
                birthCertificateNumber = Some(scottishBirthCertificateDetails),
                relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
                adoptingThroughLocalAuthority = false,
                previousClaimant = None
              )
            )
          ),
          benefits = applicantBenefits,
          paymentPreference = JourneyModel.PaymentPreference.DoNotPay
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value mustEqual expectedModel
      }

      "when the applicant is already receiving child benefit" - {

        "and wants to be paid to their existing account" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .set(RelationshipStatusPage, Single).success.value
            .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
            .set(ApplicantBenefitsPage, applicantBenefits).success.value
            .set(CurrentlyReceivingChildBenefitPage, true).success.value
            .set(EldestChildNamePage, eldestChildName).success.value
            .set(EldestChildDateOfBirthPage, now).success.value
            .set(WantToBePaidToExistingAccountPage, true).success.value

          val expectedPaymentPreference = JourneyModel.PaymentPreference.ExistingAccount(
            JourneyModel.EldestChild(eldestChildName, now)
          )
          val (errors, data) = JourneyModel.from(answers).pad

          errors mustBe empty
          data.value.paymentPreference mustEqual expectedPaymentPreference
        }

        "and does not want to be paid into their existing account" - {

          "and has a suitable account" in {

            val answers = UserAnswers("id")
              .withMinimalApplicantDetails
              .withOneChild
              .set(RelationshipStatusPage, Single).success.value
              .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
              .set(ApplicantBenefitsPage, applicantBenefits).success.value
              .set(CurrentlyReceivingChildBenefitPage, true).success.value
              .set(EldestChildNamePage, eldestChildName).success.value
              .set(EldestChildDateOfBirthPage, now).success.value
              .set(WantToBePaidToExistingAccountPage, false).success.value
              .set(ApplicantHasSuitableAccountPage, true).success.value
              .set(BankAccountHolderPage, bankAccountHolder).success.value
              .set(BankAccountDetailsPage, bankAccountDetails).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.ExistingFrequency(
              bankAccount = Some(JourneyModel.BankAccount(bankAccountHolder, bankAccountDetails)),
              eldestChild = JourneyModel.EldestChild(eldestChildName, now)
            )

            val (errors, data) = JourneyModel.from(answers).pad

            errors mustBe empty
            data.value.paymentPreference mustEqual expectedPaymentPreference
          }

          "and does not have a suitable account" in {

            val answers = UserAnswers("id")
              .withMinimalApplicantDetails
              .withOneChild
              .set(RelationshipStatusPage, Single).success.value
              .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
              .set(ApplicantBenefitsPage, applicantBenefits).success.value
              .set(CurrentlyReceivingChildBenefitPage, true).success.value
              .set(EldestChildNamePage, eldestChildName).success.value
              .set(EldestChildDateOfBirthPage, now).success.value
              .set(WantToBePaidToExistingAccountPage, false).success.value
              .set(ApplicantHasSuitableAccountPage, false).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.ExistingFrequency(
              bankAccount = None,
              eldestChild = JourneyModel.EldestChild(eldestChildName, now)
            )

            val (errors, data) = JourneyModel.from(answers).pad

            errors mustBe empty
            data.value.paymentPreference mustEqual expectedPaymentPreference
          }
        }
      }

      "when the applicant is not already receiving child benefit" - {

        val baseAnswers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
          .set(ApplicantBenefitsPage, applicantBenefits).success.value
          .set(CurrentlyReceivingChildBenefitPage, false).success.value

        "and wants to be paid every 4 weeks" - {

          "and has a suitable account" in {

            val answers = baseAnswers
              .set(WantToBePaidPage, true).success.value
              .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
              .set(ApplicantHasSuitableAccountPage, true).success.value
              .set(BankAccountHolderPage, bankAccountHolder).success.value
              .set(BankAccountDetailsPage, bankAccountDetails).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
              bankAccount = Some(JourneyModel.BankAccount(bankAccountHolder, bankAccountDetails))
            )

            val (errors, data) = JourneyModel.from(answers).pad

            errors mustBe empty
            data.value.paymentPreference mustEqual expectedPaymentPreference
          }

          "and does not have a suitable account" in {

            val answers = baseAnswers
              .set(WantToBePaidPage, true).success.value
              .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
              .set(BankAccountHolderPage, bankAccountHolder).success.value
              .set(ApplicantHasSuitableAccountPage, false).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
              bankAccount = None
            )

            val (errors, data) = JourneyModel.from(answers).pad

            errors mustBe empty
            data.value.paymentPreference mustEqual expectedPaymentPreference
          }
        }

        "and wants to be paid weekly" - {

          "and has a suitable account" in {

            val answers = baseAnswers
              .set(WantToBePaidPage, true).success.value
              .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value
              .set(ApplicantHasSuitableAccountPage, true).success.value
              .set(BankAccountHolderPage, bankAccountHolder).success.value
              .set(BankAccountDetailsPage, bankAccountDetails).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.Weekly(
              bankAccount = Some(JourneyModel.BankAccount(bankAccountHolder, bankAccountDetails))
            )

            val (errors, data) = JourneyModel.from(answers).pad

            errors mustBe empty
            data.value.paymentPreference mustEqual expectedPaymentPreference
          }

          "and does not have a suitable account" in {

            val answers = baseAnswers
              .set(WantToBePaidPage, true).success.value
              .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value
              .set(ApplicantHasSuitableAccountPage, false).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.Weekly(
              bankAccount = None
            )

            val (errors, data) = JourneyModel.from(answers).pad

            errors mustBe empty
            data.value.paymentPreference mustEqual expectedPaymentPreference
          }
        }

        "and does not want to be paid" in {

          val answers = baseAnswers
            .set(WantToBePaidPage, false).success.value

          val expectedPaymentPreference = JourneyModel.PaymentPreference.DoNotPay

          val (errors, data) = JourneyModel.from(answers).pad

          errors mustBe empty
          data.value.paymentPreference mustEqual expectedPaymentPreference
        }
      }

      "when the applicant knows their NINO" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantNinoKnownPage, true).success.value
          .set(ApplicantNinoPage, applicantNino).success.value

        val expectedApplicant = JourneyModel.Applicant(
          name = applicantName,
          previousFamilyNames = Nil,
          dateOfBirth = now,
          nationalInsuranceNumber = Some(applicantNino.value),
          currentAddress = currentAddress,
          previousAddress = None,
          telephoneNumber = phoneNumber,
          bestTimeToContact = bestTimes,
          nationality = applicantNationality,
          employmentStatus = applicantEmployment,
          memberOfHMForcesOrCivilServantAbroad = false
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.applicant mustEqual expectedApplicant
      }

      "when the applicant has some previous family names" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantHasPreviousFamilyNamePage, true).success.value
          .set(ApplicantPreviousFamilyNamePage(Index(0)), previousName1).success.value
          .set(ApplicantPreviousFamilyNamePage(Index(1)), previousName2).success.value

        val expectedApplicant = JourneyModel.Applicant(
          name = applicantName,
          previousFamilyNames = List(previousName1, previousName2),
          dateOfBirth = now,
          nationalInsuranceNumber = None,
          currentAddress = currentAddress,
          previousAddress = None,
          telephoneNumber = phoneNumber,
          bestTimeToContact = bestTimes,
          nationality = applicantNationality,
          employmentStatus = applicantEmployment,
          memberOfHMForcesOrCivilServantAbroad = false
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.applicant mustEqual expectedApplicant
      }

      "when the applicant has not lived at their current address for a year" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantLivedAtCurrentAddressOneYearPage, false).success.value
          .set(ApplicantPreviousUkAddressPage, previousAddress).success.value

        val expectedApplicant = JourneyModel.Applicant(
          name = applicantName,
          previousFamilyNames = Nil,
          dateOfBirth = now,
          nationalInsuranceNumber = None,
          currentAddress = currentAddress,
          previousAddress = Some(previousAddress),
          telephoneNumber = phoneNumber,
          bestTimeToContact = bestTimes,
          nationality = applicantNationality,
          employmentStatus = applicantEmployment,
          memberOfHMForcesOrCivilServantAbroad = false
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.applicant mustEqual expectedApplicant
      }

      "when the applicant knows their partner's NINO" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerNinoKnownPage, true).success.value
          .set(PartnerNinoPage, partnerNino).success.value

        val expectedPartner = JourneyModel.Partner(
          name = partnerName,
          dateOfBirth = now,
          nationality = partnerNationality,
          nationalInsuranceNumber = Some(partnerNino.value),
          employmentStatus = partnerEmployment,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyClaimingChildBenefit = false,
          waitingToHearAboutEntitlement = Some(false),
          eldestChild = None
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.relationship.partner.value mustEqual expectedPartner
      }

      "when the applicant's partner is entitled to Child Benefit" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerClaimingChildBenefitPage, true).success.value
          .set(PartnerEldestChildNamePage, partnerEldestChildName).success.value
          .set(PartnerEldestChildDateOfBirthPage, now).success.value

        val expectedPartner = JourneyModel.Partner(
          name = partnerName,
          dateOfBirth = now,
          nationality = partnerNationality,
          nationalInsuranceNumber = None,
          employmentStatus = partnerEmployment,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyClaimingChildBenefit = true,
          waitingToHearAboutEntitlement = None,
          eldestChild = Some(JourneyModel.EldestChild(partnerEldestChildName, now))
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.relationship.partner.value mustEqual expectedPartner
      }

      "when the applicant's partner is waiting to hear if they are entitled to Child Benefit" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerClaimingChildBenefitPage, false).success.value
          .set(PartnerWaitingForEntitlementDecisionPage, true).success.value
          .set(PartnerEldestChildNamePage, partnerEldestChildName).success.value
          .set(PartnerEldestChildDateOfBirthPage, now).success.value

        val expectedPartner = JourneyModel.Partner(
          name = partnerName,
          dateOfBirth = now,
          nationality = partnerNationality,
          nationalInsuranceNumber = None,
          employmentStatus = partnerEmployment,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyClaimingChildBenefit = false,
          waitingToHearAboutEntitlement = Some(true),
          eldestChild = Some(JourneyModel.EldestChild(partnerEldestChildName, now))
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.relationship.partner.value mustEqual expectedPartner
      }

      "when a child has previous names" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildHasPreviousNamePage(Index(0)), true).success.value
          .set(ChildNameChangedByDeedPollPage(Index(0)), true).success.value
          .set(ChildPreviousNamePage(Index(0), Index(0)), childPreviousName1).success.value
          .set(ChildPreviousNamePage(Index(0), Index(1)), childPreviousName2).success.value

        val expectedChildDetails = JourneyModel.Child(
          name = childName,
          nameChangedByDeedPoll = Some(true),
          previousNames = List(childPreviousName1, childPreviousName2),
          biologicalSex = ChildBiologicalSex.Female,
          dateOfBirth = now,
          countryOfRegistration = ChildBirthRegistrationCountry.England,
          birthCertificateNumber = Some(systemNumber),
          relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
          adoptingThroughLocalAuthority = false,
          previousClaimant = None
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.children.toList must contain only expectedChildDetails
      }

      "when a child was born in England and their birth certificate does not have a system number" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.England).success.value
          .set(BirthCertificateHasSystemNumberPage(Index(0)), false).success.value


        val expectedChildDetails = JourneyModel.Child(
          name = childName,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = ChildBiologicalSex.Female,
          dateOfBirth = now,
          countryOfRegistration = ChildBirthRegistrationCountry.England,
          birthCertificateNumber = None,
          relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
          adoptingThroughLocalAuthority = false,
          previousClaimant = None
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.children.toList must contain only expectedChildDetails
      }

      "when a child was born in Wales and their birth certificate does not have a system number" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.Wales).success.value
          .set(BirthCertificateHasSystemNumberPage(Index(0)), false).success.value


        val expectedChildDetails = JourneyModel.Child(
          name = childName,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = ChildBiologicalSex.Female,
          dateOfBirth = now,
          countryOfRegistration = ChildBirthRegistrationCountry.Wales,
          birthCertificateNumber = None,
          relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
          adoptingThroughLocalAuthority = false,
          previousClaimant = None
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.children.toList must contain only expectedChildDetails
      }

      "when a child was born in Scotland" - {

        "and their birth certificate has details" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .withMinimalSingleIncomeDetails
            .withMinimalPaymentDetails
            .set(RelationshipStatusPage, Single).success.value
            .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.Scotland).success.value
            .set(ScottishBirthCertificateHasNumbersPage(Index(0)), true).success.value
            .set(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBirthCertificateDetails).success.value

          val expectedChildDetails = JourneyModel.Child(
            name = childName,
            nameChangedByDeedPoll = None,
            previousNames = Nil,
            biologicalSex = ChildBiologicalSex.Female,
            dateOfBirth = now,
            countryOfRegistration = ChildBirthRegistrationCountry.Scotland,
            birthCertificateNumber = Some(scottishBirthCertificateDetails),
            relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
            adoptingThroughLocalAuthority = false,
            previousClaimant = None
          )

          val (errors, data) = JourneyModel.from(answers).pad

          errors mustBe empty
          data.value.children.toList must contain only expectedChildDetails
        }

        "and their birth certificate does not have details" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .withMinimalSingleIncomeDetails
            .withMinimalPaymentDetails
            .set(RelationshipStatusPage, Single).success.value
            .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.Scotland).success.value
            .set(ScottishBirthCertificateHasNumbersPage(Index(0)), false).success.value

          val expectedChildDetails = JourneyModel.Child(
            name = childName,
            nameChangedByDeedPoll = None,
            previousNames = Nil,
            biologicalSex = ChildBiologicalSex.Female,
            dateOfBirth = now,
            countryOfRegistration = ChildBirthRegistrationCountry.Scotland,
            birthCertificateNumber = None,
            relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
            adoptingThroughLocalAuthority = false,
            previousClaimant = None
          )

          val (errors, data) = JourneyModel.from(answers).pad

          errors mustBe empty
          data.value.children.toList must contain only expectedChildDetails
        }
      }

      "when the applicant is HM Forces or a civil servant abroad" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(LivedOrWorkedAbroadPage, true).success.value
          .set(ApplicantIsHmfOrCivilServantPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.applicant.memberOfHMForcesOrCivilServantAbroad mustEqual true
      }

      "when the applicant's partner is HM Forces or a civil servant abroad" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(ApplicantOrPartnerIncomePage, Income.BelowLowerThreshold).success.value
          .set(ApplicantOrPartnerBenefitsPage, applicantBenefits).success.value
          .set(LivedOrWorkedAbroadPage, true).success.value
          .set(ApplicantIsHmfOrCivilServantPage, false).success.value
          .set(PartnerIsHmfOrCivilServantPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.applicant.memberOfHMForcesOrCivilServantAbroad mustEqual false
        data.value.relationship.partner.value.memberOfHMForcesOrCivilServantAbroad mustEqual true
      }
    }

    "must fail and report the missing pages" - {

      "when any mandatory data is missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .remove(ApplicantNamePage).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ApplicantNamePage
        data mustBe empty
      }

      "when the applicant is married and partner details are missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Married).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only (
          PartnerNamePage,
          PartnerDateOfBirthPage,
          PartnerNationalityPage,
          PartnerNinoKnownPage,
          PartnerEmploymentStatusPage,
          PartnerClaimingChildBenefitPage
        )
        data mustBe empty
      }

      "when the applicant is cohabiting and cohabitation date is missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Cohabiting).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only CohabitationDatePage
        data mustBe empty
      }

      "when the applicant is cohabiting and partner details are missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Cohabiting).success.value
          .set(CohabitationDatePage, now).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only (
          PartnerNamePage,
          PartnerDateOfBirthPage,
          PartnerNationalityPage,
          PartnerNinoKnownPage,
          PartnerEmploymentStatusPage,
          PartnerClaimingChildBenefitPage
        )
        data mustBe empty
      }

      "when the applicant is separated and separation date is missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Separated).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only SeparationDatePage
        data mustBe empty
      }

      "when the applicant is currently receiving child benefit" - {

        "and whether they want to be paid to their existing account is missing" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .withMinimalSingleIncomeDetails
            .set(RelationshipStatusPage, Single).success.value
            .set(CurrentlyReceivingChildBenefitPage, true).success.value
            .set(EldestChildNamePage, eldestChildName).success.value
            .set(EldestChildDateOfBirthPage, now).success.value

          val (errors, data) = JourneyModel.from(answers).pad

          errors.value.toChain.toList must contain only WantToBePaidToExistingAccountPage
          data mustBe empty
        }

        "and their eldest child's details are missing" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .withMinimalSingleIncomeDetails
            .set(RelationshipStatusPage, Single).success.value
            .set(CurrentlyReceivingChildBenefitPage, true).success.value
            .set(WantToBePaidToExistingAccountPage, true).success.value

          val (errors, data) = JourneyModel.from(answers).pad

          errors.value.toChain.toList must contain theSameElementsInOrderAs Seq(
            EldestChildNamePage,
            EldestChildDateOfBirthPage
          )

          data mustBe empty
        }

        "and they do not want to be paid to their existing account but whether they have a suitable account is missing" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .withMinimalSingleIncomeDetails
            .set(RelationshipStatusPage, Single).success.value
            .set(CurrentlyReceivingChildBenefitPage, true).success.value
            .set(EldestChildNamePage, eldestChildName).success.value
            .set(EldestChildDateOfBirthPage, now).success.value
            .set(WantToBePaidToExistingAccountPage, false).success.value

          val (errors, data) = JourneyModel.from(answers).pad

          errors.value.toChain.toList must contain only ApplicantHasSuitableAccountPage
          data mustBe empty
        }

        "and they do not want to be paid to their existing account but the account details are missing" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .withMinimalSingleIncomeDetails
            .set(RelationshipStatusPage, Single).success.value
            .set(CurrentlyReceivingChildBenefitPage, true).success.value
            .set(EldestChildNamePage, eldestChildName).success.value
            .set(EldestChildDateOfBirthPage, now).success.value
            .set(WantToBePaidToExistingAccountPage, false).success.value
            .set(ApplicantHasSuitableAccountPage, true).success.value

          val (errors, data) = JourneyModel.from(answers).pad

          errors.value.toChain.toList must contain theSameElementsAs Seq(BankAccountHolderPage, BankAccountDetailsPage)
          data mustBe empty
        }
      }

      "when the applicant wants to be paid but whether they have a suitable account is missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(CurrentlyReceivingChildBenefitPage, false).success.value
          .set(WantToBePaidPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ApplicantHasSuitableAccountPage
        data mustBe empty
      }

      "when the applicant wants to be paid but their account details are missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(CurrentlyReceivingChildBenefitPage, false).success.value
          .set(WantToBePaidPage, true).success.value
          .set(ApplicantHasSuitableAccountPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain theSameElementsAs Seq(BankAccountHolderPage, BankAccountDetailsPage)
        data mustBe empty
      }

      "when the applicant says they have previous names but none are provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantHasPreviousFamilyNamePage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only AllPreviousFamilyNames
        data mustBe empty
      }

      "when the applicant says they know their NINO but none is provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantNinoKnownPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ApplicantNinoPage
        data mustBe empty
      }

      "when the applicant said they have lived at their current address less than a year but no previous address is provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantLivedAtCurrentAddressOneYearPage, false).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ApplicantPreviousUkAddressPage
        data mustBe empty
      }

      "when the applicant said they know their partner's NINO but one is not provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerNinoKnownPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only PartnerNinoPage
        data mustBe empty
      }

      "when the applicant said their partner is entitled to Child Benefit but their partner's eldest child's details are not provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerClaimingChildBenefitPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain theSameElementsInOrderAs Seq(
          PartnerEldestChildNamePage,
          PartnerEldestChildDateOfBirthPage
        )

        data mustBe empty
      }

      "when the applicant said their partner is waiting to hear about entitlement to Child Benefit but their partner's eldest child's details are not provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalCoupleIncomeDetails
          .withMinimalPaymentDetails
          .withMinimalPartnerDetails
          .set(RelationshipStatusPage, Married).success.value
          .set(PartnerClaimingChildBenefitPage, false).success.value
          .set(PartnerWaitingForEntitlementDecisionPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only (
          PartnerEldestChildNamePage,
          PartnerEldestChildDateOfBirthPage
        )

        data mustBe empty
      }

      "when the applicant said a child had previous names, but none are provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildHasPreviousNamePage(Index(0)), true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only (
          ChildNameChangedByDeedPollPage(Index(0)),
          AllChildPreviousNames(Index(0))
        )

        data mustBe empty
      }

      "when a child's birth was registered in England or Wales, the user said their birth certificate has a system number, but none is provided" in {

        val country = Gen.oneOf(BirthCountry.England, BirthCountry.Wales).sample.value

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), country).success.value
          .set(BirthCertificateHasSystemNumberPage(Index(0)), true).success.value
          .remove(ChildBirthCertificateSystemNumberPage(Index(0))).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ChildBirthCertificateSystemNumberPage(Index(0))

        data mustBe empty
      }

      "when a child's birth was registered in Scotland, the user said their birth certificate had details, but none are provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ScottishBirthCertificateHasNumbersPage(Index(0)), true).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), BirthCountry.Scotland).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ChildScottishBirthCertificateDetailsPage(Index(0))

        data mustBe empty
      }

      "when someone has claimed for this child before, but their details are not present" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(AnyoneClaimedForChildBeforePage(Index(0)), true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only (
          PreviousClaimantNamePage(Index(0)),
          PreviousClaimantUkAddressPage(Index(0))
        )

        data mustBe empty
      }

      "when no children are present" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only AllChildSummaries

        data mustBe empty
      }
    }
  }

  implicit class UserAnswersOps(answers: UserAnswers) {

    def withMinimalApplicantDetails: UserAnswers =
      answers
        .set(ApplicantNamePage, applicantName).success.value
        .set(ApplicantHasPreviousFamilyNamePage, false).success.value
        .set(ApplicantNinoKnownPage, false).success.value
        .set(ApplicantDateOfBirthPage, now).success.value
        .set(ApplicantCurrentUkAddressPage, currentAddress).success.value
        .set(ApplicantLivedAtCurrentAddressOneYearPage, true).success.value
        .set(ApplicantPhoneNumberPage, phoneNumber).success.value
        .set(BestTimeToContactPage, bestTimes).success.value
        .set(ApplicantNationalityPage, applicantNationality).success.value
        .set(ApplicantEmploymentStatusPage, applicantEmployment).success.value

    def withMinimalPartnerDetails: UserAnswers =
      answers
        .set(PartnerNamePage, partnerName).success.value
        .set(PartnerNinoKnownPage, false).success.value
        .set(PartnerDateOfBirthPage, now).success.value
        .set(PartnerNationalityPage, partnerNationality).success.value
        .set(PartnerEmploymentStatusPage, partnerEmployment).success.value
        .set(PartnerClaimingChildBenefitPage, false).success.value
        .set(PartnerWaitingForEntitlementDecisionPage, false).success.value

    def withOneChild: UserAnswers =
      answers
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

    def withMinimalSingleIncomeDetails: UserAnswers =
      answers
        .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
        .set(ApplicantBenefitsPage, applicantBenefits).success.value

    def withMinimalCoupleIncomeDetails: UserAnswers =
      answers
        .set(ApplicantOrPartnerIncomePage, Income.BelowLowerThreshold).success.value
        .set(ApplicantOrPartnerBenefitsPage, applicantBenefits).success.value

    def withMinimalPaymentDetails: UserAnswers =
      answers
        .set(CurrentlyReceivingChildBenefitPage, false).success.value
        .set(WantToBePaidPage, false).success.value
  }
}
