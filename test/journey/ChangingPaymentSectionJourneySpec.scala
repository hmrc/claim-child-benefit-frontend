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

package journey

import generators.ModelGenerators
import models.CurrentlyReceivingChildBenefit.{NotClaiming, NotGettingPayments}
import models.RelationshipStatus._
import models.{BankAccountDetails, BankAccountHolder, Benefits, Income, PaymentFrequency}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.income._
import pages.payments._
import pages.{CannotBePaidWeeklyPage, RelationshipStatusPage}

class ChangingPaymentSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val bankDetails = arbitrary[BankAccountDetails].sample.value

  "when the user is married or cohabiting" - {

    val relationship = Gen.oneOf(Married, Cohabiting).sample.value

    "changing their (or their partner's) income must show the tax charge explanation" in {

      val originalIncome = Gen.oneOf(Income.values).sample.value
      val newIncome = Gen.oneOf(Income.values.toSet - originalIncome).sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationship),
        submitAnswer(ApplicantOrPartnerIncomePage, originalIncome),
        submitAnswer(WantToBePaidPage, true),
        goTo(CheckPaymentDetailsPage)
      )

      startingFrom(ApplicantOrPartnerIncomePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantOrPartnerIncomePage),
          submitAnswer(ApplicantOrPartnerIncomePage, newIncome),
          pageMustBe(WantToBePaidPage)
        )
    }

    "and originally said they were not receiving benefits" - {

      "changing to say they're receiving qualifying benefits must ask if they want to be paid weekly then go to Check Payments" in {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationship),
          setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotClaiming),
          submitAnswer(WantToBePaidPage, true),
          submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
          submitAnswer(ApplicantHasSuitableAccountPage, true),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(WantToBePaidPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantOrPartnerBenefitsPage),
            submitAnswer(ApplicantOrPartnerBenefitsPage, Benefits.qualifyingBenefits),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(CheckPaymentDetailsPage)
          )
      }
    }

    "and originally said they were receiving qualifying benefits" - {

      "and wanted to be paid weekly" - {

        "changing to say they're not receiving qualifying benefits must remove Payment Frequency, tell them they cannot be paid weekly then go to Check Payments" in {

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotClaiming),
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(ApplicantOrPartnerBenefitsPage, Benefits.qualifyingBenefits),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            goTo(CheckPaymentDetailsPage)
          )

          startingFrom(WantToBePaidPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantOrPartnerBenefitsPage),
              submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
              pageMustBe(CannotBePaidWeeklyPage),
              next,
              pageMustBe(CheckPaymentDetailsPage),
              answersMustNotContain(PaymentFrequencyPage)
            )
        }
      }

      "and wanted to be paid every four weeks" - {

        "changing to say they're not receiving qualifying benefits must remove Payment Frequency then go to Check Payments" in {


          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotClaiming),
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(ApplicantOrPartnerBenefitsPage, Benefits.qualifyingBenefits),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            goTo(CheckPaymentDetailsPage)
          )

          startingFrom(WantToBePaidPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantOrPartnerBenefitsPage),
              submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
              pageMustBe(CheckPaymentDetailsPage),
              answersMustNotContain(PaymentFrequencyPage)
            )
        }
      }
    }

    "and originally said they want to be paid" - {

      "changing to say they don't want to be paid must remove all other payment details, then go to Check Payment" in {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationship),
          setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
          setUserAnswerTo(WantToBePaidPage, true),
          setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Benefits.values.toSet),
          setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
          setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
          setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
          setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
          setUserAnswerTo(BankAccountDetailsPage, bankDetails),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidPage),
            submitAnswer(WantToBePaidPage, false),
            pageMustBe(CheckPaymentDetailsPage),
            answersMustNotContain(ApplicantOrPartnerBenefitsPage),
            answersMustNotContain(PaymentFrequencyPage),
            answersMustNotContain(WantToBePaidToExistingAccountPage),
            answersMustNotContain(ApplicantHasSuitableAccountPage),
            answersMustNotContain(BankAccountHolderPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }

    "and originally said they didn't want to be paid" - {

      "changing to say they want to be paid must collect payment details then go to Check Payments" in {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationship),
          setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotClaiming),
          setUserAnswerTo(WantToBePaidPage, false),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidPage),
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(ApplicantOrPartnerBenefitsPage, Benefits.values.toSet),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, BankAccountHolder.Applicant),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckPaymentDetailsPage)
          )
      }
    }
  }

  "when the user is single, separated, divorced or widowed" - {

    val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

    "changing their income must show the tax charge explanation" in {

      val originalIncome = Gen.oneOf(Income.values).sample.value
      val newIncome = Gen.oneOf(Income.values.toSet - originalIncome).sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationship),
        submitAnswer(ApplicantIncomePage, originalIncome),
        submitAnswer(WantToBePaidPage, true),
        goTo(CheckPaymentDetailsPage)
      )

      startingFrom(ApplicantIncomePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantIncomePage),
          submitAnswer(ApplicantIncomePage, newIncome),
          pageMustBe(WantToBePaidPage)
        )
    }

    "and originally said they want to be paid" - {

      "changing to say they don't want to be paid must remove all other payment details, then go to Check Payment" in {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationship),
          setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
          setUserAnswerTo(WantToBePaidPage, true),
          setUserAnswerTo(ApplicantBenefitsPage, Benefits.values.toSet),
          setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
          setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
          setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
          setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
          setUserAnswerTo(BankAccountDetailsPage, bankDetails),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidPage),
            submitAnswer(WantToBePaidPage, false),
            pageMustBe(CheckPaymentDetailsPage),
            answersMustNotContain(ApplicantBenefitsPage),
            answersMustNotContain(PaymentFrequencyPage),
            answersMustNotContain(WantToBePaidToExistingAccountPage),
            answersMustNotContain(ApplicantHasSuitableAccountPage),
            answersMustNotContain(BankAccountHolderPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }

    "and originally said they didn't want to be paid" - {

      "changing to say they want to be paid must collect bank details then go to Check Payment" in {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationship),
          setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotClaiming),
          setUserAnswerTo(WantToBePaidPage, false),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidPage),
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(ApplicantBenefitsPage, Benefits.values.toSet),
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, BankAccountHolder.Applicant),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckPaymentDetailsPage)
          )
      }
    }
  }

  "when the user originally said they want to be paid" - {

    "to their existing account" - {

      "changing to say they want to be paid to a different account must collect bank details then go to Check Payment" in {

        val initialise = journeyOf(
          setUserAnswerTo(WantToBePaidPage, true),
          setUserAnswerTo(WantToBePaidToExistingAccountPage, true),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidToExistingAccountPage),
            submitAnswer(WantToBePaidToExistingAccountPage, false),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, BankAccountHolder.Applicant),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckPaymentDetailsPage)
          )
      }
    }

    "not to their existing account" - {

      "changing to say they want to be paid to their existing account must remove bank details then go to Check Payment" in {

        val initialise = journeyOf(
          setUserAnswerTo(WantToBePaidPage, true),
          setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
          setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
          setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
          setUserAnswerTo(BankAccountDetailsPage, bankDetails),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidToExistingAccountPage),
            submitAnswer(WantToBePaidToExistingAccountPage, true),
            pageMustBe(CheckPaymentDetailsPage),
            answersMustNotContain(ApplicantHasSuitableAccountPage),
            answersMustNotContain(BankAccountHolderPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }

    "and has a suitable account" - {

      "changing to say they don't have a suitable account must remove bank details and go to Check Payment" in {

        val initialise = journeyOf(
          setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
          setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
          setUserAnswerTo(BankAccountDetailsPage, bankDetails),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, false),
            pageMustBe(CheckPaymentDetailsPage),
            answersMustNotContain(BankAccountHolderPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }

    "and does not have a suitable account" - {

      "changing to say the have a suitable account must collect bank details and go to Check Payment" in {

        val initialise = journeyOf(
          setUserAnswerTo(ApplicantHasSuitableAccountPage, false),
          goTo(CheckPaymentDetailsPage)
        )

        startingFrom(CheckPaymentDetailsPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, BankAccountHolder.Applicant),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckPaymentDetailsPage)
          )
      }
    }
  }
}
