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

import controllers.income.{routes => incomeRoutes}
import models.RelationshipStatus.{Cohabiting, Separated}

import java.time.LocalDate
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

class RelationshipStatusDatePageSpec extends PageBehaviours {

  "RelationshipStatusDatePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](RelationshipStatusDatePage)

    beSettable[LocalDate](RelationshipStatusDatePage)

    beRemovable[LocalDate](RelationshipStatusDatePage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Applicant Income Over 50k when the relationship status is Separated" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Separated).success.value

          RelationshipStatusDatePage
            .navigate(waypoints, answers).route
            .mustEqual(incomeRoutes.ApplicantIncomeOver50kController.onPageLoad(waypoints))
        }

        "to Applicant or Partner Income Over 50k when the relationship status is Cohabiting" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Cohabiting).success.value

          RelationshipStatusDatePage
            .navigate(waypoints, answers).route
            .mustEqual(incomeRoutes.ApplicantOrPartnerIncomeOver50kController.onPageLoad(waypoints))
        }
      }
    }
  }
}
