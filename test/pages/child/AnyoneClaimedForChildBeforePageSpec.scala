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

package pages.child

import controllers.child.routes
import models.AnyoneClaimedForChildBefore._
import models.{AnyoneClaimedForChildBefore, Index}
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class AnyoneClaimedForChildBeforePageSpec extends PageBehaviours {

  "AnyoneClaimedForChildBeforePage" - {

    beRetrievable[AnyoneClaimedForChildBefore](AnyoneClaimedForChildBeforePage(index))

    beSettable[AnyoneClaimedForChildBefore](AnyoneClaimedForChildBeforePage(index))

    beRemovable[AnyoneClaimedForChildBefore](AnyoneClaimedForChildBeforePage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Previous Claimant Name with the same index when the answer is Someone Else" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), SomeoneElse).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), SomeoneElse).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers).route
            .mustEqual(routes.PreviousClaimantNameController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers).route
            .mustEqual(routes.PreviousClaimantNameController.onPageLoad(waypoints, Index(1)))
        }

        "to Adopting Child with the same index when the answer is Applicant" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), Applicant).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), Applicant).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(1)))
        }

        "to Adopting Child with the same index when the answer is Partner" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), Partner).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), Partner).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(1)))
        }

        "to Adopting Child with the same index when the answer is No" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), No).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), No).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(1)))
        }
      }
    }
  }
}
