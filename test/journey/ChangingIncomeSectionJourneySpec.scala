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
import models.{AdultName, Benefits}
import models.RelationshipStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.{CheckYourAnswersPage, RelationshipStatusPage}
import pages.income._
import pages.partner.PartnerNamePage
import pages.payments.{ApplicantHasSuitableAccountPage, ClaimedChildBenefitBeforePage, WantToBePaidPage, WantToBePaidWeeklyPage}

class ChangingIncomeSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def benefits = Set(Gen.oneOf(Benefits.values).sample.value)

  "when the user initially said their income was over 50k" - {

    "changing the answer to false must remove the answer to `income over 60k`, show the tax charge explanation then return to Check Answers" in {

      val relationshipStatus = Gen.oneOf(Single, Separated, Widowed, Divorced).sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
        submitAnswer(ApplicantIncomeOver50kPage, true),
        submitAnswer(ApplicantIncomeOver60kPage, true),
        submitAnswer(ApplicantBenefitsPage, benefits),
        next,
        submitAnswer(ClaimedChildBenefitBeforePage, false),
        submitAnswer(WantToBePaidPage, false),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantIncomeOver50kPage),
          submitAnswer(ApplicantIncomeOver50kPage, false),
          pageMustBe(TaxChargeExplanationPage),
          next,
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantIncomeOver60kPage)
        )
    }
  }

  "when the user initially said their income was not above 50k" - {

    "changing the answer to true must collect `income over 60k`, show the tax charge explanation then return to Check Answers" in {

      val relationshipStatus = Gen.oneOf(Single, Separated, Widowed, Divorced).sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
        submitAnswer(ApplicantIncomeOver50kPage, false),
        submitAnswer(ApplicantBenefitsPage, benefits),
        next,
        submitAnswer(ClaimedChildBenefitBeforePage, false),
        submitAnswer(WantToBePaidPage, false),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantIncomeOver50kPage),
          submitAnswer(ApplicantIncomeOver50kPage, true),
          submitAnswer(ApplicantIncomeOver60kPage, true),
          pageMustBe(TaxChargeExplanationPage),
          next,
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }
  
  "when the user initially said their or their partner's income was over 50k" - {

    "changing the answer to false must remove the answer to `income over 60k`, show the tax charge explanation then return to Check Answers" in {

      val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
      val partnerName        = arbitrary[AdultName].sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
        submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
        submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
        submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
        next,
        submitAnswer(ClaimedChildBenefitBeforePage, false),
        submitAnswer(WantToBePaidPage, false),
        setUserAnswerTo(PartnerNamePage, partnerName),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantOrPartnerIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantOrPartnerIncomeOver50kPage),
          submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
          pageMustBe(TaxChargeExplanationPage),
          next,
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage)
        )
    }
  }

  "when the user initially said their or their partner's income was not above 50k" - {
    
    "changing the answer to true must collect `income over 60k`, show the tax charge explanation then return to Check Answers" in {

      val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
      val partnerName        = arbitrary[AdultName].sample.value

      val initialise = journeyOf(
        setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
        submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
        submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
        next,
        submitAnswer(ClaimedChildBenefitBeforePage, false),
        submitAnswer(WantToBePaidPage, false),
        setUserAnswerTo(PartnerNamePage, partnerName),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantOrPartnerIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantOrPartnerIncomeOver50kPage),
          submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
          submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
          pageMustBe(TaxChargeExplanationPage),
          next,
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }

  "when the user initially said they or their partner did not receive any qualifying benefits" - {

    def qualifyingBenefits = Set[Benefits](Gen.oneOf(Benefits.qualifyingBenefits).sample.value)

    "and they wanted to be paid Child Benefit" - {

      "changing to say they do receive benefits must ask if they want to be paid weekly then return to Check Answers" in {

        val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
        val partnerName        = arbitrary[AdultName].sample.value

        val initialise = journeyOf(
          setUserAnswerTo(RelationshipStatusPage, relationshipStatus),
          submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
          submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
          next,
          submitAnswer(ClaimedChildBenefitBeforePage, false),
          submitAnswer(WantToBePaidPage, true),
          setUserAnswerTo(PartnerNamePage, partnerName),
          setUserAnswerTo(ApplicantHasSuitableAccountPage, false),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantOrPartnerIncomeOver50kPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantOrPartnerBenefitsPage),
            submitAnswer(ApplicantOrPartnerBenefitsPage, qualifyingBenefits),
            pageMustBe(TaxChargeExplanationPage),
            next,
            submitAnswer(WantToBePaidWeeklyPage, true),
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
          submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
          submitAnswer(ApplicantOrPartnerBenefitsPage, Set[Benefits](Benefits.NoneOfTheAbove)),
          next,
          submitAnswer(ClaimedChildBenefitBeforePage, false),
          submitAnswer(WantToBePaidPage, false),
          setUserAnswerTo(PartnerNamePage, partnerName),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantOrPartnerIncomeOver50kPage)
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
