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
  private val childName = ChildName("first", None, "last")
  private val childDob  = LocalDate.now

  private val setupUserNotEligibleForWeeklyPayments = journeyOf(
    setUserAnswerTo(RelationshipStatusPage, Married),
    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove))
  )

  "users who are currently getting Child Benefit payments" - {

    "who are Married or Cohabiting and not getting qualifying benefits" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Gen.oneOf(Married, Cohabiting).sample.value),
        setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove))
      )

      "who want to be paid to their existing account" - {

        "must proceed to the Applicant section" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(WantToBePaidToExistingAccountPage, true),
              pageMustBe(ApplicantHasPreviousFamilyNamePage)
            )
        }
      }

      "who want to be paid to a different account" - {

        "must proceed to collect bank account details" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(WantToBePaidToExistingAccountPage, false),
              pageMustBe(ApplicantHasSuitableAccountPage)
            )
        }
      }

      "who do not want to be paid" - {

        "must proceed to the Applicant section" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, false),
              pageMustBe(ApplicantHasPreviousFamilyNamePage)
            )
        }
      }
    }

    "who are Married or Cohabiting and getting qualifying benefits" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Gen.oneOf(Married, Cohabiting).sample.value),
        setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Benefits.qualifyingBenefits)
      )

      "who want to be paid to their existing account" - {

        "must proceed to the Applicant section" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
              submitAnswer(WantToBePaidToExistingAccountPage, true),
              pageMustBe(ApplicantHasPreviousFamilyNamePage)
            )
        }
      }

      "who want to be paid to a different account" - {

        "must proceed to collect bank account details" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
              submitAnswer(WantToBePaidToExistingAccountPage, false),
              pageMustBe(ApplicantHasSuitableAccountPage)
            )
        }
      }

      "who do not want to be paid" - {

        "must proceed to the Applicant section" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, false),
              pageMustBe(ApplicantHasPreviousFamilyNamePage)
            )
        }
      }
    }

    "who are Single, Separated, Widowed or Divorced" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value)
      )

      "who want to be paid to their existing account" - {

        "must proceed to the Applicant section" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
              submitAnswer(WantToBePaidToExistingAccountPage, true),
              pageMustBe(ApplicantHasPreviousFamilyNamePage)
            )
        }
      }

      "who want to be paid to a different account" - {

        "must proceed to collect bank account details" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
              submitAnswer(WantToBePaidToExistingAccountPage, false),
              pageMustBe(ApplicantHasSuitableAccountPage)
            )
        }
      }

      "who do not want to be paid" - {

        "must proceed to the Applicant section" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidPage, false),
              pageMustBe(ApplicantHasPreviousFamilyNamePage)
            )
        }
      }
    }
  }

  "users who are currently claiming Child Benefit but not getting payments" - {

    "must be asked if they want to be paid" in {

      startingFrom(CurrentlyReceivingChildBenefitPage)
        .run(
          setupUserNotEligibleForWeeklyPayments,
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, childDob),
          pageMustBe(WantToBePaidPage)
        )
    }
  }

  "users who are not claiming Child Benefit" - {

    "must be asked if the want to be paid" in {

      startingFrom(CurrentlyReceivingChildBenefitPage)
        .run(
          setupUserNotEligibleForWeeklyPayments,
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
          pageMustBe(WantToBePaidPage)
        )
    }
  }

  "users not receiving Child Benefit and wanting to be paid" - {

    "who are Married" - {

      "and not receiving qualifying benefits must be asked for bank account details" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Married).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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
            .set(CurrentlyReceivingChildBenefitPage, NotClaiming).success.value

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