/*
 * Copyright 2023 HM Revenue & Customs
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

package journey.tasklist

import generators.ModelGenerators
import journey.JourneyHelpers
import models.RelationshipStatus._
import org.scalatest.freespec.AnyFreeSpec
import pages._
import pages.applicant.ApplicantIsHmfOrCivilServantPage
import pages.partner.PartnerIsHmfOrCivilServantPage

import java.time.LocalDate

class RelationshipSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users who have recently claimed must go to the Already Claimed page" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, true),
        pageMustBe(AlreadyClaimedPage)
      )
  }

  "users who have not recently claimed" - {

    "who are Married" - {

      "must continue to the task list if they or their partner have always lived in the UK" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Married),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if they are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Married),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if their partner are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Married),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must be told to use a different form if neither they nor their partner are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Married),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, false),
            pageMustBe(UsePrintAndPostFormPage)
          )
      }
    }

    "who are Cohabiting" - {

      "must continue to the task list if they or their partner have always lived in the UK" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if they are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if their partner are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must be told to use a different form if neither they nor their partner are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            submitAnswer(PartnerIsHmfOrCivilServantPage, false),
            pageMustBe(UsePrintAndPostFormPage)
          )
      }
    }

    "who are Single" - {

      "must continue to the task list if they have always lived in the UK" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Single),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if they are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Single),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must be told to use a different form if they are not HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Single),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            pageMustBe(UsePrintAndPostFormPage)
          )
      }
    }

    "who are Separated" - {

      "must continue to the task list if they have always lived in the UK" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if they are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must be told to use a different form if they are not HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            pageMustBe(UsePrintAndPostFormPage)
          )
      }
    }

    "who are Divorced" - {

      "must continue to the task list if they have always lived in the UK" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Divorced),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if they are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Divorced),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must be told to use a different form if they are not HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Divorced),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            pageMustBe(UsePrintAndPostFormPage)
          )
      }
    }

    "who are Widowed" - {

      "must continue to the task list if they have always lived in the UK" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Widowed),
            submitAnswer(AlwaysLivedInUkPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must continue to the task list if they are HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Widowed),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            pageMustBe(CheckRelationshipDetailsPage),
            next,
            pageMustBe(TaskListPage)
          )
      }

      "must be told to use a different form if they are not HM Forces or a civil servant abroad" in {

        startingFrom(RecentlyClaimedPage)
          .run(
            submitAnswer(RecentlyClaimedPage, false),
            submitAnswer(RelationshipStatusPage, Widowed),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
            pageMustBe(UsePrintAndPostFormPage)
          )
      }
    }
  }
}
