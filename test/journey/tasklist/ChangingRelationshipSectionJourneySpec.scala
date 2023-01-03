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

package journey.tasklist

import generators.ModelGenerators
import journey.JourneyHelpers
import models._
import models.RelationshipStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.partner._
import pages._
import pages.income._
import pages.payments._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingRelationshipSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val adultName = AdultName(None, "first", None, "last")
  private val childName = ChildName("first", None, "last")
  private val nino = arbitrary[Nino].sample.value
  private val nationality = "British"
  private val bankDetails = arbitrary[BankAccountDetails].sample.value

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

  private val setFullPaymentDetails: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(ApplicantIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(ApplicantOrPartnerIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(WantToBePaidPage, true),
    setUserAnswerTo(ApplicantBenefitsPage, Benefits.qualifyingBenefits),
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
    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
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

  "when the user originally said they were married" - {

    "changing to say they are cohabiting must collect the cohabitation date then go to Check Relationship" in {

      val initialise = journeyOf(
        submitAnswer(RelationshipStatusPage, Married),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(CheckRelationshipDetailsPage)
      )

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(CohabitationDatePage, LocalDate.now),
          pageMustBe(CheckRelationshipDetailsPage)
        )
    }

    "changing to say they are separated" - {

      "when the user has always lived in the UK" - {

        "and had already given some partner and payment details" - {

          "must remove partner and payment details, tell the user their task list sections have changed, collect the separation date then go to Check Relationship" in {

            val initialise = journeyOf(
              submitAnswer(RelationshipStatusPage, Married),
              submitAnswer(AlwaysLivedInUkPage, true),
              setFullPartnerDetails,
              setFullPaymentDetails,
              pageMustBe(CheckRelationshipDetailsPage)
            )

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                pageMustBe(TaskListSectionsChangedPage),
                next,
                submitAnswer(SeparationDatePage, LocalDate.now),
                pageMustBe(CheckRelationshipDetailsPage),
                partnerDetailsMustHaveBeenRemoved,
                paymentDetailsMustHaveBeenRemoved
              )
          }
        }

        "and had already given some partner details but not payment details" - {

          "must remove partner details, tell the user their task list sections have changed collect the separation date then go to Check Relationship" in {

          }
        }

        "and had already given some payment details but not partner details" - {

          "must remove payment details, tell the user their task list sections have changed, collect the separation date then go to Check Relationship" in {

          }
        }
      }

      "when the user is HM Forces or a civil servant abroad" - {

        "and had already given some partner and payment details" - {

          "must remove partner and payment details, tell the user their task list sections have changed, collect the separation date, then go to Check Relationship" in {

          }
        }

        "and had already given some partner details but not payment details" - {

          "must remove partner details, tell the user their task list sections have changed, collect the separation date then go to Check Relationship" in {

          }
        }

        "and had already given some payment details but not partner details" - {

          "must  remove payment details, tell the user their task list sections have changed, collect the separation date then go to Check Relationship" in {

          }
        }
      }

      "when the user has not always lived in the UK and is not HM Forces or a civil servant abroad, but their partner was" - {

        "must be told to use the Print and Post form" in {

        }
      }
    }
  }
}
