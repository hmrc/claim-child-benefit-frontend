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
import models.CurrentlyReceivingChildBenefit.{NotClaiming, NotGettingPayments}
import models.RelationshipStatus._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.child.ChildNamePage
import pages.income._
import pages.partner._
import pages.payments.{ApplicantHasSuitableAccountPage, CurrentlyReceivingChildBenefitPage, PaymentFrequencyPage, WantToBePaidPage}
import pages.{CannotBePaidWeeklyPage, CheckYourAnswersPage, CohabitationDatePage, AlwaysLivedInUkPage, RelationshipStatusPage, SeparationDatePage}
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
  private def partnerName           = arbitrary[AdultName].sample.value
  private def nino                  = arbitrary[Nino].sample.value
  private def employmentStatus      = Set(arbitrary[EmploymentStatus].sample.value)
  private def eldestChildName       = arbitrary[ChildName].sample.value
  private def childName             = arbitrary[ChildName].sample.value
  private def income                = arbitrary[Income].sample.value

  "when a user initially said they were Married" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Married),
      submitAnswer(AlwaysLivedInUkPage, true),
      submitAnswer(ApplicantOrPartnerIncomePage, income),
      submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
      submitAnswer(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming),
      setUserAnswerTo(WantToBePaidPage, true),
      setUserAnswerTo(ApplicantHasSuitableAccountPage, false),
      setUserAnswerTo(PartnerNamePage, partnerName),
      setUserAnswerTo(PartnerNinoKnownPage, true),
      setUserAnswerTo(PartnerNinoPage, nino),
      setUserAnswerTo(PartnerDateOfBirthPage, LocalDate.now),
      setUserAnswerTo(PartnerNationalityPage, "nationality"),
      setUserAnswerTo(PartnerEmploymentStatusPage, employmentStatus),
      setUserAnswerTo(PartnerIsHmfOrCivilServantPage, false),
      setUserAnswerTo(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming),
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
            answersMustContain(ApplicantOrPartnerIncomePage),
            answersMustContain(ApplicantOrPartnerBenefitsPage),
            answersMustContain(PartnerNamePage),
            answersMustContain(PartnerNinoKnownPage),
            answersMustContain(PartnerNinoPage),
            answersMustContain(PartnerDateOfBirthPage),
            answersMustContain(PartnerNationalityPage),
            answersMustContain(PartnerEmploymentStatusPage),
            answersMustContain(PartnerIsHmfOrCivilServantPage),
            answersMustContain(PartnerClaimingChildBenefitPage),
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
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                submitAnswer(WantToBePaidPage, true),
                submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
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
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
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
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(WantToBePaidPage),
                    next,
                    submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
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
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
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
            setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantOrPartnerIncomePage),
            answersMustContain(ApplicantOrPartnerBenefitsPage),
            answersMustContain(PartnerNamePage),
            answersMustContain(PartnerNinoKnownPage),
            answersMustContain(PartnerNinoPage),
            answersMustContain(PartnerDateOfBirthPage),
            answersMustContain(PartnerNationalityPage),
            answersMustContain(PartnerEmploymentStatusPage),
            answersMustContain(PartnerIsHmfOrCivilServantPage),
            answersMustContain(PartnerClaimingChildBenefitPage),
            answersMustContain(PartnerEldestChildNamePage),
            answersMustContain(PartnerEldestChildDateOfBirthPage),
            answersMustContain(PaymentFrequencyPage)
          )
      }

      "changing the answer to Separated" - {

        "when the user originally said they wanted to be paid Child Benefit" - {

          "must remove joint income and partner details, then go to collect separation date and single income details and show the tax charge explanation" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(WantToBePaidPage),
                next,
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                answersMustContain(PaymentFrequencyPage)
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
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
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
                    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(WantToBePaidPage),
                    next,
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                    answersMustContain(PaymentFrequencyPage)
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
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
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
      submitAnswer(AlwaysLivedInUkPage, true),
      submitAnswer(ApplicantOrPartnerIncomePage, income),
      submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
      setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
      setUserAnswerTo(WantToBePaidPage, true),
      setUserAnswerTo(ApplicantHasSuitableAccountPage, false),
      setUserAnswerTo(PartnerNamePage, partnerName),
      setUserAnswerTo(PartnerNinoKnownPage, true),
      setUserAnswerTo(PartnerNinoPage, nino),
      setUserAnswerTo(PartnerDateOfBirthPage, LocalDate.now),
      setUserAnswerTo(PartnerNationalityPage, "nationality"),
      setUserAnswerTo(PartnerEmploymentStatusPage, employmentStatus),
      setUserAnswerTo(PartnerIsHmfOrCivilServantPage, false),
      setUserAnswerTo(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming),
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
              answersMustContain(ApplicantOrPartnerIncomePage),
              answersMustContain(ApplicantOrPartnerBenefitsPage),
              answersMustContain(PartnerNamePage),
              answersMustContain(PartnerNinoKnownPage),
              answersMustContain(PartnerNinoPage),
              answersMustContain(PartnerDateOfBirthPage),
              answersMustContain(PartnerNationalityPage),
              answersMustContain(PartnerEmploymentStatusPage),
              answersMustContain(PartnerIsHmfOrCivilServantPage),
              answersMustContain(PartnerClaimingChildBenefitPage),
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
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
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
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
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
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(WantToBePaidPage),
                    next,
                    submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
                  )
            }
          }
        }

        "when they originally said they did not want to be paid Child Benefit" - {

          "must remove cohab date, joint income and partner details, then collect single income details" in {

            forAll(Gen.oneOf(Single, Divorced, Widowed)) {
              status =>

                startingFrom(RelationshipStatusPage)
                  .run(
                    initialise,
                    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
                    setUserAnswerTo(WantToBePaidPage, false),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
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
              setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(CohabitationDatePage),
              answersMustContain(ApplicantOrPartnerIncomePage),
              answersMustContain(ApplicantOrPartnerBenefitsPage),
              answersMustContain(PartnerNamePage),
              answersMustContain(PartnerNinoKnownPage),
              answersMustContain(PartnerNinoPage),
              answersMustContain(PartnerDateOfBirthPage),
              answersMustContain(PartnerNationalityPage),
              answersMustContain(PartnerEmploymentStatusPage),
              answersMustContain(PartnerIsHmfOrCivilServantPage),
              answersMustContain(PartnerClaimingChildBenefitPage),
              answersMustContain(PartnerEldestChildNamePage),
              answersMustContain(PartnerEldestChildDateOfBirthPage),
              answersMustContain(PaymentFrequencyPage)
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
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Separated),
                submitAnswer(SeparationDatePage, LocalDate.now),
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(WantToBePaidPage),
                next,
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
                answersMustNotContain(PartnerEldestChildNamePage),
                answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                answersMustContain(PaymentFrequencyPage)
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
                submitAnswer(ApplicantIncomePage, income),
                submitAnswer(ApplicantBenefitsPage, benefits),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(CohabitationDatePage),
                answersMustNotContain(ApplicantOrPartnerIncomePage),
                answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                answersMustNotContain(PartnerNamePage),
                answersMustNotContain(PartnerNinoKnownPage),
                answersMustNotContain(PartnerNinoPage),
                answersMustNotContain(PartnerDateOfBirthPage),
                answersMustNotContain(PartnerNationalityPage),
                answersMustNotContain(PartnerEmploymentStatusPage),
                answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                answersMustNotContain(PartnerClaimingChildBenefitPage),
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
                    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                    goToChangeAnswer(RelationshipStatusPage),
                    submitAnswer(RelationshipStatusPage, status),
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(WantToBePaidPage),
                    next,
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
                    answersMustNotContain(PartnerEldestChildNamePage),
                    answersMustNotContain(PartnerEldestChildDateOfBirthPage),
                    answersMustContain(PaymentFrequencyPage)
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
                    submitAnswer(ApplicantIncomePage, income),
                    submitAnswer(ApplicantBenefitsPage, benefits),
                    pageMustBe(CheckYourAnswersPage),
                    answersMustNotContain(CohabitationDatePage),
                    answersMustNotContain(ApplicantOrPartnerIncomePage),
                    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
                    answersMustNotContain(PartnerNamePage),
                    answersMustNotContain(PartnerNinoKnownPage),
                    answersMustNotContain(PartnerNinoPage),
                    answersMustNotContain(PartnerDateOfBirthPage),
                    answersMustNotContain(PartnerNationalityPage),
                    answersMustNotContain(PartnerEmploymentStatusPage),
                    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
                    answersMustNotContain(PartnerClaimingChildBenefitPage),
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
      submitAnswer(AlwaysLivedInUkPage, true),
      submitAnswer(ApplicantIncomePage, income),
      submitAnswer(ApplicantBenefitsPage, benefits),
      submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
      submitAnswer(WantToBePaidPage, true),
      submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
      submitAnswer(ApplicantHasSuitableAccountPage, false),
      setUserAnswerTo(ChildNamePage(Index(0)), childName),
      setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotClaiming),
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
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustContain(PaymentFrequencyPage)
              )
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove the separation date, single income details and whether they want to be paid weekly, go to collect joint income details, tell the user they cannot be paid weekly, show the tax charge explanation, then collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(CannotBePaidWeeklyPage),
                next,
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.WaitingToHear),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustNotContain(PaymentFrequencyPage)
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
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotGettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustContain(PaymentFrequencyPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
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
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Married),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(PaymentFrequencyPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
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
              submitAnswer(ApplicantOrPartnerIncomePage, income),
              submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
              submitAnswer(PartnerNamePage, partnerName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage, "nationality"),
              submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, eldestChildName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(SeparationDatePage),
              answersMustNotContain(ApplicantIncomePage),
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
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustContain(PaymentFrequencyPage)
              )
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove the separation date, single income details and whether they wanted to be paid weekly, then go to collect cohab date and joint income details, tell the user they cannot be paid weekly, show the tax charge explanation, and collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(CannotBePaidWeeklyPage),
                next,
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustNotContain(PaymentFrequencyPage)
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
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustContain(PaymentFrequencyPage)
              )
          }
        }

        "and the user or their partner do not have qualifying benefits" - {

          "must remove the separation date, single income details and whether they wanted to be paid weekly, then go to collect cohab date and joint income details, show the tax charge explanation, and collect partner details" in {

            startingFrom(RelationshipStatusPage)
              .run(
                initialise,
                setUserAnswerTo(WantToBePaidPage, true),
                setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                goToChangeAnswer(RelationshipStatusPage),
                submitAnswer(RelationshipStatusPage, Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                pageMustBe(WantToBePaidPage),
                next,
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(SeparationDatePage),
                answersMustNotContain(ApplicantIncomePage),
                answersMustNotContain(ApplicantBenefitsPage),
                answersMustNotContain(PaymentFrequencyPage)
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
              submitAnswer(ApplicantOrPartnerIncomePage, income),
              submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
              submitAnswer(PartnerNamePage, partnerName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage, "nationality"),
              submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, eldestChildName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(SeparationDatePage),
              answersMustNotContain(ApplicantIncomePage),
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
              answersMustContain(ApplicantIncomePage),
              answersMustContain(ApplicantBenefitsPage)
            )
      }
    }
  }

  "when the user initially said they were Single, Divorced or Widowed" - {

    def initialise(status: RelationshipStatus) = journeyOf(
      submitAnswer(RelationshipStatusPage, status),
      submitAnswer(AlwaysLivedInUkPage, true),
      submitAnswer(ApplicantIncomePage, income),
      submitAnswer(ApplicantBenefitsPage, benefits),
      submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
      submitAnswer(WantToBePaidPage, true),
      submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
      submitAnswer(ApplicantHasSuitableAccountPage, false),
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(PaymentFrequencyPage)
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(CannotBePaidWeeklyPage),
                  next,
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(PaymentFrequencyPage)
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(PaymentFrequencyPage)
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Married),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(PaymentFrequencyPage)
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
                submitAnswer(ApplicantOrPartnerIncomePage, income),
                submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                submitAnswer(PartnerNamePage, partnerName),
                submitAnswer(PartnerNinoKnownPage, true),
                submitAnswer(PartnerNinoPage, nino),
                submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                submitAnswer(PartnerNationalityPage, "nationality"),
                submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                pageMustBe(CheckYourAnswersPage),
                answersMustNotContain(ApplicantIncomePage),
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(PaymentFrequencyPage)
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(CannotBePaidWeeklyPage),
                  next,
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(PaymentFrequencyPage)
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustContain(PaymentFrequencyPage)
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
                  setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.EveryFourWeeks),
                  goToChangeAnswer(RelationshipStatusPage),
                  submitAnswer(RelationshipStatusPage, Cohabiting),
                  submitAnswer(CohabitationDatePage, LocalDate.now),
                  submitAnswer(ApplicantOrPartnerIncomePage, income),
                  submitAnswer(ApplicantOrPartnerBenefitsPage, nonQualifyingBenefits),
                  pageMustBe(WantToBePaidPage),
                  next,
                  submitAnswer(PartnerNamePage, partnerName),
                  submitAnswer(PartnerNinoKnownPage, true),
                  submitAnswer(PartnerNinoPage, nino),
                  submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
                  submitAnswer(PartnerNationalityPage, "nationality"),
                  submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
                  submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
                  submitAnswer(PartnerEldestChildNamePage, eldestChildName),
                  submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
                  pageMustBe(CheckYourAnswersPage),
                  answersMustNotContain(ApplicantIncomePage),
                  answersMustNotContain(ApplicantBenefitsPage),
                  answersMustNotContain(PaymentFrequencyPage)
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
            setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantIncomePage),
            answersMustContain(ApplicantBenefitsPage),
            answersMustContain(PaymentFrequencyPage)
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
            setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, newStatus),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantIncomePage),
            answersMustContain(ApplicantBenefitsPage),
            answersMustContain(PaymentFrequencyPage)
          )
      }
    }
  }
}
