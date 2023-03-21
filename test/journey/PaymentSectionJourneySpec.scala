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
import models.CurrentlyReceivingChildBenefit.{GettingPayments, NotClaiming, NotGettingPayments}
import models.RelationshipStatus._
import models.{AccountType, BankAccountDetails, BankAccountHolder, Benefits, BuildingSocietyDetails, Income, PaymentFrequency}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.TaskListPage
import pages.applicant.CurrentlyReceivingChildBenefitPage
import pages.partner.RelationshipStatusPage
import pages.payments._

class PaymentSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users who are Married or Cohabiting" - {

    val relationship = Gen.oneOf(Married, Cohabiting).sample.value
    val income = Gen.oneOf(Income.values).sample.value

    "who want to be paid" - {

      "who are getting paid Child Benefit now" - {

        "must be asked for their income and if they want to be paid, then go to Check Details" in {

          startingFrom(ApplicantOrPartnerIncomePage)
            .run(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(ApplicantOrPartnerIncomePage, income),
              submitAnswer(WantToBePaidPage, true),
              pageMustBe(CheckPaymentDetailsPage)
            )
        }
      }

      "who are not getting paid Child Benefit now" - {

        val currentlyReceiving = Gen.oneOf(NotClaiming, NotGettingPayments).sample.value
        val benefits = Set(Gen.oneOf(Benefits.qualifyingBenefits).sample.value)

        "must be asked for their income and benefits details" in {

          startingFrom(ApplicantOrPartnerIncomePage)
            .run(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
              submitAnswer(ApplicantOrPartnerIncomePage, income),
              submitAnswer(WantToBePaidPage, true),
              pageMustBe(ApplicantOrPartnerBenefitsPage)
            )
        }

        "who receive qualifying benefits" - {

          "must be asked how often they want to be paid, then be asked if they have a suitable bank account" in {

            startingFrom(ApplicantOrPartnerIncomePage)
              .run(
                setUserAnswerTo(RelationshipStatusPage, relationship),
                setUserAnswerTo(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
                submitAnswer(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                submitAnswer(ApplicantHasSuitableAccountPage, false),
                pageMustBe(CheckPaymentDetailsPage),
                next,
                pageMustBe(TaskListPage)
              )
          }
        }

        "who do not receive qualifying benefits" - {

          "must not be asked how often they want to be paid, and be asked if they have a suitable bank account" in {

            startingFrom(ApplicantOrPartnerIncomePage)
              .run(
                setUserAnswerTo(RelationshipStatusPage, relationship),
                setUserAnswerTo(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                pageMustBe(ApplicantHasSuitableAccountPage)
              )
          }
        }
      }
    }

    "who do not want to be paid" - {

      "must be asked for their income then go to Check Payment Details" in {

        startingFrom(ApplicantOrPartnerIncomePage)
          .run(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(ApplicantOrPartnerIncomePage, income),
            submitAnswer(WantToBePaidPage, false),
            pageMustBe(CheckPaymentDetailsPage)
          )
      }
    }
  }

  "users who are Single, Separated, Divorced or Widowed" - {

    val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value
    val income = Gen.oneOf(Income.values).sample.value

    "who want to be paid" - {

      "who are getting paid Child Benefit now" - {

        "must be asked for their income and if they want to be paid, then go to Check Details" in {

          startingFrom(ApplicantIncomePage)
            .run(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(ApplicantIncomePage, income),
              submitAnswer(WantToBePaidPage, true),
              pageMustBe(CheckPaymentDetailsPage)
            )
        }
      }

      "who are not getting paid Child Benefit now" - {

        val currentlyReceiving = Gen.oneOf(NotClaiming, NotGettingPayments).sample.value

        "must be asked for their income and benefits details" in {

          startingFrom(ApplicantIncomePage)
            .run(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
              submitAnswer(ApplicantIncomePage, income),
              submitAnswer(WantToBePaidPage, true),
              pageMustBe(ApplicantBenefitsPage)
            )
        }

        "must be asked how often they want to be paid, then be asked if they have a suitable bank account" in {

          val benefits = Set(Gen.oneOf(Benefits.qualifyingBenefits).sample.value)

          startingFrom(ApplicantIncomePage)
            .run(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
              submitAnswer(ApplicantIncomePage, income),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(ApplicantBenefitsPage, benefits),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
              pageMustBe(ApplicantHasSuitableAccountPage)
            )
        }
      }
    }

    "who do not want to be paid" - {

      "must be asked for their income then go to Check Payment Details" in {

        startingFrom(ApplicantIncomePage)
          .run(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(ApplicantIncomePage, income),
            submitAnswer(WantToBePaidPage, false),
            pageMustBe(CheckPaymentDetailsPage)
          )
      }
    }
  }

  "users who have a suitable account who know their sort code and account number must be asked for the details then go to Check Payment" in {

    val accountHolder = Gen.oneOf(BankAccountHolder.values).sample.value
    val bankDetails = arbitrary[BankAccountDetails].sample.value

    startingFrom(ApplicantHasSuitableAccountPage)
      .run(
        submitAnswer(ApplicantHasSuitableAccountPage, true),
        submitAnswer(BankAccountHolderPage, accountHolder),
        submitAnswer(AccountTypePage, AccountType.SortCodeAccountNumber),
        submitAnswer(BankAccountDetailsPage, bankDetails),
        pageMustBe(CheckPaymentDetailsPage)
      )
  }

  "users who have a suitable account who know their roll number must be asked for the details then go to Check Payment" in {

    val accountHolder = Gen.oneOf(BankAccountHolder.values).sample.value
    val buildingSocietyDetails = arbitrary[BuildingSocietyDetails].sample.value

    startingFrom(ApplicantHasSuitableAccountPage)
      .run(
        submitAnswer(ApplicantHasSuitableAccountPage, true),
        submitAnswer(BankAccountHolderPage, accountHolder),
        submitAnswer(AccountTypePage, AccountType.BuildingSocietyRollNumber),
        submitAnswer(BuildingSocietyDetailsPage, buildingSocietyDetails),
        pageMustBe(CheckPaymentDetailsPage)
      )
  }

  "users who do not have a suitable account must go to Check Payment" in {

    startingFrom(ApplicantHasSuitableAccountPage)
      .run(
        submitAnswer(ApplicantHasSuitableAccountPage, false),
        pageMustBe(CheckPaymentDetailsPage)
      )
  }
}
