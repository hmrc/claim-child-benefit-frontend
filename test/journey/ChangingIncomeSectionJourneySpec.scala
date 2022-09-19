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
import models.CurrentlyReceivingChildBenefit.NotClaiming
import models.RelationshipStatus._
import models.{AdultName, Benefits, Income, PaymentFrequency}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.income._
import pages.partner.PartnerNamePage
import pages.payments.{ApplicantHasSuitableAccountPage, CurrentlyReceivingChildBenefitPage, PaymentFrequencyPage, WantToBePaidPage}
import pages.{CheckYourAnswersPage, RelationshipStatusPage}

class ChangingIncomeSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def benefits = Set(Gen.oneOf(Benefits.values).sample.value)

  "when the user changes their income answer" - {

    "they must be shown the tax charge explanation then return to Check Answers" in {

      val relationshipStatus = Gen.oneOf(Single, Separated, Widowed, Divorced).sample.value
      val income1 = Gen.oneOf(Income.values).sample.value
      val income2 = Gen.oneOf(Income.values.filterNot(_ == income1)).sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
        submitAnswer(ApplicantIncomePage, income1),
        submitAnswer(ApplicantBenefitsPage, benefits),
        next,
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
        submitAnswer(WantToBePaidPage, false),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantIncomePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantIncomePage),
          submitAnswer(ApplicantIncomePage, income2),
          pageMustBe(TaxChargeExplanationPage),
          next,
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }

  "when the user changes their or their partner's income answer" - {

    "they must be shown the tax charge explanation then return to Check Answers" in {

      val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
      val income1 = Gen.oneOf(Income.values).sample.value
      val income2 = Gen.oneOf(Income.values.filterNot(_ == income1)).sample.value
      val partnerName = arbitrary[AdultName].sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
        submitAnswer(ApplicantOrPartnerIncomePage, income1),
        submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
        next,
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
        submitAnswer(WantToBePaidPage, false),
        setUserAnswerTo(PartnerNamePage, partnerName),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantOrPartnerIncomePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantOrPartnerIncomePage),
          submitAnswer(ApplicantOrPartnerIncomePage, income2),
          pageMustBe(TaxChargeExplanationPage),
          next,
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }


  "when the user initially said they or their partner did not receive any qualifying benefits" - {

    def qualifyingBenefits = Set[Benefits](Gen.oneOf(Benefits.qualifyingBenefits).sample.value)
    def income = arbitrary[Income].sample.value

    "and they wanted to be paid Child Benefit" - {

      "changing to say they do receive benefits must ask if they want to be paid weekly then return to Check Answers" in {

        val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
        val partnerName        = arbitrary[AdultName].sample.value

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
          submitAnswer(ApplicantOrPartnerIncomePage, income),
          submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
          next,
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
          submitAnswer(WantToBePaidPage, true),
          setUserAnswerTo(PartnerNamePage, partnerName),
          setUserAnswerTo(ApplicantHasSuitableAccountPage, false),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantOrPartnerIncomePage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantOrPartnerBenefitsPage),
            submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
            pageMustBe(TaxChargeExplanationPage),
            next,
            submitAnswer(PaymentFrequencyPage, PaymentFrequency.Weekly),
            pageMustBe(CheckYourAnswersPage)
          )
      }
    }

    "and they did not want to be paid Child Benefit" - {

      "changing to say they do receive benefits must return to Check Answers" in {

        val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
        val partnerName        = arbitrary[AdultName].sample.value

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
          submitAnswer(ApplicantOrPartnerIncomePage, income),
          submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
          next,
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
          submitAnswer(WantToBePaidPage, false),
          setUserAnswerTo(PartnerNamePage, partnerName),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantOrPartnerIncomePage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantOrPartnerBenefitsPage),
            submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
            pageMustBe(CheckYourAnswersPage)
          )
      }
    }
  }
}
