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

import models.{ApplicantName, RelationshipStatus}
import org.scalatest.freespec.AnyFreeSpec
import pages.{AnyChildLivedWithOthersPage, ApplicantNamePage, EverLivedOrWorkedAbroadPage, RelationshipStatusDatePage, RelationshipStatusPage}

import java.time.LocalDate

class InitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers {
  
  private val applicantName = ApplicantName(None, "first", None, "last")

  "eligible Married users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Married)
      )
  }

  "eligible Cohabiting users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Cohabiting),
        submitAnswer(RelationshipStatusDatePage, LocalDate.now.minusDays(1))
      )
  }

  "eligible Single users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Single)
      )
  }

  "eligible Separated users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Separated),
        submitAnswer(RelationshipStatusDatePage, LocalDate.now.minusDays(1))
      )
  }

  "eligible Divorced users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Divorced)
      )
  }

  "eligible Widowed users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Widowed)
      )
  }

  "users who have lived or worked abroad must go to the Use Print and Post Form page" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, true)
      )
  }

  "users claiming for a child who has recently lived with someone else must go to the Use Print and Post Form page" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .run(
        submitAnswer(EverLivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, true)
      )
  }
}
