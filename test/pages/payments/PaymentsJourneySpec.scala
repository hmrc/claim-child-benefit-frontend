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

package pages.payments

import models.RelationshipStatus._
import models.{AccountHolderNames, BankAccountDetails, BankAccountType, Benefits, BuildingSocietyAccountDetails, EldestChildName, UserAnswers}
import pages.{JourneyHelpers, RelationshipStatusPage}
import pages.JourneyState.startingFrom
import pages.applicant.ApplicantHasPreviousFamilyNamePage
import pages.income.ApplicantOrPartnerBenefitsPage

import java.time.LocalDate

class PaymentsJourneySpec extends JourneyHelpers {

  "users who have not claimed Child Benefit before" - {

    "must be shown the tax charge explanation and asked if they want to be paid Child Benefit" in {

      startingFrom(ClaimedChildBenefitBeforePage)
        .steps(
          answerPage(ClaimedChildBenefitBeforePage, false, TaxChargeExplanationPage),
          next,
          pageMustBe(WantToBePaidPage)
        )
    }
  }

  "users who have claimed Child Benefit before" - {

    "who are not currently entitled" - {

      "must be shown the tax charge explanation and asked if they want to be paid Child Benefit" in {

        startingFrom(ClaimedChildBenefitBeforePage)
          .steps(
            answerPage(ClaimedChildBenefitBeforePage, true, CurrentlyEntitledToChildBenefitPage),
            answerPage(CurrentlyEntitledToChildBenefitPage, false, TaxChargeExplanationPage),
            next,
            pageMustBe(WantToBePaidPage)
          )
      }
    }

    "who are currently entitled but not currently receiving Child Benefit" - {

      "must be shown the tax charge explanation and asked if they want to be paid Child Benefit" in {

        startingFrom(ClaimedChildBenefitBeforePage)
          .steps(
            answerPage(ClaimedChildBenefitBeforePage, true, CurrentlyEntitledToChildBenefitPage),
            answerPage(CurrentlyEntitledToChildBenefitPage, true, CurrentlyReceivingChildBenefitPage),
            answerPage(CurrentlyReceivingChildBenefitPage, false, TaxChargeExplanationPage),
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
            .steps(
              answerPage(ClaimedChildBenefitBeforePage, true, CurrentlyEntitledToChildBenefitPage),
              answerPage(CurrentlyEntitledToChildBenefitPage, true, CurrentlyReceivingChildBenefitPage),
              answerPage(CurrentlyReceivingChildBenefitPage, true, EldestChildNamePage),
              answerPage(EldestChildNamePage, childName, EldestChildDateOfBirthPage),
              answerPage(EldestChildDateOfBirthPage, childDob, WantToBePaidToExistingAccountPage),
              answerPage(WantToBePaidToExistingAccountPage, true, ApplicantHasPreviousFamilyNamePage)
            )
        }
      }

      "who do not want to be paid to their existing bank account" -{

        "must be asked for bank account details" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .steps(
              answerPage(ClaimedChildBenefitBeforePage, true, CurrentlyEntitledToChildBenefitPage),
              answerPage(CurrentlyEntitledToChildBenefitPage, true, CurrentlyReceivingChildBenefitPage),
              answerPage(CurrentlyReceivingChildBenefitPage, true, EldestChildNamePage),
              answerPage(EldestChildNamePage, childName, EldestChildDateOfBirthPage),
              answerPage(EldestChildDateOfBirthPage, childDob, WantToBePaidToExistingAccountPage),
              answerPage(WantToBePaidToExistingAccountPage, false, ApplicantHasSuitableAccountPage)
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
          .steps(
            answerPage(WantToBePaidPage, true, ApplicantHasSuitableAccountPage)
          )
      }

      "and receive qualifying benefits must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Married).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.qualifyingBenefits.head)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .steps(
            answerPage(WantToBePaidPage, true, WantToBePaidWeeklyPage),
            answerPage(WantToBePaidWeeklyPage, true, ApplicantHasSuitableAccountPage)
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
          .steps(
            answerPage(WantToBePaidPage, true, ApplicantHasSuitableAccountPage)
          )
      }


      "and receive qualifying benefits must be asked if they want to be paid weekly" in {

        val initialAnswers =
          UserAnswers("id")
            .set(RelationshipStatusPage, Cohabiting).success.value
            .set(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.qualifyingBenefits.head)).success.value

        startingFrom(WantToBePaidPage, answers = initialAnswers)
          .steps(
            answerPage(WantToBePaidPage, true, WantToBePaidWeeklyPage),
            answerPage(WantToBePaidWeeklyPage, true, ApplicantHasSuitableAccountPage)
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
          .steps(
            answerPage(WantToBePaidPage, true, WantToBePaidWeeklyPage),
            answerPage(WantToBePaidWeeklyPage, true, ApplicantHasSuitableAccountPage)
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
          .steps(
            answerPage(WantToBePaidPage, true, WantToBePaidWeeklyPage),
            answerPage(WantToBePaidWeeklyPage, true, ApplicantHasSuitableAccountPage)
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
          .steps(
            answerPage(WantToBePaidPage, true, WantToBePaidWeeklyPage),
            answerPage(WantToBePaidWeeklyPage, true, ApplicantHasSuitableAccountPage)
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
          .steps(
            answerPage(WantToBePaidPage, true, WantToBePaidWeeklyPage),
            answerPage(WantToBePaidWeeklyPage, true, ApplicantHasSuitableAccountPage)
          )
      }
    }
  }

  "users not wanting to be paid Child Benefit" - {

    "must proceed to the Applicant section" in {

      startingFrom(WantToBePaidPage)
        .steps(
          answerPage(WantToBePaidPage, false, ApplicantHasPreviousFamilyNamePage)
        )
    }
  }

  "users who do not have a suitable account" - {

    "must proceed to the Applicant section" in {

      startingFrom(ApplicantHasSuitableAccountPage)
        .steps(
          answerPage(ApplicantHasSuitableAccountPage, false, ApplicantHasPreviousFamilyNamePage)
        )
    }
  }

  "users who have a suitable account" - {

    "in their name" - {

      "must be asked if it is a bank or building society account" in {

        startingFrom(AccountInApplicantsNamePage)
          .steps(
            answerPage(AccountInApplicantsNamePage, true, BankAccountTypePage)
          )
      }
    }

    "not in their name" - {

      "when the account is in one name" - {

        "must be asked for the account holder name, then the type of account" in {

          startingFrom(AccountInApplicantsNamePage)
            .steps(
              answerPage(AccountInApplicantsNamePage, false, AccountIsJointPage),
              answerPage(AccountIsJointPage, false, AccountHolderNamePage),
              answerPage(AccountHolderNamePage, "name", BankAccountTypePage)
            )
        }
      }

      "when the account is joint" - {

        "must be asked for the account holder names, then the type of account" in {

          val accountHolderNames = AccountHolderNames("name 1", "name 2")

          startingFrom(AccountInApplicantsNamePage)
            .steps(
              answerPage(AccountInApplicantsNamePage, false, AccountIsJointPage),
              answerPage(AccountIsJointPage, true, AccountHolderNamesPage),
              answerPage(AccountHolderNamesPage, accountHolderNames, BankAccountTypePage)
            )
        }
      }
    }
  }

  "users choosing to give bank details must be asked those details then proceed to the Applicant section" in {

    val bankDetails = BankAccountDetails("bank name", "12345678", "123456")

    startingFrom(BankAccountTypePage)
      .steps(
        answerPage(BankAccountTypePage, BankAccountType.Bank, BankAccountDetailsPage),
        answerPage(BankAccountDetailsPage, bankDetails, ApplicantHasPreviousFamilyNamePage)
      )
  }

  "users choosing to give building society details must be asked those details then proceed to the Applicant section" in {

    val buildingSocietyDetails = BuildingSocietyAccountDetails("building society name", "12345678", "123456", None)

    startingFrom(BankAccountTypePage)
      .steps(
        answerPage(BankAccountTypePage, BankAccountType.BuildingSociety, BuildingSocietyAccountDetailsPage),
        answerPage(BuildingSocietyAccountDetailsPage, buildingSocietyDetails, ApplicantHasPreviousFamilyNamePage)
      )
  }
}