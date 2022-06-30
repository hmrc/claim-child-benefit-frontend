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
import models.RelationshipStatus._
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.income._
import pages.{CheckYourAnswersPage, RelationshipStatusDatePage, RelationshipStatusPage}

import java.time.LocalDate

class ChangingInitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers {

  "when a user initially said they were Married" - {

    def benefits = Set(Gen.oneOf(Benefits.values).sample.value)

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Married),
      submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
      submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
      submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Cohabiting must collect the date then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(RelationshipStatusDatePage, LocalDate.now),
          pageMustBe(CheckYourAnswersPage)
        )
    }

    "changing the answer to Separated must collect the date and single income details, remove joint income details, then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Separated),
          submitAnswer(RelationshipStatusDatePage, LocalDate.now),
          submitAnswer(ApplicantIncomeOver50kPage, true),
          submitAnswer(ApplicantIncomeOver60kPage, true),
          submitAnswer(ApplicantBenefitsPage, benefits),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustNotContain(ApplicantOrPartnerBenefitsPage)
        )
    }

    "changing the answer to Single must collect single income details, remove joint income details, then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Single),
          submitAnswer(ApplicantIncomeOver50kPage, true),
          submitAnswer(ApplicantIncomeOver60kPage, true),
          submitAnswer(ApplicantBenefitsPage, benefits),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustNotContain(ApplicantOrPartnerBenefitsPage)
        )
    }

    "changing the answer to Divorced must collect single income details, remove joint income details, then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Divorced),
          submitAnswer(ApplicantIncomeOver50kPage, true),
          submitAnswer(ApplicantIncomeOver60kPage, true),
          submitAnswer(ApplicantBenefitsPage, benefits),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustNotContain(ApplicantOrPartnerBenefitsPage)
        )
    }

    "changing the answer to Widowed must collect single income details, remove joint income details, then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Widowed),
          submitAnswer(ApplicantIncomeOver50kPage, true),
          submitAnswer(ApplicantIncomeOver60kPage, true),
          submitAnswer(ApplicantBenefitsPage, benefits),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustNotContain(ApplicantOrPartnerBenefitsPage)
        )
    }
  }
}