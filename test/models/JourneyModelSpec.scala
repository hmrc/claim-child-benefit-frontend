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
import models.ApplicantRelationshipToChild.AdoptingChild
import models.{ChildBirthRegistrationCountry => Country}
import models.RelationshipStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{EitherValues, OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
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
  private val applicantName = AdultName(None, "first", None, "last")
  private val applicantNino = arbitrary[Nino].sample.value
  private val currentAddress = Address("line 1", None, "town", None, "AA11 1AA")
  private val previousAddress = Address("line 1", None, "town", None, "BB22 2BB")
  private val phoneNumber = "07777 777777"
  private val bestTimes = Set[BestTimeToContact](BestTimeToContact.Morning)
  private val applicantBenefits = Set[Benefits](Benefits.NoneOfTheAbove)
  private val applicantNationality = "applicant nationality"
  private val applicantEmployment = Set[ApplicantEmploymentStatus](ApplicantEmploymentStatus.Employed)
  private val previousName1 = "previous name 1"
  private val previousName2 = "previous name 2"

  private val bankAccountDetails = BankAccountDetails("name", "00000000", "000000", None)
  private val eldestChildName = ChildName("first", None, "last")

  private val partnerName = AdultName(None, "partner first", None, "partner last")
  private val partnerNationality = "partner nationality"
  private val partnerEmployment = Set[PartnerEmploymentStatus](PartnerEmploymentStatus.Employed)
  private val partnerNino = arbitrary[Nino].sample.value
  private val partnerEldestChildName = ChildName("partner child first", None, "partner child last")

  private val childName = ChildName("first", None, "last")
  private val biologicalSex = ChildBiologicalSex.Female
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = "000000000"
  private val scottishBirthCertificateDetails = "0000000000"
  private val childPreviousName1 = ChildName("first 1", None, "last 1")
  private val childPreviousName2 = ChildName("first 2", None, "last 2")
  private val documents = Set[IncludedDocuments](IncludedDocuments.BirthCertificate)

  ".from" - {

    "must create a journey model" - {

      "from minimal answers when the applicant does not have a partner" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantIncomeOver50kPage, false).success.value
          .set(ApplicantBenefitsPage, applicantBenefits).success.value
          .set(ClaimedChildBenefitBeforePage, false).success.value
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
            employmentStatus = applicantEmployment
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
              adoptingThroughLocalAuthority = None,
              previousClaimant = None,
              documents = Set.empty
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
          .set(ApplicantOrPartnerIncomeOver50kPage, false).success.value
          .set(ApplicantOrPartnerBenefitsPage, applicantBenefits).success.value
          .set(ClaimedChildBenefitBeforePage, false).success.value
          .set(WantToBePaidPage, false).success.value
          .set(PartnerNamePage, partnerName).success.value
          .set(PartnerNinoKnownPage, false).success.value
          .set(PartnerDateOfBirthPage, now).success.value
          .set(PartnerNationalityPage, partnerNationality).success.value
          .set(PartnerEmploymentStatusPage, partnerEmployment).success.value
          .set(PartnerEntitledToChildBenefitPage, false).success.value
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
            employmentStatus = applicantEmployment
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
              currentlyEntitledToChildBenefit = false,
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
              adoptingThroughLocalAuthority = None,
              previousClaimant = None,
              documents = Set.empty
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
          .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value
          .set(ApplicantRelationshipToChildPage(Index(1)), relationshipToChild).success.value
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
            employmentStatus = applicantEmployment
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
              currentlyEntitledToChildBenefit = false,
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
              adoptingThroughLocalAuthority = None,
              previousClaimant = None,
              documents = Set.empty
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
                adoptingThroughLocalAuthority = None,
                previousClaimant = None,
                documents = Set.empty
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
            .set(ApplicantIncomeOver50kPage, false).success.value
            .set(ApplicantBenefitsPage, applicantBenefits).success.value
            .set(ClaimedChildBenefitBeforePage, true).success.value
            .set(CurrentlyEntitledToChildBenefitPage, true).success.value
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
              .set(ApplicantIncomeOver50kPage, false).success.value
              .set(ApplicantBenefitsPage, applicantBenefits).success.value
              .set(ClaimedChildBenefitBeforePage, true).success.value
              .set(CurrentlyEntitledToChildBenefitPage, true).success.value
              .set(CurrentlyReceivingChildBenefitPage, true).success.value
              .set(EldestChildNamePage, eldestChildName).success.value
              .set(EldestChildDateOfBirthPage, now).success.value
              .set(WantToBePaidToExistingAccountPage, false).success.value
              .set(ApplicantHasSuitableAccountPage, true).success.value
              .set(BankAccountDetailsPage, bankAccountDetails).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.ExistingFrequency(
              bankAccountDetails = Some(bankAccountDetails),
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
              .set(ApplicantIncomeOver50kPage, false).success.value
              .set(ApplicantBenefitsPage, applicantBenefits).success.value
              .set(ClaimedChildBenefitBeforePage, true).success.value
              .set(CurrentlyEntitledToChildBenefitPage, true).success.value
              .set(CurrentlyReceivingChildBenefitPage, true).success.value
              .set(EldestChildNamePage, eldestChildName).success.value
              .set(EldestChildDateOfBirthPage, now).success.value
              .set(WantToBePaidToExistingAccountPage, false).success.value
              .set(ApplicantHasSuitableAccountPage, false).success.value

            val expectedPaymentPreference = JourneyModel.PaymentPreference.ExistingFrequency(
              bankAccountDetails = None,
              eldestChild = JourneyModel.EldestChild(eldestChildName, now)
            )

            val (errors, data) = JourneyModel.from(answers).pad

            errors mustBe empty
            data.value.paymentPreference mustEqual expectedPaymentPreference
          }
        }
      }

      "when the applicant is not already receiving child benefit" - {

        "and is entitled to child benefit" - {

          val baseAnswers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .set(RelationshipStatusPage, Single).success.value
            .set(ApplicantIncomeOver50kPage, false).success.value
            .set(ApplicantBenefitsPage, applicantBenefits).success.value
            .set(ClaimedChildBenefitBeforePage, true).success.value
            .set(CurrentlyEntitledToChildBenefitPage, true).success.value
            .set(CurrentlyReceivingChildBenefitPage, false).success.value

          "and wants to be paid every 4 weeks" - {

            "and has a suitable account" in {

              val answers = baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, true).success.value
                .set(BankAccountDetailsPage, bankAccountDetails).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
                bankAccountDetails = Some(bankAccountDetails)
              )

              val (errors, data) = JourneyModel.from(answers).pad

              errors mustBe empty
              data.value.paymentPreference mustEqual expectedPaymentPreference
            }

            "and does not have a suitable account" in {

              val answers = baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
                bankAccountDetails = None
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
                .set(BankAccountDetailsPage, bankAccountDetails).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.Weekly(
                bankAccountDetails = Some(bankAccountDetails)
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
                bankAccountDetails = None
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

        "and is not entitled to child benefit" - {

          val baseAnswers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .set(RelationshipStatusPage, Single).success.value
            .set(ApplicantIncomeOver50kPage, false).success.value
            .set(ApplicantBenefitsPage, applicantBenefits).success.value
            .set(ClaimedChildBenefitBeforePage, true).success.value
            .set(CurrentlyEntitledToChildBenefitPage, false).success.value

          "and wants to be paid every 4 weeks" - {

            "and has a suitable account" in {

              val answers = baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, true).success.value
                .set(BankAccountDetailsPage, bankAccountDetails).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
                bankAccountDetails = Some(bankAccountDetails)
              )

              val (errors, data) = JourneyModel.from(answers).pad

              errors mustBe empty
              data.value.paymentPreference mustEqual expectedPaymentPreference
            }

            "and does not have a suitable account" in {

              val answers = baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
                bankAccountDetails = None
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
                .set(BankAccountDetailsPage, bankAccountDetails).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.Weekly(
                bankAccountDetails = Some(bankAccountDetails)
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
                bankAccountDetails = None
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

        "and has never claimed child benefit before" - {

          val baseAnswers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .set(RelationshipStatusPage, Single).success.value
            .set(ApplicantIncomeOver50kPage, false).success.value
            .set(ApplicantBenefitsPage, applicantBenefits).success.value
            .set(ClaimedChildBenefitBeforePage, false).success.value

          "and wants to be paid every 4 weeks" - {

            "and has a suitable account" in {

              val answers = baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, true).success.value
                .set(BankAccountDetailsPage, bankAccountDetails).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
                bankAccountDetails = Some(bankAccountDetails)
              )

              val (errors, data) = JourneyModel.from(answers).pad

              errors mustBe empty
              data.value.paymentPreference mustEqual expectedPaymentPreference
            }

            "and does not have a suitable account" in {

              val answers = baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.EveryFourWeeks(
                bankAccountDetails = None
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
                .set(BankAccountDetailsPage, bankAccountDetails).success.value

              val expectedPaymentPreference = JourneyModel.PaymentPreference.Weekly(
                bankAccountDetails = Some(bankAccountDetails)
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
                bankAccountDetails = None
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
          employmentStatus = applicantEmployment
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
          employmentStatus = applicantEmployment
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
          .set(ApplicantPreviousAddressPage, previousAddress).success.value

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
          employmentStatus = applicantEmployment
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
          currentlyEntitledToChildBenefit = false,
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
          .set(PartnerEntitledToChildBenefitPage, true).success.value
          .set(PartnerEldestChildNamePage, partnerEldestChildName).success.value
          .set(PartnerEldestChildDateOfBirthPage, now).success.value

        val expectedPartner = JourneyModel.Partner(
          name = partnerName,
          dateOfBirth = now,
          nationality = partnerNationality,
          nationalInsuranceNumber = None,
          employmentStatus = partnerEmployment,
          currentlyEntitledToChildBenefit = true,
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
          .set(PartnerEntitledToChildBenefitPage, false).success.value
          .set(PartnerWaitingForEntitlementDecisionPage, true).success.value
          .set(PartnerEldestChildNamePage, partnerEldestChildName).success.value
          .set(PartnerEldestChildDateOfBirthPage, now).success.value

        val expectedPartner = JourneyModel.Partner(
          name = partnerName,
          dateOfBirth = now,
          nationality = partnerNationality,
          nationalInsuranceNumber = None,
          employmentStatus = partnerEmployment,
          currentlyEntitledToChildBenefit = false,
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
          adoptingThroughLocalAuthority = None,
          previousClaimant = None,
          documents = Set.empty
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.children.toList must contain only expectedChildDetails
      }

      "when a child was born in Scotland" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.Scotland).success.value
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
          adoptingThroughLocalAuthority = None,
          previousClaimant = None,
          documents = Set.empty
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.children.toList must contain only expectedChildDetails
      }

      "when a child was born outside of England, Wales and Scotland" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.Other).success.value
          .set(IncludedDocumentsPage(Index(0)), documents).success.value

        val expectedChildDetails = JourneyModel.Child(
          name = childName,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = ChildBiologicalSex.Female,
          dateOfBirth = now,
          countryOfRegistration = ChildBirthRegistrationCountry.Other,
          birthCertificateNumber = None,
          relationshipToApplicant = ApplicantRelationshipToChild.BirthChild,
          adoptingThroughLocalAuthority = None,
          previousClaimant = None,
          documents = documents
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.children.toList must contain only expectedChildDetails
      }

      "when a child is being adopted" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantRelationshipToChildPage(Index(0)), ApplicantRelationshipToChild.AdoptingChild).success.value
          .set(AdoptingThroughLocalAuthorityPage(Index(0)), true).success.value

        val expectedChildDetails = JourneyModel.Child(
          name = childName,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = ChildBiologicalSex.Female,
          dateOfBirth = now,
          countryOfRegistration = ChildBirthRegistrationCountry.England,
          birthCertificateNumber = Some(systemNumber),
          relationshipToApplicant = ApplicantRelationshipToChild.AdoptingChild,
          adoptingThroughLocalAuthority = Some(true),
          previousClaimant = None,
          documents = Set.empty
        )

        val (errors, data) = JourneyModel.from(answers).pad

        errors mustBe empty
        data.value.children.toList must contain only expectedChildDetails
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
          PartnerEntitledToChildBenefitPage
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
          PartnerEntitledToChildBenefitPage
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

      "when the applicant has claimed child benefit before and the next question missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ClaimedChildBenefitBeforePage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only CurrentlyEntitledToChildBenefitPage
        data mustBe empty
      }

      "when the applicant is currently entitled child benefit and the next question missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ClaimedChildBenefitBeforePage, true).success.value
          .set(CurrentlyEntitledToChildBenefitPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only CurrentlyReceivingChildBenefitPage
        data mustBe empty
      }

      "when the applicant is currently receiving child benefit" - {

        "and whether they want to be paid to their existing account is missing" in {

          val answers = UserAnswers("id")
            .withMinimalApplicantDetails
            .withOneChild
            .withMinimalSingleIncomeDetails
            .set(RelationshipStatusPage, Single).success.value
            .set(ClaimedChildBenefitBeforePage, true).success.value
            .set(CurrentlyEntitledToChildBenefitPage, true).success.value
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
            .set(ClaimedChildBenefitBeforePage, true).success.value
            .set(CurrentlyEntitledToChildBenefitPage, true).success.value
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
            .set(ClaimedChildBenefitBeforePage, true).success.value
            .set(CurrentlyEntitledToChildBenefitPage, true).success.value
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
            .set(ClaimedChildBenefitBeforePage, true).success.value
            .set(CurrentlyEntitledToChildBenefitPage, true).success.value
            .set(CurrentlyReceivingChildBenefitPage, true).success.value
            .set(EldestChildNamePage, eldestChildName).success.value
            .set(EldestChildDateOfBirthPage, now).success.value
            .set(WantToBePaidToExistingAccountPage, false).success.value
            .set(ApplicantHasSuitableAccountPage, true).success.value

          val (errors, data) = JourneyModel.from(answers).pad

          errors.value.toChain.toList must contain only BankAccountDetailsPage
          data mustBe empty
        }
      }

      "when the applicant wants to be paid but whether they have a suitable account is missing" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ClaimedChildBenefitBeforePage, false).success.value
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
          .set(ClaimedChildBenefitBeforePage, false).success.value
          .set(WantToBePaidPage, true).success.value
          .set(ApplicantHasSuitableAccountPage, true).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only BankAccountDetailsPage
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

        errors.value.toChain.toList must contain only ApplicantPreviousAddressPage
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
          .set(PartnerEntitledToChildBenefitPage, true).success.value

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
          .set(PartnerEntitledToChildBenefitPage, false).success.value
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

      "when a child's birth was registered in England or Wales, but no birth certificate system number is provided" in {

        val country = Gen.oneOf(Country.England, Country.Wales).sample.value

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), country).success.value
          .remove(ChildBirthCertificateSystemNumberPage(Index(0))).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ChildBirthCertificateSystemNumberPage(Index(0))

        data mustBe empty
      }

      "when a child's birth was registered in Scotland, but no Scottish birth certificate details are provided" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), Country.Scotland).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only ChildScottishBirthCertificateDetailsPage(Index(0))

        data mustBe empty
      }

      "when a child's birth was registered outside the UK or in an unknown country, but no documents are included" in {

        val country = Gen.oneOf(Country.Other, Country.Unknown).sample.value

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ChildBirthRegistrationCountryPage(Index(0)), country).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only IncludedDocumentsPage(Index(0))

        data mustBe empty
      }

      "when a child is being adopted, but whether it is through a local authority is not present" in {

        val answers = UserAnswers("id")
          .withMinimalApplicantDetails
          .withOneChild
          .withMinimalSingleIncomeDetails
          .withMinimalPaymentDetails
          .set(RelationshipStatusPage, Single).success.value
          .set(ApplicantRelationshipToChildPage(Index(0)), AdoptingChild).success.value

        val (errors, data) = JourneyModel.from(answers).pad

        errors.value.toChain.toList must contain only AdoptingThroughLocalAuthorityPage(Index(0))

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
          PreviousClaimantAddressPage(Index(0))
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
        .set(ApplicantCurrentAddressPage, currentAddress).success.value
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
        .set(PartnerEntitledToChildBenefitPage, false).success.value
        .set(PartnerWaitingForEntitlementDecisionPage, false).success.value

    def withOneChild: UserAnswers =
      answers
        .set(ChildNamePage(Index(0)), childName).success.value
        .set(ChildHasPreviousNamePage(Index(0)), false).success.value
        .set(ChildBiologicalSexPage(Index(0)), biologicalSex).success.value
        .set(ChildDateOfBirthPage(Index(0)), now).success.value
        .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.England).success.value
        .set(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber).success.value
        .set(ApplicantRelationshipToChildPage(Index(0)), relationshipToChild).success.value
        .set(AnyoneClaimedForChildBeforePage(Index(0)), false).success.value

    def withMinimalSingleIncomeDetails: UserAnswers =
      answers
        .set(ApplicantIncomeOver50kPage, false).success.value
        .set(ApplicantBenefitsPage, applicantBenefits).success.value

    def withMinimalCoupleIncomeDetails: UserAnswers =
      answers
        .set(ApplicantOrPartnerIncomeOver50kPage, false).success.value
        .set(ApplicantOrPartnerBenefitsPage, applicantBenefits).success.value

    def withMinimalPaymentDetails: UserAnswers =
      answers
        .set(ClaimedChildBenefitBeforePage, false).success.value
        .set(WantToBePaidPage, false).success.value
  }
}
