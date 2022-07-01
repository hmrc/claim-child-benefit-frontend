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

import models.Benefits
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.CheckYourAnswersPage
import pages.income._

class ChangingIncomeSectionJourneySpec extends AnyFreeSpec with JourneyHelpers {

  private def benefits = Set(Gen.oneOf(Benefits.values).sample.value)

  "when the user initially said their income was over 50k" - {

    "changing the answer to false must remove the answer to `income over 60k` and return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantIncomeOver50kPage, true),
        submitAnswer(ApplicantIncomeOver60kPage, true),
        submitAnswer(ApplicantBenefitsPage, benefits),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantIncomeOver50kPage),
          submitAnswer(ApplicantIncomeOver50kPage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantIncomeOver60kPage)
        )
    }
  }

  "when the user initially said their income was not above 50k" - {


    "changing the answer to true must collect `income over 60k` and return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantIncomeOver50kPage, false),
        submitAnswer(ApplicantBenefitsPage, benefits),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantIncomeOver50kPage),
          submitAnswer(ApplicantIncomeOver50kPage, true),
          submitAnswer(ApplicantIncomeOver60kPage, true),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }
  
  "when the user initially said their or their partner's income was over 50k" - {

    "changing the answer to false must remove the answer to `income over 60k` and return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
        submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
        submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantOrPartnerIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantOrPartnerIncomeOver50kPage),
          submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage)
        )
    }
  }

  "when the user initially said their or their partner's income was not above 50k" - {
    
    "changing the answer to true must collect `income over 60k` and return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantOrPartnerIncomeOver50kPage, false),
        submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantOrPartnerIncomeOver50kPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantOrPartnerIncomeOver50kPage),
          submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
          submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }
}
