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

import controllers.routes
import models.RelationshipStatus
import pages.behaviours.PageBehaviours

class RelationshipStatusPageSpec extends PageBehaviours {

  "RelationshipStatusPage" - {

    beRetrievable[RelationshipStatus](RelationshipStatusPage)

    beSettable[RelationshipStatus](RelationshipStatusPage)

    beRemovable[RelationshipStatus](RelationshipStatusPage)

    "must navigate" - {

      import models.RelationshipStatus._

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Relationship Status Date when the answer is Cohabiting" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Cohabiting).success.value

          RelationshipStatusPage
            .navigate(waypoints, answers)
            .mustEqual(routes.RelationshipStatusDateController.onPageLoad(waypoints))
        }

        "to Relationship Status Date when the answer is Separated" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Separated).success.value

          RelationshipStatusPage
            .navigate(waypoints, answers)
            .mustEqual(routes.RelationshipStatusDateController.onPageLoad(waypoints))
        }

        "to Applicant or Partner Income over 50k when the answer is Married" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Married).success.value

          RelationshipStatusPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantOrPartnerIncomeOver50kController.onPageLoad(waypoints))
        }

        "to Applicant Income Over 50k when the answer is Widowed" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Widowed).success.value

          RelationshipStatusPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantIncomeOver50kController.onPageLoad(waypoints))
        }

        "to Applicant Income Over 50k when the answer is Divorced" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Divorced).success.value

          RelationshipStatusPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantIncomeOver50kController.onPageLoad(waypoints))
        }

        "to Applicant Income Over 50k when the answer is Single" in {

          val answers = emptyUserAnswers.set(RelationshipStatusPage, Single).success.value

          RelationshipStatusPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantIncomeOver50kController.onPageLoad(waypoints))
        }
      }
    }
  }
}
