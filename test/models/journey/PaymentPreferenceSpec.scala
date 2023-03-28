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

package models.journey

import generators.ModelGenerators
import models.{ChildName, CurrentlyReceivingChildBenefit, PaymentFrequency, RelationshipDetails, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.applicant.{CurrentlyReceivingChildBenefitPage, EldestChildDateOfBirthPage, EldestChildNamePage}
import pages.payments._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class PaymentPreferenceSpec extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues {

  private val childName = arbitrary[ChildName].sample.value
  private val nino = arbitrary[Nino].sample.value

  ".build" - {

    "when the user is authenticated" - {

      "and has previously claimed Child Benefit" - {

        val baseAnswers = UserAnswers("id", nino = Some(nino.value), relationshipDetails = Some(RelationshipDetails(hasClaimedChildBenefit = true)))

        "and wants to be paid" - {

          "must be Existing Account" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, true).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.ExistingAccount(None)
            errors must not be defined
          }
        }

        "and does not want to be paid" - {

          "must be DoNotPay" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.DoNotPay(None)
            errors must not be defined
          }
        }

        "must return errors when whether they want to be paid is missing" in {

          val (errors, data) = PaymentPreference.build(baseAnswers).pad

          data must not be defined
          errors.value.toChain.toList must contain only WantToBePaidPage
        }

      }

      "and has not previously claimed Child Benefit" - {

        val baseAnswers = UserAnswers("id", nino = Some(nino.value), relationshipDetails = Some(RelationshipDetails(hasClaimedChildBenefit = false)))

        "and wants to be paid weekly" - {

          "must return Weekly" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.Weekly(None, None)
            errors must not be defined
          }

          "must return errors when payment details are missing" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors.value.toChain.toList must contain only ApplicantHasSuitableAccountPage
          }
        }

        "and wants to be paid every four weeks" - {

          "must return EveryFourWeeks" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.EveryFourWeeks(None, None)
            errors must not be defined
          }

          "must return errors when payment details are missing" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors.value.toChain.toList must contain only ApplicantHasSuitableAccountPage
          }
        }

        "and wants to be paid but payment frequency is missing" - {

          "must return Every Four Weeks" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, true).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.EveryFourWeeks(None, None)
            errors must not be defined
          }
        }

        "and does not want to be paid" - {

          "must return Do Not Pay" in {

            val answers =
              baseAnswers
                .set(WantToBePaidPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.DoNotPay(None)
            errors must not be defined
          }
        }

        "must return errors when whether they want to be paid is missing" in {

          val (errors, data) = PaymentPreference.build(baseAnswers).pad

          data must not be defined
          errors.value.toChain.toList must contain only WantToBePaidPage
        }
      }
    }

    "when the user is unauthenticated" - {

      "and is currently getting Child Benefit payments" - {

        "must return Existing Account when they want to be paid" in {

          val answers =
            UserAnswers("id")
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.GettingPayments).success.value
              .set(WantToBePaidPage, true).success.value
              .set(EldestChildNamePage, childName).success.value
              .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

          val (errors, data) = PaymentPreference.build(answers).pad

          data.value mustEqual PaymentPreference.ExistingAccount(Some(EldestChild(childName, LocalDate.now)))
          errors must not be defined
        }

        "must return DoNotPay when they do not want to be paid" in {

          val answers =
            UserAnswers("id")
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.GettingPayments).success.value
              .set(WantToBePaidPage, false).success.value
              .set(EldestChildNamePage, childName).success.value
              .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

          val (errors, data) = PaymentPreference.build(answers).pad

          data.value mustEqual PaymentPreference.DoNotPay(Some(EldestChild(childName, LocalDate.now)))
          errors must not be defined
        }

        "must return errors when whether they want to be paid is missing" in {

          val answers =
            UserAnswers("id")
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.GettingPayments).success.value
              .set(EldestChildNamePage, childName).success.value
              .set(EldestChildDateOfBirthPage, LocalDate.now).success.value
          val (errors, data) = PaymentPreference.build(answers).pad

          data must not be defined
          errors.value.toChain.toList must contain only WantToBePaidPage
        }

        "must return errors when they want to be paid but their eldest child's details are missing" in {

          val answers =
            UserAnswers("id")
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.GettingPayments).success.value
              .set(WantToBePaidPage, true).success.value

          val (errors, data) = PaymentPreference.build(answers).pad

          data must not be defined
          errors mustBe defined
        }

        "must return errors when they do not want to be paid but their eldest child's details are missing" in {

          val answers =
            UserAnswers("id")
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.GettingPayments).success.value
              .set(WantToBePaidPage, false).success.value

          val (errors, data) = PaymentPreference.build(answers).pad

          data must not be defined
          errors mustBe defined
        }
      }

      "and is currently claiming but not getting payments" - {

        "and wants to be paid weekly" - {

          "must return Weekly" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value
                .set(EldestChildNamePage, childName).success.value
                .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.Weekly(None, Some(EldestChild(childName, LocalDate.now)))
            errors must not be defined
          }

          "must return errors when their eldest child's details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }

          "must return errors when payment details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value
                .set(EldestChildNamePage, childName).success.value
                .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }
        }

        "and wants to be paid every four weeks" - {

          "must return EveryFourWeeks" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value
                .set(EldestChildNamePage, childName).success.value
                .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.EveryFourWeeks(None, Some(EldestChild(childName, LocalDate.now)))
            errors must not be defined
          }

          "must return errors when their eldest child's details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }

          "must return errors when payment details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(EldestChildNamePage, childName).success.value
                .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }
        }

        "and wants to be paid but payment frequency is missing" - {

          "must return Every Four Weeks" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, true).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value
                .set(EldestChildNamePage, childName).success.value
                .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.EveryFourWeeks(None, Some(EldestChild(childName, LocalDate.now)))
            errors must not be defined
          }
        }

        "and does not want to be paid" - {

          "must return Do Not Pay" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, false).success.value
                .set(EldestChildNamePage, childName).success.value
                .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.DoNotPay(Some(EldestChild(childName, LocalDate.now)))
            errors must not be defined
          }

          "must return errors when their eldest child's details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value
                .set(WantToBePaidPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }
        }

        "must return errors when whether they want to be paid is missing" in {

          val answers =
            UserAnswers("id")
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotGettingPayments).success.value

          val (errors, data) = PaymentPreference.build(answers).pad

          data must not be defined
          errors.value.toChain.toList must contain only WantToBePaidPage
        }
      }

      "and is not claiming Child Benefit" - {

        "and wants to be paid weekly" - {

          "must return Weekly" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.Weekly(None, None)
            errors must not be defined
          }

          "must return errors when payment details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.Weekly).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }
        }

        "and wants to be paid every four weeks" - {

          "must return Every Four Weeks" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.EveryFourWeeks(None, None)
            errors must not be defined
          }

          "must return errors when payment details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
                .set(WantToBePaidPage, true).success.value
                .set(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }
        }

        "and wants to be paid but payment frequency is missing" - {

          "must return Every Four Weeks" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
                .set(WantToBePaidPage, true).success.value
                .set(ApplicantHasSuitableAccountPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.EveryFourWeeks(None, None)
            errors must not be defined
          }

          "must return errors when payment details are missing" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
                .set(WantToBePaidPage, true).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data must not be defined
            errors mustBe defined
          }
        }

        "and does not want to be paid" - {

          "must return Do Not Pay" in {

            val answers =
              UserAnswers("id")
                .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
                .set(WantToBePaidPage, false).success.value

            val (errors, data) = PaymentPreference.build(answers).pad

            data.value mustEqual PaymentPreference.DoNotPay(None)
            errors must not be defined
          }
        }

        "must return errors when whether they want to be paid is missing" in {

          val answers =
            UserAnswers("id")
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value

          val (errors, data) = PaymentPreference.build(answers).pad

          data must not be defined
          errors.value.toChain.toList must contain only WantToBePaidPage
        }
      }

      "must return errors when whether the user is claiming Child Benefit is missing" in {

        val answers = UserAnswers("id")

        val (errors, data) = PaymentPreference.build(answers).pad

        data must not be defined
        errors.value.toChain.toList must contain only CurrentlyReceivingChildBenefitPage
      }
    }
  }
}