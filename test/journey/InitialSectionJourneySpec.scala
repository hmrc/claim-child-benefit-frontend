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

import models.{AdultName, RelationshipStatus}
import org.scalatest.freespec.AnyFreeSpec
import pages.income.{ApplicantIncomePage, ApplicantOrPartnerIncomePage}
import pages._
import pages.applicant.ApplicantIsHmfOrCivilServantPage

import java.time.LocalDate

class InitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers {
  
  private val applicantName = AdultName("first", None, "last")

  "eligible Married users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(LivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Married),
        pageMustBe(ApplicantOrPartnerIncomePage)
      )
  }

  "eligible Cohabiting users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(LivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Cohabiting),
        submitAnswer(CohabitationDatePage, LocalDate.now.minusDays(1)),
        pageMustBe(ApplicantOrPartnerIncomePage)
      )
  }

  "eligible Single users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(LivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Single),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "eligible Separated users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(LivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Separated),
        submitAnswer(SeparationDatePage, LocalDate.now.minusDays(1)),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "eligible Divorced users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(LivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Divorced),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "eligible Widowed users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(LivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Widowed),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "users who have recently claimed must go to the Already Claimed page" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, true),
        pageMustBe(AlreadyClaimedPage)
      )
  }

  "users who have lived or worked abroad" - {

    "who are HM Forces or a civil servant abroad" - {

      "must proceed with the journey" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(LivedOrWorkedAbroadPage, true),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            submitAnswer(AnyChildLivedWithOthersPage, false),
            pageMustBe(ApplicantNamePage)
          )
      }
    }

    "who are not HM Forces or a civil servant abroad" - {

      "must go to the Use Print and Post Form page" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(LivedOrWorkedAbroadPage, true),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            pageMustBe(UsePrintAndPostFormPage)
          )
      }
    }
  }

  "users claiming for a child who has recently lived with someone else must go to the Use Print and Post Form page" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(LivedOrWorkedAbroadPage, false),
        submitAnswer(AnyChildLivedWithOthersPage, true),
        pageMustBe(UsePrintAndPostFormPage)
      )
  }
}
