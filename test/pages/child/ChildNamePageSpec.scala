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
import models.{ChildName, Index}
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class ChildNamePageSpec extends PageBehaviours {

  "ChildNamePage" - {

    beRetrievable[ChildName](ChildNamePage(index))

    beSettable[ChildName](ChildNamePage(index))

    beRemovable[ChildName](ChildNamePage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Child Has Previous Name for the same index" in {

          ChildNamePage(Index(0))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(0)))

          ChildNamePage(Index(1))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(1)))
        }
      }

      "when the current waypoint is Check Child Details" - {

        def waypoints(index: Index) =
          EmptyWaypoints.setNextWaypoint(CheckChildDetailsPage(index).waypoint)

        "to Check Child Details with the current waypoint removed" in {

          ChildNamePage(Index(0))
            .navigate(waypoints(Index(0)), emptyUserAnswers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

          ChildNamePage(Index(1))
            .navigate(waypoints(Index(1)), emptyUserAnswers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
        }
      }
    }
  }
}
