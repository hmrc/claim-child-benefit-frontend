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
import models.CurrentlyReceivingChildBenefit._
import models.RelationshipStatus._
import models.{AdultName, BankAccountDetails, BankAccountHolder, Benefits, ChildName, PaymentFrequency}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.applicant.ApplicantHasPreviousFamilyNamePage
import pages.income.ApplicantOrPartnerBenefitsPage
import pages.partner.PartnerNamePage
import pages.payments._
import pages.{CheckYourAnswersPage, RelationshipStatusPage}

import java.time.LocalDate

class ChangingPaymentSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val childName               = arbitrary[ChildName].sample.value
  private val adultName               = arbitrary[AdultName].sample.value
  private val bankAccountHolder       = arbitrary[BankAccountHolder].sample.value
  private val bankDetails             = arbitrary[BankAccountDetails].sample.value
  private val benefits: Set[Benefits] = Set(Gen.oneOf(Benefits.qualifyingBenefits).sample.value)

  "when the user initially said they were getting Child Benefit payments" - {

    "and wanted to be paid to their existing account" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Married),
        setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
        submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
        submitAnswer(EldestChildNamePage, childName),
        submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
        submitAnswer(WantToBePaidPage, true),
        submitAnswer(WantToBePaidToExistingAccountPage, true),
        goTo(CheckYourAnswersPage)
      )

      "changing to say they are claiming Child Benefit but not getting payments must collect bank account details" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            pageMustBe(ApplicantHasSuitableAccountPage),
            answersMustNotContain(WantToBePaidToExistingAccountPage)
          )
      }

      "changing to say they are not claiming Child Benefit must remove oldest child details and collect bank account details" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            pageMustBe(ApplicantHasSuitableAccountPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage),
            answersMustNotContain(WantToBePaidToExistingAccountPage)
          )
      }
    }

    "and wanted to be paid to a different account" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Married),
        setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
        submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
        submitAnswer(EldestChildNamePage, childName),
        submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
        submitAnswer(WantToBePaidPage, true),
        submitAnswer(WantToBePaidToExistingAccountPage, false),
        submitAnswer(ApplicantHasSuitableAccountPage, true),
        submitAnswer(BankAccountHolderPage, bankAccountHolder),
        submitAnswer(BankAccountDetailsPage, bankDetails),
        goTo(CheckYourAnswersPage)
      )

      "changing to say they are claiming Child Benefit but not getting payments must return to Check Answers" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(WantToBePaidToExistingAccountPage)
          )
      }

      "changing to say they are not claiming Child Benefit must remove oldest child details and return to Check Answers" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage),
            answersMustNotContain(WantToBePaidToExistingAccountPage)
          )
      }
    }

    "and did not want to be paid" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Single),
        submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
        submitAnswer(EldestChildNamePage, childName),
        submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
        submitAnswer(WantToBePaidPage, false),
        goTo(CheckYourAnswersPage)
      )

      "changing to say they are claiming Child Benefit but not getting payments must return the user to Check Answers" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "changing to say they are not claiming Child Benefit must remove oldest child details then return to Check Answers" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage)
          )
      }
    }
  }

  "when the user initially said they were claiming Child Benefit but not getting payments" - {

    "and wanted to be paid" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Married),
        setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
        submitAnswer(EldestChildNamePage, childName),
        submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
        submitAnswer(WantToBePaidPage, true),
        submitAnswer(ApplicantHasSuitableAccountPage, true),
        submitAnswer(BankAccountHolderPage, bankAccountHolder),
        submitAnswer(BankAccountDetailsPage, bankDetails),
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        goTo(CheckYourAnswersPage)
      )

      "changing to say they are getting Child Benefit payments" - {

        "and that they want to be paid to their existing account" - {

          "must remove bank details and return to Check Answers" in {

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
                submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
                submitAnswer(WantToBePaidToExistingAccountPage, true),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantHasSuitableAccountPage),
                answersMustNotContain(BankAccountHolderPage),
                answersMustNotContain(BankAccountDetailsPage)
              )
          }
        }

        "and that they do not want to be paid to their existing account" - {

          "must return to Check Answers" in {

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
                submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
                submitAnswer(WantToBePaidToExistingAccountPage, false),
                pageMustBe(CheckYourAnswersPage),
                answersMustContain(ApplicantHasSuitableAccountPage),
                answersMustContain(BankAccountHolderPage),
                answersMustContain(BankAccountDetailsPage)
              )
          }
        }
      }

      "changing to say they are not claiming Child Benefit must remove oldest child details then return to Check Answers" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantHasSuitableAccountPage),
            answersMustContain(BankAccountHolderPage),
            answersMustContain(BankAccountDetailsPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage)
          )
      }
    }

    "and did not want to be paid" - {

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, Single),
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
          submitAnswer(WantToBePaidPage, false),
          goTo(CheckYourAnswersPage)
        )

      "changing to say they are getting Child Benefit payments must return the user to Check Answers" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "changing to say they are not claiming Child Benefit must remove oldest child details then return to Check Answers" in {

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage)
          )
      }
    }
  }

  "when the user initially said they were not claiming Child Benefit" - {

    "and wanted to be paid" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Married),
        setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
        submitAnswer(WantToBePaidPage, true),
        submitAnswer(ApplicantHasSuitableAccountPage, true),
        submitAnswer(BankAccountHolderPage, bankAccountHolder),
        submitAnswer(BankAccountDetailsPage, bankDetails),
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        goTo(CheckYourAnswersPage)
      )

      "changing to say they are getting Child Benefit payments" - {

        "and that they want to be paid to their existing account" - {

          "must collect oldest child details, remove bank details and return to Check Answers" in {

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
                submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
                submitAnswer(EldestChildNamePage, childName),
                submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
                submitAnswer(WantToBePaidToExistingAccountPage, true),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantHasSuitableAccountPage),
                answersMustNotContain(BankAccountHolderPage),
                answersMustNotContain(BankAccountDetailsPage)
              )
          }
        }

        "and that they do not want to be paid to their existing account" - {

          "must collect oldest child details and return to Check Answers" in {

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
                submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
                submitAnswer(EldestChildNamePage, childName),
                submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
                submitAnswer(WantToBePaidToExistingAccountPage, false),
                pageMustBe(CheckYourAnswersPage),
                answersMustContain(ApplicantHasSuitableAccountPage),
                answersMustContain(BankAccountHolderPage),
                answersMustContain(BankAccountDetailsPage)
              )
          }
        }
      }

      "changing to say they are claiming Child Benefit but not getting payments" - {

        "must collect oldest child details and return to Check Answers" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckYourAnswersPage),
              answersMustContain(ApplicantHasSuitableAccountPage),
              answersMustContain(BankAccountHolderPage),
              answersMustContain(BankAccountDetailsPage)
            )
        }
      }
    }

    "and did not want to be paid" - {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Married),
        setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
        submitAnswer(WantToBePaidPage, false),
        goTo(CheckYourAnswersPage)
      )

      "changing to say they are getting Child Benefit payments" - {

        "must collect oldest child details then return to Check Answers" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckYourAnswersPage)
            )
        }
      }

      "changing to say they are claiming Child Benefit but not getting payments" - {

        "must collect oldest child details then return to Check Answers" in {

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckYourAnswersPage)
            )
        }
      }
    }
  }

  "when the user initially said they want to be paid" - {

    "changing that answer to `no` must remove all bank details" in {

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, Married),
        setUserAnswerTo(WantToBePaidPage, true),
        setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
        setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
        setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
        setUserAnswerTo(BankAccountHolderPage, bankAccountHolder),
        setUserAnswerTo(BankAccountDetailsPage, bankDetails),
        setUserAnswerTo(ApplicantHasPreviousFamilyNamePage, false),
        setUserAnswerTo(PartnerNamePage, adultName),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(CheckYourAnswersPage)
        .run(
          initialise,
          goToChangeAnswer(WantToBePaidPage),
          submitAnswer(WantToBePaidPage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(WantToBePaidToExistingAccountPage),
          answersMustNotContain(ApplicantHasSuitableAccountPage),
          answersMustNotContain(BankAccountHolderPage),
          answersMustNotContain(BankAccountDetailsPage)
        )
    }

    "to their existing account" - {

      "changing that answer to `no` must collect bank details then go to Check Answers" in {

        val initialise = journeyOf(
          setUserAnswerTo(WantToBePaidPage, true),
          setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
          setUserAnswerTo(WantToBePaidToExistingAccountPage, true),
          setUserAnswerTo(ApplicantHasPreviousFamilyNamePage, false),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(CheckYourAnswersPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidToExistingAccountPage),
            submitAnswer(WantToBePaidToExistingAccountPage, false),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, bankAccountHolder),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }
    }

    "not to their existing account" - {

      "changing that answer to `yes` must remove bank details and go to Check Answers" in {
        val initialise = journeyOf(
          setUserAnswerTo(WantToBePaidPage, true),
          setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
          setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
          setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
          setUserAnswerTo(BankAccountHolderPage, bankAccountHolder),
          setUserAnswerTo(BankAccountDetailsPage, bankDetails),
          setUserAnswerTo(ApplicantHasPreviousFamilyNamePage, false),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(CheckYourAnswersPage)
          .run(
            initialise,
            goToChangeAnswer(WantToBePaidToExistingAccountPage),
            submitAnswer(WantToBePaidToExistingAccountPage, true),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(ApplicantHasSuitableAccountPage),
            answersMustNotContain(BankAccountHolderPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }

    "and had a suitable account" - {

      "changing that answer to `no` must remove bank details and go to Check Answers" in {

        val initialise = journeyOf(
          submitAnswer(ApplicantHasSuitableAccountPage, true),
          submitAnswer(BankAccountHolderPage, bankAccountHolder),
          submitAnswer(BankAccountDetailsPage, bankDetails),
          setUserAnswerTo(ApplicantHasPreviousFamilyNamePage, false),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, false),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(BankAccountHolderPage),
            answersMustNotContain(BankAccountDetailsPage)
          )
      }
    }

    "and did not have a suitable account " - {

      val initialise = journeyOf(
        submitAnswer(ApplicantHasSuitableAccountPage, false),
        setUserAnswerTo(ApplicantHasPreviousFamilyNamePage, false),
        goTo(CheckYourAnswersPage)
      )

      "changing that answer to 'yes' must collect bank details then go to Check Answers" in {

        startingFrom(ApplicantHasSuitableAccountPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantHasSuitableAccountPage),
            submitAnswer(ApplicantHasSuitableAccountPage, true),
            submitAnswer(BankAccountHolderPage, bankAccountHolder),
            submitAnswer(BankAccountDetailsPage, bankDetails),
            pageMustBe(CheckYourAnswersPage)
          )
      }
    }
  }

  "when the user initially said they did not want to be paid" - {

    "and are Married or Cohabiting" - {

      val relationship = Gen.oneOf(Married, Cohabiting).sample.value

      "and they or their partner receive qualifying benefits" - {

        "and they are currently getting Child Benefit payments" - {

          "changing to say they want to be paid must collect bank details" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, benefits),
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              submitAnswer(WantToBePaidPage, false),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidPage),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
                submitAnswer(WantToBePaidToExistingAccountPage, false),
                submitAnswer(ApplicantHasSuitableAccountPage, false)
              )
          }
        }

        "and they are currently not getting Child Benefit payments" - {

          "changing to say they want to be paid must collect bank details" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, benefits),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              submitAnswer(WantToBePaidPage, false),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidPage),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
                submitAnswer(ApplicantHasSuitableAccountPage, false)
              )
          }
        }

        "and they are currently not claiming Child Benefit" - {

          "changing to say they want to be paid must collect bank details" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, benefits),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
              submitAnswer(WantToBePaidPage, false),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidPage),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
                submitAnswer(ApplicantHasSuitableAccountPage, false)
              )
          }
        }
      }

      "and they and their partner do not receive qualifying benefits" - {

        "and they are currently getting Child Benefit payments" - {

          "changing to say they want to be paid must collect bank details" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              submitAnswer(WantToBePaidPage, false),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidPage),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(WantToBePaidToExistingAccountPage, false),
                submitAnswer(ApplicantHasSuitableAccountPage, false)
              )
          }
        }

        "and they are currently not getting Child Benefit payments" - {

          "changing to say they want to be paid must collect bank details" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              submitAnswer(WantToBePaidPage, false),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidPage),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(ApplicantHasSuitableAccountPage, false)
              )
          }
        }

        "and they are currently not claiming Child Benefit" - {

          "changing to say they want to be paid must collect bank details" in {

            val initialise = journeyOf(
              setUserAnswerTo(RelationshipStatusPage, relationship),
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
              submitAnswer(WantToBePaidPage, false),
              goTo(CheckYourAnswersPage)
            )

            startingFrom(CurrentlyReceivingChildBenefitPage)
              .run(
                initialise,
                goToChangeAnswer(WantToBePaidPage),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(ApplicantHasSuitableAccountPage, false)
              )
          }
        }
      }
    }

    "and are Single, Separated, Divorced or Widowed" - {

      val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

      "and they are currently getting Child Benefit payments" - {

        "changing to say they want to be paid must collect bank details" in {

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            submitAnswer(WantToBePaidPage, false),
            goTo(CheckYourAnswersPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(WantToBePaidPage),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
              submitAnswer(WantToBePaidToExistingAccountPage, false),
              submitAnswer(ApplicantHasSuitableAccountPage, false)
            )
        }
      }

      "and they are currently not getting Child Benefit payments" - {

        "changing to say they want to be paid must collect bank details" in {

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            submitAnswer(WantToBePaidPage, false),
            goTo(CheckYourAnswersPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(WantToBePaidPage),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
              submitAnswer(ApplicantHasSuitableAccountPage, false)
            )
        }
      }

      "and they are currently not claiming Child Benefit" - {

        "changing to say they want to be paid must collect bank details" in {

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            submitAnswer(WantToBePaidPage, false),
            goTo(CheckYourAnswersPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(WantToBePaidPage),
              submitAnswer(WantToBePaidPage, true),
              submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
              submitAnswer(ApplicantHasSuitableAccountPage, false)
            )
        }
      }
    }
  }
}
