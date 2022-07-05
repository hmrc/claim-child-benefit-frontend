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
import models.RelationshipStatus._
import models.{AccountHolderNames, BankAccountDetails, BankAccountType, Benefits, BuildingSocietyAccountDetails, EldestChildName, RelationshipStatus}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.income.ApplicantOrPartnerBenefitsPage
import pages.{CheckYourAnswersPage, RelationshipStatusPage}
import pages.payments._

import java.time.LocalDate

class ChangingPaymentSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val childName               = arbitrary[EldestChildName].sample.value
  private val bankDetails             = arbitrary[BankAccountDetails].sample.value
  private val buildingSocietyDetails  = arbitrary[BuildingSocietyAccountDetails].sample.value
  private val accountHolderNames      = arbitrary[AccountHolderNames].sample.value
  private val benefits: Set[Benefits] = Set(Gen.oneOf(Benefits.values).sample.value)
  private val accountHolderName       = arbitrary[String].sample.value

  "when the user initially said they had claimed Child Benefit before" - {

    "and were currently entitled" - {

      "changing to say they are not entitled must remove eldest child details and `want to be paid to existing account`, show the Tax Charge explanation and ask if they want to be paid" in {

        val initialise = journeyOf(
          submitAnswer(ClaimedChildBenefitBeforePage, true),
          submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
          submitAnswer(CurrentlyReceivingChildBenefitPage, true),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
          submitAnswer(WantToBePaidToExistingAccountPage, true),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ClaimedChildBenefitBeforePage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyEntitledToChildBenefitPage),
            submitAnswer(CurrentlyEntitledToChildBenefitPage, false),
            pageMustBe(TaxChargeExplanationPage),
            next,
            pageMustBe(WantToBePaidPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage),
            answersMustNotContain(WantToBePaidToExistingAccountPage)
          )
      }

      "and were currently receiving Child Benefit" - {

        val initialise = journeyOf(
          submitAnswer(ClaimedChildBenefitBeforePage, true),
          submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
          submitAnswer(CurrentlyReceivingChildBenefitPage, true),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
          submitAnswer(WantToBePaidToExistingAccountPage, true),
          goTo(CheckYourAnswersPage)
        )

        "changing to say they are not currently receiving Child Benefit must remove the relevant questions, show the Tax Charge explanation, then ask if the user want to be paid" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, false),
              pageMustBe(TaxChargeExplanationPage),
              next,
              pageMustBe(WantToBePaidPage),
              answersMustNotContain(EldestChildNamePage),
              answersMustNotContain(EldestChildDateOfBirthPage),
              answersMustNotContain(WantToBePaidToExistingAccountPage)
            )
        }

        "changing `claimed before` to no must remove the relevant questions, show the Tax Charge explanation, then ask if the user wants to be paid" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              initialise,
              goToChangeAnswer(ClaimedChildBenefitBeforePage),
              submitAnswer(ClaimedChildBenefitBeforePage, false),
              pageMustBe(TaxChargeExplanationPage),
              next,
              pageMustBe(WantToBePaidPage),
              answersMustNotContain(CurrentlyEntitledToChildBenefitPage),
              answersMustNotContain(CurrentlyReceivingChildBenefitPage),
              answersMustNotContain(EldestChildNamePage),
              answersMustNotContain(EldestChildDateOfBirthPage),
              answersMustNotContain(WantToBePaidToExistingAccountPage)
            )
        }
        
        "and initially said they wanted to be paid to their existing bank account" - {
          
          "changing that answer to `no` must collect bank account details" in {

            val initialise = journeyOf(
              submitAnswer(WantToBePaidToExistingAccountPage, true),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(WantToBePaidToExistingAccountPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidToExistingAccountPage),
                submitAnswer(WantToBePaidToExistingAccountPage, false),
                pageMustBe(ApplicantHasSuitableAccountPage)
              )
          }
        }
        
        "and initially said they did not want to be paid to their existing bank account" - {
          
          "changing that answer to `yes` must remove their bank account details and return to Check Answers" in {

            val initialise = journeyOf(
              submitAnswer(WantToBePaidToExistingAccountPage, false),
              submitAnswer(ApplicantHasSuitableAccountPage, true),
              submitAnswer(AccountInApplicantsNamePage, false),
              submitAnswer(AccountIsJointPage, false),
              submitAnswer(AccountHolderNamePage, accountHolderName),
              submitAnswer(BankAccountTypePage, BankAccountType.Bank),
              submitAnswer(BankAccountDetailsPage, bankDetails),
              setUserAnswerTo(AccountHolderNamesPage, accountHolderNames),
              setUserAnswerTo(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(WantToBePaidToExistingAccountPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidToExistingAccountPage),
                submitAnswer(WantToBePaidToExistingAccountPage, true),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantHasSuitableAccountPage),
                answersMustNotContain(AccountInApplicantsNamePage),
                answersMustNotContain(AccountIsJointPage),
                answersMustNotContain(AccountHolderNamePage),
                answersMustNotContain(AccountHolderNamesPage),
                answersMustNotContain(BankAccountTypePage),
                answersMustNotContain(BankAccountDetailsPage),
                answersMustNotContain(BuildingSocietyAccountDetailsPage),
              )
          }
        }
      }

      "and were not currently receiving Child Benefit" - {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, RelationshipStatus.Single),
          submitAnswer(ClaimedChildBenefitBeforePage, true),
          submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
          submitAnswer(CurrentlyReceivingChildBenefitPage, false),
          pageMustBe(TaxChargeExplanationPage),
          next,
          submitAnswer(WantToBePaidPage, true),
          submitAnswer(WantToBePaidWeeklyPage, true),
          submitAnswer(ApplicantHasSuitableAccountPage, true),
          goTo(CheckYourAnswersPage)
        )

        "changing `claimed before` to no must remove the relevant questions then go to Check Answers" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              initialise,
              goToChangeAnswer(ClaimedChildBenefitBeforePage),
              submitAnswer(ClaimedChildBenefitBeforePage, false),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(CurrentlyEntitledToChildBenefitPage),
              answersMustNotContain(CurrentlyReceivingChildBenefitPage)
            )
        }

        "changing `currently receiving` to yes must remove `want to be paid` questions, collect eldest child details then ask about payment to existing account" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, true),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              submitAnswer(WantToBePaidToExistingAccountPage, false),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(WantToBePaidPage),
              answersMustNotContain(WantToBePaidWeeklyPage)
            )
        }
      }
    }

    "and were not currently entitled" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, RelationshipStatus.Single),
        submitAnswer(ClaimedChildBenefitBeforePage, true),
        submitAnswer(CurrentlyEntitledToChildBenefitPage, false),
        pageMustBe(TaxChargeExplanationPage),
        next,
        submitAnswer(WantToBePaidPage, true),
        submitAnswer(WantToBePaidWeeklyPage, true),
        goTo(CheckYourAnswersPage)
      )

      "changing `currently entitled` to `yes`" - {

        "then answering `currently receiving` as `no` must return to Check Answers" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyEntitledToChildBenefitPage),
              submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
              submitAnswer(CurrentlyReceivingChildBenefitPage, false),
              pageMustBe(CheckYourAnswersPage)
            )
        }

        "then answering `currently receiving` as `yes` must collect eldest child details and remove `want to be paid` details" in {

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyEntitledToChildBenefitPage),
              submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
              submitAnswer(CurrentlyReceivingChildBenefitPage, true),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(WantToBePaidToExistingAccountPage),
              answersMustNotContain(WantToBePaidPage),
              answersMustNotContain(WantToBePaidWeeklyPage)
            )
        }
      }
    }
  }

  "when the user initially said they had not claimed Child Benefit before" - {

    "changing to say they had received Child Benefit before" - {

      "and that they are currently entitled" - {

        "and that they are currently receiving Child Benefit" - {

          "must remove `want to be paid` answers and collect details of the eldest child" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, Single),
              submitAnswer(ClaimedChildBenefitBeforePage, false),
              next,
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(WantToBePaidWeeklyPage, true),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(ClaimedChildBenefitBeforePage)
              .run(
                initialise,
                goToChangeAnswer(ClaimedChildBenefitBeforePage),
                submitAnswer(ClaimedChildBenefitBeforePage, true),
                submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
                submitAnswer(CurrentlyReceivingChildBenefitPage, true),
                submitAnswer(EldestChildNamePage, childName),
                submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(WantToBePaidToExistingAccountPage),
                answersMustNotContain(WantToBePaidPage),
                answersMustNotContain(WantToBePaidWeeklyPage)
              )
          }
        }

        "and that they are not currently receiving Child Benefit" - {

          "must return to Check Answers" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, Single),
              submitAnswer(ClaimedChildBenefitBeforePage, false),
              next,
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(WantToBePaidWeeklyPage, true),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(ClaimedChildBenefitBeforePage)
              .run(
                initialise,
                goToChangeAnswer(ClaimedChildBenefitBeforePage),
                submitAnswer(ClaimedChildBenefitBeforePage, true),
                submitAnswer(CurrentlyEntitledToChildBenefitPage, true),
                submitAnswer(CurrentlyReceivingChildBenefitPage, false),
                pageMustBe(CheckYourAnswersPage)
              )
          }
        }
      }

      "and that they are not currently entitled" - {

        "must return to Check Answers" in {

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, Single),
            submitAnswer(ClaimedChildBenefitBeforePage, false),
            next,
            submitAnswer(WantToBePaidPage, true),
            submitAnswer(WantToBePaidWeeklyPage, true),
            goTo(CheckYourAnswersPage)
          )

          startingFrom(ClaimedChildBenefitBeforePage)
            .run(
              initialise,
              goToChangeAnswer(ClaimedChildBenefitBeforePage),
              submitAnswer(ClaimedChildBenefitBeforePage, true),
              submitAnswer(CurrentlyEntitledToChildBenefitPage, false),
              pageMustBe(CheckYourAnswersPage)
            )
        }
      }
    }

    "and did not want to be paid" - {

      "and the user is Single, Separated, Divorced or Widowed" - {

        "changing `want to be paid` to `yes` must ask `want to be paid weekly` then proceed to account details" in {

          Seq(Single, Separated, Divorced, Widowed).foreach { status =>

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, status),
              submitAnswer(ClaimedChildBenefitBeforePage, false),
              next,
              submitAnswer(WantToBePaidPage, false),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(ClaimedChildBenefitBeforePage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidPage),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(WantToBePaidWeeklyPage, arbitrary[Boolean].sample.value),
                pageMustBe(ApplicantHasSuitableAccountPage)
              )
          }
        }
      }

      "and the user is Married or Cohabiting" - {

        "and they or their partner receive a qualifying benefit" - {

          "changing `want to be paid` to `yes` must ask `want to be paid weekly` then proceed to account details" in {

            Seq(Married, Cohabiting).foreach { status =>

              val benefits = Set(Gen.oneOf(Benefits.qualifyingBenefits).sample.value)

              val initialise = journeyOf(
                setUserAnswerTo(RelationshipStatusPage, status),
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, benefits),
                submitAnswer(ClaimedChildBenefitBeforePage, false),
                next,
                submitAnswer(WantToBePaidPage, false),
                goTo(CheckYourAnswersPage)
              )

              startingFrom(ClaimedChildBenefitBeforePage)
                .run(
                  initialise,
                  goToChangeAnswer(WantToBePaidPage),
                  submitAnswer(WantToBePaidPage, true),
                  submitAnswer(WantToBePaidWeeklyPage, arbitrary[Boolean].sample.value),
                  pageMustBe(ApplicantHasSuitableAccountPage)
                )
            }
          }
        }

        "and they or their partner do not receive a qualifying benefit" - {

          "changing `want to be paid` to `yes` must proceed to account details" in {

            Seq(Married, Cohabiting).foreach { status =>

              val benefits: Set[Benefits] = Set(Benefits.NoneOfTheAbove)

              val initialise = journeyOf(
                setUserAnswerTo(RelationshipStatusPage, status),
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, benefits),
                submitAnswer(ClaimedChildBenefitBeforePage, false),
                next,
                submitAnswer(WantToBePaidPage, false),
                goTo(CheckYourAnswersPage)
              )

              startingFrom(ClaimedChildBenefitBeforePage)
                .run(
                  initialise,
                  goToChangeAnswer(WantToBePaidPage),
                  submitAnswer(WantToBePaidPage, true),
                  pageMustBe(ApplicantHasSuitableAccountPage)
                )
            }
          }
        }
      }
    }

    "and wanted to be paid" - {

      "changing `want to be paid` to `no` must remove all of the bank details and `want to be paid weekly`" in {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, Single),
          setUserAnswerTo(ApplicantOrPartnerBenefitsPage, benefits),
          submitAnswer(ClaimedChildBenefitBeforePage, false),
          next,
          submitAnswer(WantToBePaidPage, true),
          submitAnswer(WantToBePaidWeeklyPage, arbitrary[Boolean].sample.value),
          submitAnswer(ApplicantHasSuitableAccountPage, true),
          submitAnswer(AccountInApplicantsNamePage, true),
          submitAnswer(BankAccountTypePage, BankAccountType.Bank),
          submitAnswer(BankAccountDetailsPage, bankDetails),
          setUserAnswerTo(AccountIsJointPage, true),
          setUserAnswerTo(AccountHolderNamePage, "name"),
          setUserAnswerTo(AccountHolderNamesPage, accountHolderNames),
          setUserAnswerTo(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ClaimedChildBenefitBeforePage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidPage),
            submitAnswer(WantToBePaidPage, false),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(WantToBePaidWeeklyPage),
            answersMustNotContain(ApplicantHasSuitableAccountPage),
            answersMustNotContain(AccountInApplicantsNamePage),
            answersMustNotContain(AccountIsJointPage),
            answersMustNotContain(AccountHolderNamePage),
            answersMustNotContain(AccountHolderNamesPage),
            answersMustNotContain(BankAccountTypePage),
            answersMustNotContain(BankAccountDetailsPage),
            answersMustNotContain(BuildingSocietyAccountDetailsPage),
          )
      }
    }
  }

  "when the user initially said they have a suitable account" - {

    "changing the answer to `no` must remove all of the bank details" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantHasSuitableAccountPage, true),
        submitAnswer(AccountInApplicantsNamePage, true),
        submitAnswer(BankAccountTypePage, BankAccountType.Bank),
        submitAnswer(BankAccountDetailsPage, bankDetails),
        setUserAnswerTo(AccountIsJointPage, true),
        setUserAnswerTo(AccountHolderNamePage, "name"),
        setUserAnswerTo(AccountHolderNamesPage, accountHolderNames),
        setUserAnswerTo(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantHasSuitableAccountPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantHasSuitableAccountPage),
          submitAnswer(ApplicantHasSuitableAccountPage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(AccountInApplicantsNamePage),
          answersMustNotContain(AccountIsJointPage),
          answersMustNotContain(AccountHolderNamePage),
          answersMustNotContain(AccountHolderNamesPage),
          answersMustNotContain(BankAccountTypePage),
          answersMustNotContain(BankAccountDetailsPage),
          answersMustNotContain(BuildingSocietyAccountDetailsPage),
        )
    }
  }

  "when the user initially said they did not have a suitable account" - {

    val initialise = journeyOf(
      submitAnswer(ApplicantHasSuitableAccountPage, false),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to `yes` must proceed to collect bank details" - {

      "when the user has a bank account in their name" in {

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(AccountInApplicantsNamePage, true),
            submitAnswer(BankAccountTypePage, BankAccountType.Bank),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "when the user has a building society account in their name" in {

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(AccountInApplicantsNamePage, true),
            submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
            submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "when the user gives details of a bank account in one other person's name" in {

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, false),
            submitAnswer(AccountHolderNamePage, accountHolderName),
            submitAnswer(BankAccountTypePage, BankAccountType.Bank),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "when the user gives details of a joint bank account in other people's names" in {

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, true),
            submitAnswer(AccountHolderNamesPage, accountHolderNames),
            submitAnswer(BankAccountTypePage, BankAccountType.Bank),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "when the user gives details of a building society account in one other person's name" in {

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, false),
            submitAnswer(AccountHolderNamePage, accountHolderName),
            submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
            submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "when the user gives details of a joint building society account in other people's names" in {

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, true),
            submitAnswer(AccountHolderNamesPage, accountHolderNames),
            submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
            submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }
    }
  }

  "when the user initially said the account was in their name" - {

    val initialise = journeyOf(
      submitAnswer(AccountInApplicantsNamePage, true),
      submitAnswer(BankAccountTypePage, arbitrary[BankAccountType].sample.value),
      setUserAnswerTo(BankAccountDetailsPage, bankDetails),
      setUserAnswerTo(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
      goTo(CheckYourAnswersPage)
    )

    "changing to say it's not in their name must delete the account details then collect the account holder name(s) and details" - {

      "for a bank account in one person's name" in {

        startingFrom(AccountInApplicantsNamePage)
          .run(
            initialise,
            goToChangeAnswer(AccountInApplicantsNamePage),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, false),
            submitAnswer(AccountHolderNamePage, accountHolderName),
            submitAnswer(BankAccountTypePage, BankAccountType.Bank),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(BuildingSocietyAccountDetailsPage)
          )
      }

      "for a building society account in one person's name" in {

        startingFrom(AccountInApplicantsNamePage)
          .run(
            initialise,
            goToChangeAnswer(AccountInApplicantsNamePage),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, false),
            submitAnswer(AccountHolderNamePage, accountHolderName),
            submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
            submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }

      "for a bank account in two people's name" in {

        startingFrom(AccountInApplicantsNamePage)
          .run(
            initialise,
            goToChangeAnswer(AccountInApplicantsNamePage),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, true),
            submitAnswer(AccountHolderNamesPage, accountHolderNames),
            submitAnswer(BankAccountTypePage, BankAccountType.Bank),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(BuildingSocietyAccountDetailsPage)
          )
      }

      "for a building society account in two people's names" in {

        startingFrom(AccountInApplicantsNamePage)
          .run(
            initialise,
            goToChangeAnswer(AccountInApplicantsNamePage),
            submitAnswer(AccountInApplicantsNamePage, false),
            submitAnswer(AccountIsJointPage, true),
            submitAnswer(AccountHolderNamesPage, accountHolderNames),
            submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
            submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }
  }

  "when the user initially said the account was not in their name" - {

    val initialise = journeyOf(
      submitAnswer(AccountInApplicantsNamePage, false),
      submitAnswer(AccountIsJointPage, false),
      submitAnswer(AccountHolderNamePage, accountHolderName),
      setUserAnswerTo(AccountHolderNamesPage, accountHolderNames),
      submitAnswer(BankAccountTypePage, arbitrary[BankAccountType].sample.value),
      setUserAnswerTo(BankAccountDetailsPage, bankDetails),
      setUserAnswerTo(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
      goTo(CheckYourAnswersPage)
    )

    "changing to say it is in their name must remove the name and account details information, then collect new account information" - {

      "for a bank account" in {

        startingFrom(AccountInApplicantsNamePage)
          .run(
            initialise,
            goToChangeAnswer(AccountInApplicantsNamePage),
            submitAnswer(AccountInApplicantsNamePage, true),
            submitAnswer(BankAccountTypePage, BankAccountType.Bank),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(AccountIsJointPage),
            answersMustNotContain(AccountHolderNamePage),
            answersMustNotContain(AccountHolderNamesPage),
            answersMustNotContain(BuildingSocietyAccountDetailsPage)
          )
      }

      "for a building society account" in {

        startingFrom(AccountInApplicantsNamePage)
          .run(
            initialise,
            goToChangeAnswer(AccountInApplicantsNamePage),
            submitAnswer(AccountInApplicantsNamePage, true),
            submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
            submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(AccountIsJointPage),
            answersMustNotContain(AccountHolderNamePage),
            answersMustNotContain(AccountHolderNamesPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }
  }

  "when the user initially said the account was in joint names" - {

    "changing to say it's in one person's name should remove the joint names, collect the single name, then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(AccountIsJointPage, true),
        submitAnswer(AccountHolderNamesPage, accountHolderNames),
        submitAnswer(BankAccountTypePage, arbitrary[BankAccountType].sample.value),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(AccountIsJointPage)
        .run(
          initialise,
          goToChangeAnswer(AccountIsJointPage),
          submitAnswer(AccountIsJointPage, false),
          submitAnswer(AccountHolderNamePage, accountHolderName),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(AccountHolderNamesPage)
        )
    }
  }

  "when the user initially said the account was not in joint names" - {

    "changing to say joint should remove the single name, collect the joint names, then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(AccountIsJointPage, false),
        submitAnswer(AccountHolderNamePage, accountHolderName),
        submitAnswer(BankAccountTypePage, arbitrary[BankAccountType].sample.value),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(AccountIsJointPage)
        .run(
          initialise,
          goToChangeAnswer(AccountIsJointPage),
          submitAnswer(AccountIsJointPage, true),
          submitAnswer(AccountHolderNamesPage, accountHolderNames),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(AccountHolderNamePage)
        )
    }
  }

  "when the user initially gave bank details" - {

    "changing the account type to `Building Society` should remove the bank details, collect Building Society details then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(BankAccountTypePage, BankAccountType.Bank),
        submitAnswer(BankAccountDetailsPage, bankDetails),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(BankAccountTypePage)
        .run(
          initialise,
          goToChangeAnswer(BankAccountTypePage),
          submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
          submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(BankAccountDetailsPage)
        )
    }
  }

  "when the user initially gave building society details" - {

    "changing the account type to `Bank` should remove the building society details, collect Bank details then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(BankAccountTypePage, BankAccountType.BuildingSociety),
        submitAnswer(BuildingSocietyAccountDetailsPage, buildingSocietyDetails),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(BankAccountTypePage)
        .run(
          initialise,
          goToChangeAnswer(BankAccountTypePage),
          submitAnswer(BankAccountTypePage, BankAccountType.Bank),
          submitAnswer(BankAccountDetailsPage, bankDetails),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(BuildingSocietyAccountDetailsPage)
        )
    }
  }
}
