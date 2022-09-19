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

package journey

import generators.ModelGenerators
import models.CurrentlyReceivingChildBenefit.{GettingPayments, NotGettingPayments}
import models.RelationshipStatus._
import models.{BankAccountDetails, BankAccountHolder, Benefits, ChildName, PaymentFrequency, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.RelationshipStatusPage
import pages.applicant.ApplicantHasPreviousFamilyNamePage
import pages.income.ApplicantOrPartnerBenefitsPage
import pages.payments._

import java.time.LocalDate

class PaymentsJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def bankDetails = arbitrary[BankAccountDetails].sample.value

  "users who are currently receiving Child Benefit" - {

    val childName = ChildName("first", None, "last")
    val childDob  = LocalDate.now
    val currentlyReceiving = Gen.oneOf(GettingPayments, NotGettingPayments).sample.value

    "who want to be paid to their existing bank account" - {

      "must proceed to the Applicant section" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            submitAnswer(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, childDob),
            submitAnswer(WantToBePaidToExistingAccountPage, true),
            pageMustBe(ApplicantHasPreviousFamilyNamePage)
          )
      }
    }

    "who do not want to be paid to their existing bank account" -{

      "must proceed to the bank details section" in {

        val currentlyReceiving = Gen.oneOf(GettingPayments, NotGettingPayments).sample.value

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            submitAnswer(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, childDob),
            submitAnswer(WantToBePaidToExistingAccountPage, false),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, BankAccountHolder.Applicant),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(ApplicantHasPreviousFamilyNamePage)
          )
      }
    }
  }

  "users wanting to be paid Child Benefit" - {

    "who are Married" - {

      "and not receiving qualifying benefits must be asked for bank account details" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Married).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, BankAccountHolder.Applicant),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(ApplicantHasPreviousFamilyNamePage)
          )
      }

      "and receive qualifying benefits must be asked if they want to be paid weekly then be asked their bank account details" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Married).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.qualifyingBenefits.head)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(ApplicantHasSuitableAccountPage)
          )
      }
    }

    "who are Cohabiting" - {

      "and not receiving qualifying benefits must be asked for bank account details" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Cohabiting).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            pageMustBe(ApplicantHasSuitableAccountPage)
          )
      }


      "and receiving qualifying benefits must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Cohabiting).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.qualifyingBenefits.head)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(ApplicantHasSuitableAccountPage)
          )
      }
    }

    "who are Single" - {

      "must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Single).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(ApplicantHasSuitableAccountPage)
          )
      }
    }

    "who are Separated" - {

      "must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Separated).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(ApplicantHasSuitableAccountPage)
          )
      }
    }

    "who are Divorced" - {

      "must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Divorced).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(ApplicantHasSuitableAccountPage)
          )
      }
    }

    "who are Widowed" - {

      "must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Widowed).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(ApplicantHasSuitableAccountPage)
          )
      }
    }
  }

  "users not wanting to be paid Child Benefit" - {

    "must proceed to the Applicant section" in {

      startingFrom(WantToBePaidPage)
        .run(
          submitAnswer(WantToBePaidPage, false),
          pageMustBe(ApplicantHasPreviousFamilyNamePage)
        )
    }
  }

  "users who do not have a suitable account" - {

    "must proceed to the Applicant section" in {

      startingFrom(ApplicantHasSuitableAccountPage)
        .run(
          submitAnswer(ApplicantHasSuitableAccountPage, false),
          pageMustBe(ApplicantHasPreviousFamilyNamePage)
        )
    }
  }
}