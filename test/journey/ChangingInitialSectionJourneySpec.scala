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
import models._
import models.RelationshipStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.child.ChildNamePage
import pages.income._
import pages.partner._
import pages.payments.{ApplicantHasSuitableAccountPage, ClaimedChildBenefitBeforePage, WantToBePaidPage, WantToBePaidWeeklyPage}
import pages.{CannotBePaidWeeklyPage, CheckYourAnswersPage, CohabitationDatePage, RelationshipStatusPage, SeparationDatePage}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingInitialSectionJourneySpec
  extends AnyFreeSpec
    with JourneyHelpers
    with ScalaCheckPropertyChecks
    with ModelGenerators {

  private def benefits              = Set(Gen.oneOf(Benefits.values).sample.value)
  private def qualifyingBenefits    = Set(Gen.oneOf(Benefits.qualifyingBenefits).sample.value)
  private val nonQualifyingBenefits = Set[Benefits](Benefits.NoneOfTheAbove)
  private def partnerName           = arbitrary[PartnerName].sample.value
  private def nino                  = arbitrary[Nino].sample.value
  private def employmentStatus      = Set(arbitrary[PartnerEmploymentStatus].sample.value)
  private def eldestChildName       = arbitrary[PartnerEldestChildName].sample.value
  private def childName             = arbitrary[ChildName].sample.value

  "when a user initially said they were Married" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Married),
      submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
      submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
      submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
      next,
      submitAnswer(ClaimedChildBenefitBeforePage, false),
      setUserAnswerTo(WantToBePaidPage, true),
      setUserAnswerTo(ApplicantHasSuitableAccountPage, false),
      setUserAnswerTo(PartnerNamePage, partnerName),
      setUserAnswerTo(PartnerNinoKnownPage, true),
      setUserAnswerTo(PartnerNinoPage, nino),
      setUserAnswerTo(PartnerDateOfBirthPage, LocalDate.now),
      setUserAnswerTo(PartnerNationalityPage, "nationality"),
      setUserAnswerTo(PartnerEmploymentStatusPage, employmentStatus),
      setUserAnswerTo(PartnerEntitledToChildBenefitPage, false),
      setUserAnswerTo(PartnerWaitingForEntitlementDecisionPage, true),
      setUserAnswerTo(PartnerEldestChildNamePage, eldestChildName),
      setUserAnswerTo(PartnerEldestChildDateOfBirthPage, LocalDate.now),
      setUserAnswerTo(ChildNamePage(Index(0)), childName),
      goTo(CheckYourAnswersPage)
    )

    "and were not eligible to be paid weekly" - {

      "changing the answer to Cohabiting must collect the cohabitation date then return to Check Answers" in {

        startingFrom(RelationshipStatusPage)
          .run(
            initialise,
            setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantOrPartnerIncomeOver50kPage),
            answersMustContain(ApplicantOrPartnerIncomeOver60kPage),
            answersMustContain(ApplicantOrPartnerBenefitsPage),
            answersMustContain(PartnerNamePage),
            answersMustContain(PartnerNinoKnownPage),
            answersMustContain(PartnerNinoPage),
            answersMustContain(PartnerDateOfBirthPage),
            answersMustContain(PartnerNationalityPage),
            answersMustContain(PartnerEmploymentStatusPage),
            answersMustContain(PartnerEntitledToChildBenefitPage),
            answersMustContain(PartnerWaitingForEntitlementDecisionPage),
            answersMustContain(PartnerEldestChildNamePage),
            answersMustContain(PartnerEldestChildDateOfBirthPage)
          )
      }

      "changing the answer to Separated " - {

        "when the user originally said they wanted to be paid Child Benefit" - {

          "must remove joint income and partner details, then collect the separation date and single income details, show the tax charge explanation, and ask whether they want to be paid weekly" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(WantToBePaidWeeklyPage, true),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage)
              )
          }
        }

        "when the user originally said they did not want to be paid Child Benefit" - {

          "must remove joint income and partner details, then collect the separation date and single income details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                setUserAnswerTo(WantToBePaidPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage)
              )
          }
        }
      }

      "changing the answer to Single, Divorced or Widowed" - {

        "when the user originally said they wanted to be paid Child Benefit" - {

          "must remove joint income and partner details, then go to collect single income details, show the tax charge explanation, and ask whether they want to be paid weekly" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(TaxChargeExplanationPage),
                    next,
                    submitAnswer(WantToBePaidWeeklyPage, true),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
                  )
            }
          }
        }

        "when the user originally said they did not want to be paid Child Benefit" - {

          "must remove joint income and partner details, then go to collect single income details" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                    setUserAnswerTo(WantToBePaidPage, false),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
                  )
            }
          }
        }
      }
    }

    "and were eligible to be paid weekly" - {

      "changing the answer to Cohabiting must collect the cohabitation date then return to Check Answers" in {

        startingFrom(RelationshipStatusPage)
          .run(
            initialise,
            setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
            setUserAnswerTo(WantToBePaidWeeklyPage, false),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantOrPartnerIncomeOver50kPage),
            answersMustContain(ApplicantOrPartnerIncomeOver60kPage),
            answersMustContain(ApplicantOrPartnerBenefitsPage),
            answersMustContain(PartnerNamePage),
            answersMustContain(PartnerNinoKnownPage),
            answersMustContain(PartnerNinoPage),
            answersMustContain(PartnerDateOfBirthPage),
            answersMustContain(PartnerNationalityPage),
            answersMustContain(PartnerEmploymentStatusPage),
            answersMustContain(PartnerEntitledToChildBenefitPage),
            answersMustContain(PartnerWaitingForEntitlementDecisionPage),
            answersMustContain(PartnerEldestChildNamePage),
            answersMustContain(PartnerEldestChildDateOfBirthPage),
            answersMustContain(WantToBePaidWeeklyPage)
          )
      }

      "changing the answer to Separated" - {

        "when the user originally said they wanted to be paid Child Benefit" - {

          "must remove joint income and partner details, then go to collect separation date and single income details and show the tax charge explanation" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                setUserAnswerTo(WantToBePaidWeeklyPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                answersMustContain(WantToBePaidWeeklyPage)
              )
          }
        }

        "when the user originally said they did not want to be paid Child Benefit" - {

          "must remove joint income and partner details, then go to collect separation date and single income details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                setUserAnswerTo(WantToBePaidPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage)
              )
          }
        }
      }

      "changing the answer to Single, Divorced or Widowed" - {

        "when the user originally said they wanted to be paid Child Benefit" - {

          "must remove joint income and partner details, then go to collect single income details and show the tax charge explanation" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                    setUserAnswerTo(WantToBePaidWeeklyPage, false),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(TaxChargeExplanationPage),
                    next,
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                    answersMustContain(WantToBePaidWeeklyPage)
                  )
            }
          }
        }

        "when the user originally said they did not want to be paid Child Benefit" - {

          "must remove joint income and partner details, then go to collect single income details" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                    setUserAnswerTo(WantToBePaidPage, false),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
                  )
            }
          }
        }
      }
    }
  }

  "when a user initially said they were Cohabiting" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Cohabiting),
      submitAnswer(CohabitationDatePage, LocalDate.now),
      submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
      submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
      submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
      setUserAnswerTo(WantToBePaidPage, true),
      setUserAnswerTo(ApplicantHasSuitableAccountPage, false),
      setUserAnswerTo(PartnerNamePage, partnerName),
      setUserAnswerTo(PartnerNinoKnownPage, true),
      setUserAnswerTo(PartnerNinoPage, nino),
      setUserAnswerTo(PartnerDateOfBirthPage, LocalDate.now),
      setUserAnswerTo(PartnerNationalityPage, "nationality"),
      setUserAnswerTo(PartnerEmploymentStatusPage, employmentStatus),
      setUserAnswerTo(PartnerEntitledToChildBenefitPage, false),
      setUserAnswerTo(PartnerWaitingForEntitlementDecisionPage, true),
      setUserAnswerTo(PartnerEldestChildNamePage, eldestChildName),
      setUserAnswerTo(PartnerEldestChildDateOfBirthPage, LocalDate.now),
      setUserAnswerTo(ChildNamePage(Index(0)), childName),
      goTo(CheckYourAnswersPage)
    )

    "and were not eligible to be paid weekly" - {

      "changing the answer to Married" - {

        "must remove the cohabitation date then return to Check Answers" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(CohabitationDatePage),
              answersMustContain(ApplicantOrPartnerIncomeOver50kPage),
              answersMustContain(ApplicantOrPartnerIncomeOver60kPage),
              answersMustContain(ApplicantOrPartnerBenefitsPage),
              answersMustContain(PartnerNamePage),
              answersMustContain(PartnerNinoKnownPage),
              answersMustContain(PartnerNinoPage),
              answersMustContain(PartnerDateOfBirthPage),
              answersMustContain(PartnerNationalityPage),
              answersMustContain(PartnerEmploymentStatusPage),
              answersMustContain(PartnerEntitledToChildBenefitPage),
              answersMustContain(PartnerWaitingForEntitlementDecisionPage),
              answersMustContain(PartnerEldestChildNamePage),
              answersMustContain(PartnerEldestChildDateOfBirthPage)
            )
        }
      }

      "changing the answer to Separated" - {

        "when the user originally said they wanted to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, collect separation date and single income details, show the tax charge explanation, and ask whether they want to be paid weekly" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(WantToBePaidWeeklyPage, false),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage)
              )
          }
        }

        "when the user originally said they did not want to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, collect separation date and single income details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                setUserAnswerTo(WantToBePaidPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage)
              )
          }
        }
      }

      "changing the answer to Single, Divorced or Widowed" - {

        "when they originally said they wanted to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, then collect single income details, show the tax charge explanation, and ask whether they want to be paid weekly" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(TaxChargeExplanationPage),
                    next,
                    submitAnswer(WantToBePaidWeeklyPage, true),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
                  )
            }
          }
        }

        "when they originally said they did not want to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, then collect single income details and whether they want to be paid weekly" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                    setUserAnswerTo(WantToBePaidPage, false),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
                  )
            }
          }
        }
      }
    }

    "and were eligible to be paid weekly" - {

      "changing the answer to Married" - {

        "must remove the cohabitation date then return to Check Answers" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
              setUserAnswerTo(WantToBePaidWeeklyPage, false),
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(CohabitationDatePage),
              answersMustContain(ApplicantOrPartnerIncomeOver50kPage),
              answersMustContain(ApplicantOrPartnerIncomeOver60kPage),
              answersMustContain(ApplicantOrPartnerBenefitsPage),
              answersMustContain(PartnerNamePage),
              answersMustContain(PartnerNinoKnownPage),
              answersMustContain(PartnerNinoPage),
              answersMustContain(PartnerDateOfBirthPage),
              answersMustContain(PartnerNationalityPage),
              answersMustContain(PartnerEmploymentStatusPage),
              answersMustContain(PartnerEntitledToChildBenefitPage),
              answersMustContain(PartnerWaitingForEntitlementDecisionPage),
              answersMustContain(PartnerEldestChildNamePage),
              answersMustContain(PartnerEldestChildDateOfBirthPage),
              answersMustContain(WantToBePaidWeeklyPage)
            )
        }
      }

      "changing the answer to Separated" - {

        "when the user initially said they wanted to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, collect separation date and single income details and show the tax charge explanation" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                setUserAnswerTo(WantToBePaidWeeklyPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                answersMustContain(WantToBePaidWeeklyPage)
              )
          }
        }

        "when the user initially said they did not want to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, collect separation date and single income details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                setUserAnswerTo(WantToBePaidPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomeOver50kPage, false),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerEntitledToChildBenefitPage),
                answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage)
              )
          }
        }
      }

      "changing the answer to Single, Divorced or Widowed" - {

        "when the user initially said they wanted to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, collect separation date and single income details and show the tax charge explanation" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                    setUserAnswerTo(WantToBePaidWeeklyPage, false),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(TaxChargeExplanationPage),
                    next,
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                    answersMustContain(WantToBePaidWeeklyPage)
                  )
            }
          }
        }

        "when the user initially said they did not want to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, collect separation date and single income details" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>
                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                    setUserAnswerTo(WantToBePaidPage, false),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomeOver50kPage, false),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
                    answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerEntitledToChildBenefitPage),
                    answersMustNotContain(PartnerWaitingForEntitlementDecisionPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
                  )
            }
          }
        }
      }
    }
  }

  "when the user initially said they were Separated" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Separated),
      submitAnswer(SeparationDatePage, LocalDate.now),
      submitAnswer(ApplicantIncomeOver50kPage, true),
      submitAnswer(ApplicantIncomeOver60kPage, true),
      submitAnswer(ApplicantBenefitsPage, benefits),
      setUserAnswerTo(ChildNamePage(Index(0)), childName),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Married" - {

      "when they originally wanted to be paid weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove the separation date and single income details, go to collect joint income details, show the tax charge explanation, then collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, true),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustContain(WantToBePaidWeeklyPage)
              )
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove the separation date, single income details and whether they want to be paid weekly, go to collect joint income details, tell the user they cannot be paid weekly, show the tax charge explanation, then collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, true),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(CannotBePaidWeeklyPage),
                next,
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustNotContain(WantToBePaidWeeklyPage)
              )
          }
        }
      }

      "when they originally wanted to be paid but not weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove the separation date and single income details, then go to collect joint income, show the tax charge explanation, then collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustContain(WantToBePaidWeeklyPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage)
              )
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove the separation date, single income details and whether they want to be paid weekly, then go to collect joint income details, show the tax charge explanation, then collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(WantToBePaidWeeklyPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage)
              )
          }
        }
      }

      "when they originally did not want to be paid" - {

        "must remove the separation date and single income details, then go to collect joint income then partner details" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setUserAnswerTo(WantToBePaidPage, false),
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
              submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
              submitAnswer(PartnerNamePage, partnerName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage, "nationality"),
              submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
              submitAnswer(PartnerEntitledToChildBenefitPage, false),
              submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
              submitAnswer(PartnerEldestChildNamePage, eldestChildName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(SeparationDatePage),
              answersMustNotContain(ApplicantIncomeOver50kPage),
              answersMustNotContain(ApplicantIncomeOver60kPage),
              answersMustNotContain(ApplicantBenefitsPage)
            )
        }
      }
    }

    "changing the answer to Cohabiting" - {

      "when they originally wanted to be paid weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove the separation date and single income details, then go to collect cohab date and joint income details, show the tax charge explanation, the collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, true),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustContain(WantToBePaidWeeklyPage)
              )
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove the separation date, single income details and whether they wanted to be paid weekly, then go to collect cohab date and joint income details, tell the user they cannot be paid weekly, show the tax charge explanation, and collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, true),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(CannotBePaidWeeklyPage),
                next,
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustNotContain(WantToBePaidWeeklyPage)
              )
          }
        }
      }

      "when they originally wanted to be paid but not weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove the separation date and single income details, then go to collect cohab date, joint income details and partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustContain(WantToBePaidWeeklyPage)
              )
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove the separation date, single income details and whether they wanted to be paid weekly, then go to collect cohab date and joint income details, show the tax charge explanation, and collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(WantToBePaidWeeklyPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(TaxChargeExplanationPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustNotContain(WantToBePaidWeeklyPage)
              )
          }
        }
      }

      "when they originally did not want to be paid" - {

        "must remove the separation date and single income details, then go to collect cohab date and joint income details" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setUserAnswerTo(WantToBePaidPage, false),
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
              submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
              submitAnswer(PartnerNamePage, partnerName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage, "nationality"),
              submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
              submitAnswer(PartnerEntitledToChildBenefitPage, false),
              submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
              submitAnswer(PartnerEldestChildNamePage, eldestChildName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(SeparationDatePage),
              answersMustNotContain(ApplicantIncomeOver50kPage),
              answersMustNotContain(ApplicantIncomeOver60kPage),
              answersMustNotContain(ApplicantBenefitsPage)
            )
        }
      }
    }

    "changing the answer to Single, Divorced or Widowed must remove the separation date and go to Check Answers" in {

      forAll(Gen.oneOf(Single, Divorced, Widowed)) {
        status =>

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, status),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(SeparationDatePage),
              answersMustContain(ApplicantIncomeOver50kPage),
              answersMustContain(ApplicantIncomeOver60kPage),
              answersMustContain(ApplicantBenefitsPage)
            )
      }
    }
  }

  "when the user initially said they were Single, Divorced or Widowed" - {

    def initialise(status: RelationshipStatus) = journeyOf(
      submitAnswer(RelationshipStatusPage, status),
      submitAnswer(ApplicantIncomeOver50kPage, true),
      submitAnswer(ApplicantIncomeOver60kPage, true),
      submitAnswer(ApplicantBenefitsPage, benefits),
      setUserAnswerTo(ChildNamePage(Index(0)), childName),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Married" - {

      "when they originally wanted to be paid weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove single income details, then go to collect joint income details, show the tax charge explanation, and collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, true),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove single income details and whether they want to be paid weekly, go to collect joint income details, tell the user they cannot be paid weekly, show the tax charge explanation, then collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, true),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(CannotBePaidWeeklyPage),
                  next,
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }
      }

      "and they initially wanted to be paid but not weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove single income details, then go to collect joint income details, show the tax charge explanation, and collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, false),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove single income details and whether they want to be paid weekly,  go to collect joint income details, show the tax charge explanation,  and collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, false),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }
      }

      "and they initially did not want to be paid" - {

        "must remove single income details, then go to collect joint income and partner details" in {

          Seq(Single, Divorced, Widowed).foreach { status =>

            startingFrom(RelationshipStatusPage)
              .run(
                initialise(status),
                setUserAnswerTo(WantToBePaidPage, false),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerEntitledToChildBenefitPage, false),
                submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantIncomeOver50kPage),
                answersMustNotContain(ApplicantIncomeOver60kPage),
                answersMustNotContain(ApplicantBenefitsPage),
              )
          }
        }
      }
    }

    "changing the answer to Cohabiting" - {

      "when they originally wanted to be paid weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove single income details, then collect the cohabitation date then joint income details, show the tax charge explanation, and collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, true),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove single income details and whether they want to be paid weekly, collect the cohabitation date then joint income details, tell the user they cannot be paid weekly, show the tax charge explanation, then collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, true),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(CannotBePaidWeeklyPage),
                  next,
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }
      }

      "when they initially wanted to be paid but not weekly" - {

        "and the user or their partner have qualifying benefits" - {

          "must remove single income details, then collect the cohabitation date then joint income details, show the tax charge explanation, then collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, false),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove single income details and whether they want to be paid weekly, then collect the cohabitation date then joint income details, show the tax charge explanation, and collect partner details" in {

            Seq(Single, Divorced, Widowed).foreach { status =>

              startingFrom(RelationshipStatusPage)
                .run(
                  initialise(status),
                  setUserAnswerTo(WantToBePaidPage, true),
                  setUserAnswerTo(WantToBePaidWeeklyPage, false),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(TaxChargeExplanationPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerEntitledToChildBenefitPage, false),
                  submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomeOver50kPage),
                  answersMustNotContain(ApplicantIncomeOver60kPage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(WantToBePaidWeeklyPage)
                )
            }
          }
        }
      }
    }

    "changing the answer to Separated must collect the separation date then go to Check Answers" in {

      Seq(Single, Divorced, Widowed).foreach { status =>

        startingFrom(RelationshipStatusPage)
          .run(
            initialise(status),
            setUserAnswerTo(WantToBePaidPage, true),
            setUserAnswerTo(WantToBePaidWeeklyPage, true),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantIncomeOver50kPage),
            answersMustContain(ApplicantIncomeOver60kPage),
            answersMustContain(ApplicantBenefitsPage),
            answersMustContain(WantToBePaidWeeklyPage)
          )
      }
    }

    "changing the answer to Single, Divorced or Widowed must go to Check Answers" in {

      Seq(Single, Divorced, Widowed).foreach { status =>

        val newStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

        startingFrom(RelationshipStatusPage)
          .run(
            initialise(status),
            setUserAnswerTo(WantToBePaidPage, true),
            setUserAnswerTo(WantToBePaidWeeklyPage, true),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, newStatus),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantIncomeOver50kPage),
            answersMustContain(ApplicantIncomeOver60kPage),
            answersMustContain(ApplicantBenefitsPage),
            answersMustContain(WantToBePaidWeeklyPage)
          )
      }
    }
  }
}
