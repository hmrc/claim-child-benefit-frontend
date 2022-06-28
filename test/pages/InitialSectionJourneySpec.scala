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

package pages

import models.{ApplicantName, RelationshipStatus}
import pages.JourneyState.startingFrom
import pages.income.{ApplicantIncomeOver50kPage, ApplicantOrPartnerIncomeOver50kPage}

import java.time.LocalDate

class InitialSectionJourneySpec extends JourneyHelpers {
  
  private val applicantName = ApplicantName(None, "first", None, "last")

  "eligible Married users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, false, AnyChildLivedWithOthersPage),
        answerPage(AnyChildLivedWithOthersPage, false, ApplicantNamePage),
        answerPage(ApplicantNamePage, applicantName, RelationshipStatusPage),
        answerPage(RelationshipStatusPage, RelationshipStatus.Married, ApplicantOrPartnerIncomeOver50kPage)
      )
  }

  "eligible Cohabiting users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, false, AnyChildLivedWithOthersPage),
        answerPage(AnyChildLivedWithOthersPage, false, ApplicantNamePage),
        answerPage(ApplicantNamePage, applicantName, RelationshipStatusPage),
        answerPage(RelationshipStatusPage, RelationshipStatus.Cohabiting, RelationshipStatusDatePage),
        answerPage(RelationshipStatusDatePage, LocalDate.now.minusDays(1), ApplicantOrPartnerIncomeOver50kPage)
      )
  }

  "eligible Single users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, false, AnyChildLivedWithOthersPage),
        answerPage(AnyChildLivedWithOthersPage, false, ApplicantNamePage),
        answerPage(ApplicantNamePage, applicantName, RelationshipStatusPage),
        answerPage(RelationshipStatusPage, RelationshipStatus.Single, ApplicantIncomeOver50kPage)
      )
  }

  "eligible Separated users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, false, AnyChildLivedWithOthersPage),
        answerPage(AnyChildLivedWithOthersPage, false, ApplicantNamePage),
        answerPage(ApplicantNamePage, applicantName, RelationshipStatusPage),
        answerPage(RelationshipStatusPage, RelationshipStatus.Separated, RelationshipStatusDatePage),
        answerPage(RelationshipStatusDatePage, LocalDate.now.minusDays(1), ApplicantIncomeOver50kPage)
      )
  }

  "eligible Divorced users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, false, AnyChildLivedWithOthersPage),
        answerPage(AnyChildLivedWithOthersPage, false, ApplicantNamePage),
        answerPage(ApplicantNamePage, applicantName, RelationshipStatusPage),
        answerPage(RelationshipStatusPage, RelationshipStatus.Divorced, ApplicantIncomeOver50kPage)
      )
  }

  "eligible Widowed users must continue to the income section" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, false, AnyChildLivedWithOthersPage),
        answerPage(AnyChildLivedWithOthersPage, false, ApplicantNamePage),
        answerPage(ApplicantNamePage, applicantName, RelationshipStatusPage),
        answerPage(RelationshipStatusPage, RelationshipStatus.Widowed, ApplicantIncomeOver50kPage)
      )
  }

  "users who have lived or worked abroad must go to the Use Print and Post Form page" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, true, UsePrintAndPostFormPage)
      )
  }

  "users claiming for a child who has recently lived with someone else must go to the Use Print and Post Form page" in {

    startingFrom(EverLivedOrWorkedAbroadPage)
      .steps(
        answerPage(EverLivedOrWorkedAbroadPage, false, AnyChildLivedWithOthersPage),
        answerPage(AnyChildLivedWithOthersPage, true, UsePrintAndPostFormPage)
      )
  }
}
