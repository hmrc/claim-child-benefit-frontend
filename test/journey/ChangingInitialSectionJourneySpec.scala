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

import models.RelationshipStatus
import models.RelationshipStatus._
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.{CheckYourAnswersPage, RelationshipStatusDatePage, RelationshipStatusPage}

import java.time.LocalDate

class ChangingInitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers {

  "when a user initially said they were Married, Single, Widowed or Divorced" - {

    def status: RelationshipStatus = Gen.oneOf(Married, Single, Widowed, Divorced).sample.value

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, status),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Separated must collect the separation date then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Separated),
          submitAnswer(RelationshipStatusDatePage, LocalDate.now),
          pageMustBe(CheckYourAnswersPage)
        )
    }

    "changing the answer to Cohabiting must collect the separation date then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(RelationshipStatusDatePage, LocalDate.now),
          pageMustBe(CheckYourAnswersPage)
        )
    }

    "changing the answer to Married, Single, Divorced or Widowed must return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, status),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }

  "when a user initially said they were Cohabiting or Separated" - {

    def status: RelationshipStatus = Gen.oneOf(Cohabiting, Separated).sample.value

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, status),
      submitAnswer(RelationshipStatusDatePage, LocalDate.now),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Married must remove Relationship Status Date and return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Married),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(RelationshipStatusDatePage)
        )
    }

    "changing the answer to Single must remove Relationship Status Date and return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Single),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(RelationshipStatusDatePage)
        )
    }

    "changing the answer to Divorced must remove Relationship Status Date and return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Divorced),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(RelationshipStatusDatePage)
        )
    }

    "changing the answer to Widowed must remove Relationship Status Date and return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Widowed),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(RelationshipStatusDatePage)
        )
    }

    "changing the answer to Cohabiting or Separated must return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, status),
          pageMustBe(CheckYourAnswersPage),
          answersMustContain(RelationshipStatusDatePage)
        )
    }
  }
}