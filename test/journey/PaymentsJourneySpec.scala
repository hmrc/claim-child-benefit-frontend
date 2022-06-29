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

import models.RelationshipStatus._
import models.{AccountHolderNames, BankAccountDetails, BankAccountType, Benefits, BuildingSocietyAccountDetails, EldestChildName, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import pages.RelationshipStatusPage
import pages.applicant.ApplicantHasPreviousFamilyNamePage
import pages.income.ApplicantOrPartnerBenefitsPage
import pages.payments._

import java.time.LocalDate

class PaymentsJourneySpec extends AnyFreeSpec with JourneyHelpers {

  "users who have not claimed Child Benefit before" - {

    "must be shown the tax charge explanation and asked if they want to be paid Child Benefit" in {

      startingFrom(ClaimedChildBenefitBeforePage)
        .run(
          submitAnswer(ClaimedChildBenefitBeforePage, false),
          next,
          pageMustBe(WantToBePaidPage)
        )
    }
  }

  "users who have claimed Child Benefit before" - {

    "who are not currently entitled" - {

      "must be shown the tax charge explanation and asked if they want to be paid Child Benefit" in {

        startingFrom(ClaimedChildBenefitBeforePage)
          .run(
            submitAnswer(ClaimedChildBenefitBeforePage, true),
            submitAnswer(CurrentlyEntitledToChildBenefitPage, false),
            next,
            pageMustBe(WantToBePaidPage)
          )
      }
    }

    "who are currently entitled but not currently receiving Child Benefit" - {

      "must be shown the tax charge explanation and asked if they want to be paid Child Benefit" in {

        startingFrom(ClaimedChildBenefitBeforePage)
          .run(
            submitAnswer(ClaimedChildBenefitBeforePage, true),
            submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
            submitAnswer(CurrentlyReceivingChildBenefitPage, false),
            next,
            pageMustBe(WantToBePaidPage)
          )
      }
    }

    "who are currently receiving Child Benefit" - {

      val childName = EldestChildName("first", None, "last")
      val childDob  = LocalDate.now

      "who want to be paid to their existing bank account" - {

        "must proceed to the Applicant section" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              submitAnswer(ClaimedChildBenefitBeforePage, true),
              submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
              submitAnswer(CurrentlyReceivingChildBenefitPage, true),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidToExistingAccountPage, true)
            )
        }
      }

      "who do not want to be paid to their existing bank account" -{

        "must be asked for bank account details" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              submitAnswer(ClaimedChildBenefitBeforePage, true),
              submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
              submitAnswer(CurrentlyReceivingChildBenefitPage, true),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, childDob),
              submitAnswer(WantToBePaidToExistingAccountPage, false)
            )
        }
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
            submitAnswer(WantToBePaidPage, true)
          )
      }

      "and receive qualifying benefits must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Married).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.qualifyingBenefits.head)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(WantToBePaidWeeklyPage, true)
          )
      }
    }

    "who are Cohabiting" - {

      "and not receiving qualifying benefits  must be asked for bank account details" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Cohabiting).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true)
          )
      }


      "and receive qualifying benefits must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Cohabiting).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.qualifyingBenefits.head)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .run(
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(WantToBePaidWeeklyPage, true)
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
            submitAnswer(WantToBePaidWeeklyPage, true)
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
            submitAnswer(WantToBePaidWeeklyPage, true)
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
            submitAnswer(WantToBePaidWeeklyPage, true)
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
            submitAnswer(WantToBePaidWeeklyPage, true)
          )
      }
    }
  }

  "users not wanting to be paid Child Benefit" - {

    "must proceed to the Applicant section" in {

      startingFrom(WantToBePaidPage)
        .run(
          submitAnswer(WantToBePaidPage, false)
        )
    }
  }

  "users who do not have a suitable account" - {

    "must proceed to the Applicant section" in {

      startingFrom(ApplicantHasSuitableAccountPage)
        .run(
          submitAnswer(ApplicantHasSuitableAccountPage, false)
        )
    }
  }

  "users who have a suitable account" - {

    "in their name" - {

      "must be asked if it is a bank or building society account" in {

        startingFrom(AccountInApplicantsNamePage)
          .run(
            submitAnswer(AccountInApplicantsNamePage, true)
          )
      }
    }

    "not in their name" - {

      "when the account is in one name" - {

        "must be asked for the account holder name, then the type of account" in {

          startingFrom(AccountInApplicantsNamePage)
            .run(
              submitAnswer(AccountInApplicantsNamePage, false),
              submitAnswer(AccountIsJointPage, false),
              submitAnswer(AccountHolderNamePage, "name")
            )
        }
      }

      "when the account is joint" - {

        "must be asked for the account holder names, then the type of account" in {

          val accountHolderNames = AccountHolderNames("name 1", "name 2")

          startingFrom(AccountInApplicantsNamePage)
            .run(
              submitAnswer(AccountInApplicantsNamePage, false),
              submitAnswer(AccountIsJointPage, true),
              submitAnswer(AccountHolderNamesPage, accountHolderNames)
            )
        }
      }
    }
  }

  "users choosing to give bank details must be asked those details then proceed to the Applicant section" in {

    val bankDetails = BankAccountDetails("bank name", "12345678", "123456")

    startingFrom(BankAccountTypePage)
      .run(
        submitAnswer(BankAccountTypePage, BankAccountType.Bank),
        submitAnswer(BankAccountDetailsPage, bankDetails)
      )
  }

  "users choosing to give building society details must be asked those details then proceed to the Applicant section" in {

    val buildingSocietyDetails = BuildingSocietyAccountDetails("building society name", "12345678", "123456", None)

    startingFrom(BankAccountTypePage)
      .run(
        submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
        submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails)
      )
  }
}