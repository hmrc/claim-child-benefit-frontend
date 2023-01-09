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

package journey.tasklist

import generators.ModelGenerators
import journey.JourneyHelpers
import models.RelationshipStatus._
import models.TaskListSectionChange.{PartnerDetailsRemoved, PartnerDetailsRequired, PaymentDetailsRemoved}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages._
import pages.applicant.ApplicantIsHmfOrCivilServantPage
import pages.income._
import pages.partner._
import pages.payments._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingRelationshipSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val adultName = AdultName(None, "first", None, "last")
  private val childName = ChildName("first", None, "last")
  private val nino = arbitrary[Nino].sample.value
  private val nationality = "British"
  private val bankDetails = arbitrary[BankAccountDetails].sample.value

  private val setAlwaysLivedInUk = journeyOf(submitAnswer(AlwaysLivedInUkPage, true))
  private val setHmForces = journeyOf(
    submitAnswer(AlwaysLivedInUkPage, false),
    submitAnswer(ApplicantIsHmfOrCivilServantPage, true)
  )
  private def setAlwaysLivedInUkOrHmForces: JourneyStep[Unit] = Gen.oneOf(setAlwaysLivedInUk, setHmForces).sample.value

  private val setFullPartnerDetails: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(PartnerNamePage, adultName),
    setUserAnswerTo(PartnerNinoKnownPage, true),
    setUserAnswerTo(PartnerNinoPage, nino),
    setUserAnswerTo(PartnerDateOfBirthPage, LocalDate.now),
    setUserAnswerTo(PartnerNationalityPage, nationality),
    setUserAnswerTo(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotGettingPayments),
    setUserAnswerTo(PartnerEldestChildNamePage, childName),
    setUserAnswerTo(PartnerEldestChildDateOfBirthPage, LocalDate.now)
  )

  private val setFullPaymentDetailsSingle: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(ApplicantIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(WantToBePaidPage, true),
    setUserAnswerTo(ApplicantBenefitsPage, Benefits.qualifyingBenefits),
    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
    setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
    setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
    setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
    setUserAnswerTo(BankAccountDetailsPage, bankDetails)
  )

  private val setFullPaymentDetailsPartner: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(ApplicantOrPartnerIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(WantToBePaidPage, true),
    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Benefits.qualifyingBenefits),
    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
    setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
    setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
    setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
    setUserAnswerTo(BankAccountDetailsPage, bankDetails)
  )

  private val partnerDetailsMustHaveBeenRemoved: JourneyStep[Unit] = journeyOf(
    answersMustNotContain(PartnerNamePage),
    answersMustNotContain(PartnerNinoKnownPage),
    answersMustNotContain(PartnerNinoPage),
    answersMustNotContain(PartnerDateOfBirthPage),
    answersMustNotContain(PartnerNationalityPage),
    answersMustNotContain(PartnerClaimingChildBenefitPage),
    answersMustNotContain(PartnerEldestChildNamePage),
    answersMustNotContain(PartnerEldestChildDateOfBirthPage),
    answersMustNotContain(PartnerIsHmfOrCivilServantPage)
  )

  private val partnerDetailsMustRemain: JourneyStep[Unit] = journeyOf(
    answersMustContain(PartnerNamePage),
    answersMustContain(PartnerNinoKnownPage),
    answersMustContain(PartnerNinoPage),
    answersMustContain(PartnerDateOfBirthPage),
    answersMustContain(PartnerNationalityPage),
    answersMustContain(PartnerClaimingChildBenefitPage),
    answersMustContain(PartnerEldestChildNamePage),
    answersMustContain(PartnerEldestChildDateOfBirthPage)
  )

  private val paymentDetailsMustHaveBeenRemoved: JourneyStep[Unit] = journeyOf(
    answersMustNotContain(ApplicantIncomePage),
    answersMustNotContain(ApplicantOrPartnerIncomePage),
    answersMustNotContain(WantToBePaidPage),
    answersMustNotContain(ApplicantBenefitsPage),
    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
    answersMustNotContain(PaymentFrequencyPage),
    answersMustNotContain(WantToBePaidToExistingAccountPage),
    answersMustNotContain(ApplicantHasSuitableAccountPage),
    answersMustNotContain(BankAccountHolderPage),
    answersMustNotContain(BankAccountDetailsPage)
  )

  private val paymentDetailsMustRemainSingle: JourneyStep[Unit] = journeyOf(
    answersMustContain(ApplicantIncomePage),
    answersMustContain(WantToBePaidPage),
    answersMustContain(ApplicantBenefitsPage),
    answersMustContain(PaymentFrequencyPage),
    answersMustContain(WantToBePaidToExistingAccountPage),
    answersMustContain(ApplicantHasSuitableAccountPage),
    answersMustContain(BankAccountHolderPage),
    answersMustContain(BankAccountDetailsPage)
  )

  private val paymentDetailsMustRemainPartner: JourneyStep[Unit] = journeyOf(
    answersMustContain(ApplicantOrPartnerIncomePage),
    answersMustContain(WantToBePaidPage),
    answersMustContain(ApplicantOrPartnerBenefitsPage),
    answersMustContain(PaymentFrequencyPage),
    answersMustContain(WantToBePaidToExistingAccountPage),
    answersMustContain(ApplicantHasSuitableAccountPage),
    answersMustContain(BankAccountHolderPage),
    answersMustContain(BankAccountDetailsPage)
  )

  "when the user originally said they were married" - {

    "changing to say they are cohabiting must collect the cohabitation date then go to Check Relationship" in {

      val initialise = journeyOf(
        submitAnswer(RelationshipStatusPage, Married),
        submitAnswer(AlwaysLivedInUkPage, true),
        setFullPaymentDetailsPartner,
        setFullPartnerDetails,
        pageMustBe(CheckRelationshipDetailsPage)
      )

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(CohabitationDatePage, LocalDate.now),
          pageMustBe(CheckRelationshipDetailsPage),
          partnerDetailsMustRemain,
          paymentDetailsMustRemainPartner,
          answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange])
        )
    }

    "changing to say they are separated" - {

      "when the user said they had not always lived in the UK and wasn't HM Forces or a civil servant abroad, but there partner was" - {

        "must be told they need to use the Print and Post form" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Married),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Separated),
              pageMustBe(UsePrintAndPostFormPage),
              answersMustNotContain(PartnerIsHmfOrCivilServantPage)
            )
        }
      }

      "when the user has always lived in the UK, or is HM Forces or a civil servant abroad" - {

        "when the user had already given some partner and payment details" - {

          "must remove partner and payment details, tell the user those task list sections have changed, collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                paymentDetailsMustHaveBeenRemoved,
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved, PaymentDetailsRemoved))
              )
          }
        }

        "and had already given some partner details but not payment details" - {

          "must remove partner details, tell the user those task list sections have changed, collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved))
              )
          }
        }

        "and had already given some payment details but not partner details" - {

          "must remove payment details, tell the user those task list sections have changed, collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                paymentDetailsMustHaveBeenRemoved,
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PaymentDetailsRemoved))
              )
          }
        }

        "and had not given any partner or payment details" - {

          "must collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange])
              )
          }
        }
      }
    }

    "changing to say they are single, divorced or widowed" - {

      def relationship: RelationshipStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

      "when the user said they had not always lived in the UK and wasn't HM Forces or a civil servant abroad, but there partner was" - {

        "must be told they need to use the Print and Post form" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Married),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, relationship),
              pageMustBe(UsePrintAndPostFormPage),
              answersMustNotContain(PartnerIsHmfOrCivilServantPage)
            )
        }
      }

      "when the user has always lived in the UK, or is HM Forces or a civil servant abroad" - {

        "and had already given some partner and payment details" - {

          "must remove partner and payment details, tell the user those task list sections have changed, then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                paymentDetailsMustHaveBeenRemoved,
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved, PaymentDetailsRemoved))
              )
          }
        }

        "and had already given some partner details but not payment details" - {

          "must remove partner details, tell the user those task list sections have changed, then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved))
              )
          }
        }

        "and had already given some payment details but not partner details" - {

          "must remove payment details, tell the user those task list sections have changed, then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                next,
                pageMustBe(CheckRelationshipDetailsPage),
                paymentDetailsMustHaveBeenRemoved,
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PaymentDetailsRemoved))
              )
          }
        }

        "and has not given any partner or payment details" - {

          "must go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              setAlwaysLivedInUkOrHmForces,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                pageMustBe(CheckRelationshipDetailsPage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange])
              )
          }
        }
      }
    }
  }

  "when the user originally said they were cohabiting" - {

    "changing to say they are married must remove the cohabitation date then go to Check Relationship" in {

      val initialise = journeyOf(
        submitAnswer(RelationshipStatusPage, Cohabiting),
        submitAnswer(CohabitationDatePage, LocalDate.now),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(CheckRelationshipDetailsPage),
        setFullPartnerDetails,
        setFullPaymentDetailsPartner
      )

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Married),
          pageMustBe(CheckRelationshipDetailsPage),
          answersMustNotContain(CohabitationDatePage),
          partnerDetailsMustRemain,
          paymentDetailsMustRemainPartner,
          answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange])
        )
    }

    "changing to say they are separated" - {

      "when the user said they had not always lived in the UK and wasn't HM Forces or a civil servant abroad, but there partner was" - {

        "must be told they need to use the Print and Post form" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Separated),
              pageMustBe(UsePrintAndPostFormPage),
              answersMustNotContain(PartnerIsHmfOrCivilServantPage)
            )
        }
      }

      "when the user has always lived in the UK, or is HM Forces or a civil servant abroad" - {

        "and had already given some partner and payment details" - {

          "must remove partner and payment details, tell the user those task list sections have changed, collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                paymentDetailsMustHaveBeenRemoved,
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved, PaymentDetailsRemoved))
              )
          }
        }

        "and had already given some partner details but not payment details" - {

          "must remove partner details, tell the user those task list sections have changed, collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved))
              )
          }
        }

        "and had already given some payment details but not partner details" - {

          "must remove payment details, tell the user those task list sections have changed, collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                paymentDetailsMustHaveBeenRemoved,
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PaymentDetailsRemoved))
              )
          }
        }

        "and had not given any partner or payment details" - {

          "must collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange])
              )
          }
        }
      }
    }

    "changing to say they are single, divorced or widowed" - {

      def relationship: RelationshipStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

      "when the user said they had not always lived in the UK and wasn't HM Forces or a civil servant abroad, but there partner was" - {

        "must be told they need to use the Print and Post form" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, relationship),
              pageMustBe(UsePrintAndPostFormPage),
              answersMustNotContain(PartnerIsHmfOrCivilServantPage)
            )
        }
      }

      "when the user has always lived in the UK, or is HM Forces or a civil servant abroad" - {

        "and had already given some partner and payment details" - {

          "must remove cohabitation date, partner and payment details, tell the user those task list sections have changed, then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                paymentDetailsMustHaveBeenRemoved,
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved, PaymentDetailsRemoved))
              )
          }
        }

        "and had already given some partner details but not payment details" - {

          "must remove partner details, tell the user those task list sections have changed, then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              setFullPartnerDetails,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                pageMustBe(RelationshipStatusChangesTaskListPage),
                next,
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRemoved))
              )
          }
        }

        "and had already given some payment details but not partner details" - {

          "must remove payment details, tell the user those task list sections have changed, then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              setFullPaymentDetailsPartner,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                next,
                pageMustBe(CheckRelationshipDetailsPage),
                paymentDetailsMustHaveBeenRemoved,
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PaymentDetailsRemoved))
              )
          }
        }

        "and had not given any partner or payment details" - {

          "must remove cohabitation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              setAlwaysLivedInUkOrHmForces,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, relationship),
                pageMustBe(CheckRelationshipDetailsPage),
                answersMustNotContain(CohabitationDatePage),
                answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange])
              )
          }
        }
      }
    }
  }

  "when the user originally said they were separated" - {

    "changing to say they are married" - {

      "when the user had already given payment details" - {

        "must remove separation date and payment details, tell the user partner details are needed and payment details removed, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, true),
            setFullPaymentDetailsSingle,
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              pageMustBe(CheckRelationshipDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answersMustNotContain(SeparationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired, PaymentDetailsRemoved))
            )
        }
      }

      "when the user had not already given payment details" - {

        "must remove separation date, tell the user partner details are needed, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              pageMustBe(CheckRelationshipDetailsPage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired)),
              answersMustNotContain(SeparationDatePage)
            )
        }
      }
    }

    "changing to say they are cohabiting" - {

      "when the user had already given payment details" - {

        "must remove separation date and payment details, tell the user partner details are needed and payment details removed, collect cohabitation date, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, true),
            setFullPaymentDetailsSingle,
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(CohabitationDatePage, LocalDate.now),
              pageMustBe(CheckRelationshipDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answersMustNotContain(SeparationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired, PaymentDetailsRemoved))
            )
        }
      }

      "when the user had not already given payment details" - {

        "must remove separation date, tell the user partner details are needed, collect cohabitation date, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(CohabitationDatePage, LocalDate.now),
              pageMustBe(CheckRelationshipDetailsPage),
              answersMustNotContain(SeparationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired))
            )
        }
      }
    }

    "changing to say they are single, divorced or widowed" - {

      def relationship: RelationshipStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

      "must remove separation date then go to Check Relationship" in {

        val initialise = journeyOf(
          submitAnswer(RelationshipStatusPage, Separated),
          submitAnswer(SeparationDatePage, LocalDate.now),
          submitAnswer(AlwaysLivedInUkPage, true),
          setFullPaymentDetailsSingle,
          pageMustBe(CheckRelationshipDetailsPage)
        )

        startingFrom(RelationshipStatusPage)
          .run(
            initialise,
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, relationship),
            pageMustBe(CheckRelationshipDetailsPage),
            paymentDetailsMustRemainSingle,
            answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange]),
            answersMustNotContain(SeparationDatePage)
          )
      }
    }
  }

  "when the user originally said they were single, divorced or widowed" - {

    def relationship = Gen.oneOf(Single, Divorced, Widowed).sample.value

    "changing to say they are married" - {

      "when the user had already given payment details" - {

        "must remove payment details, tell the user partner details are needed and payment details removed, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, relationship),
            submitAnswer(AlwaysLivedInUkPage, true),
            setFullPaymentDetailsSingle,
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              pageMustBe(CheckRelationshipDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired, PaymentDetailsRemoved))
            )
        }
      }

      "when the user had not already given payment details" - {

        "must tell the user partner details are needed, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, relationship),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              pageMustBe(CheckRelationshipDetailsPage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired))
            )
        }
      }
    }

    "changing to say they are cohabiting" - {

      "when the user had already given payment details" - {

        "must remove payment details, tell the user partner details are needed and payment details removed, collect cohabitation date, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, relationship),
            submitAnswer(AlwaysLivedInUkPage, true),
            setFullPaymentDetailsSingle,
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(CohabitationDatePage, LocalDate.now),
              pageMustBe(CheckRelationshipDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired, PaymentDetailsRemoved))
            )
        }
      }

      "when the user had not already given payment details" - {

        "must tell the user partner details are needed, collect cohabitation date, then go to Check Relationship" in {

          val initialise = journeyOf(
            submitAnswer(RelationshipStatusPage, relationship),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage)
          )

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(CohabitationDatePage, LocalDate.now),
              pageMustBe(CheckRelationshipDetailsPage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, Set[TaskListSectionChange](PartnerDetailsRequired))
            )
        }
      }
    }

    "changing to say they are separated" - {

      def relationship: RelationshipStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

      "must collect separation date then go to Check Relationship" in {

        val initialise = journeyOf(
          submitAnswer(RelationshipStatusPage, relationship),
          submitAnswer(AlwaysLivedInUkPage, true),
          setFullPaymentDetailsSingle,
          pageMustBe(CheckRelationshipDetailsPage)
        )

        startingFrom(RelationshipStatusPage)
          .run(
            initialise,
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            pageMustBe(CheckRelationshipDetailsPage),
            paymentDetailsMustRemainSingle,
            answerMustEqual(RelationshipStatusChangesTaskListPage, Set.empty[TaskListSectionChange])
          )
      }
    }
  }

  "when the user originally said they had always lived in the UK" - {

    "changing to say they haven't must ask if they are HM Forces or a civil servant abroad" in {

      val initialise = journeyOf(
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(CheckRelationshipDetailsPage)
      )

      startingFrom(AlwaysLivedInUkPage)
        .run(
          initialise,
          goToChangeAnswer(AlwaysLivedInUkPage),
          submitAnswer(AlwaysLivedInUkPage, false),
          submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
          pageMustBe(CheckRelationshipDetailsPage)
        )
    }
  }

  "when the user said they hadn't always lived in the UK" - {

    "changing to say they have always lived in the UK must remove whether they or their partner are HM Forces and go to Check Relationship" in {

      val initialise = journeyOf(
        submitAnswer(RelationshipStatusPage, Married),
        submitAnswer(AlwaysLivedInUkPage, false),
        submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
        submitAnswer(PartnerIsHmfOrCivilServantPage, true),
        pageMustBe(CheckRelationshipDetailsPage)
      )

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(AlwaysLivedInUkPage),
          submitAnswer(AlwaysLivedInUkPage, true),
          pageMustBe(CheckRelationshipDetailsPage),
          answersMustNotContain(ApplicantIsHmfOrCivilServantPage),
          answersMustNotContain(PartnerIsHmfOrCivilServantPage)
        )
    }

    "and they were HM Forces or a civil servant abroad" - {

      "changing to say they aren't HM Forces or a civil servant abroad" - {

        "must ask if their partner is HM Forces or a civil servant abroad when the user has a partner" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value
          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true)
          )

          startingFrom(AlwaysLivedInUkPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantIsHmfOrCivilServantPage),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, true),
              pageMustBe(CheckRelationshipDetailsPage)
            )
        }

        "must tell the user to use the Print and Post form when the user does not have a partner" in {

          val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value
          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true)
          )

          startingFrom(AlwaysLivedInUkPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantIsHmfOrCivilServantPage),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
              pageMustBe(UsePrintAndPostFormPage)
            )
        }
      }
    }
  }
}
