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
import models.{IncludedDocuments, Index}
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class IncludedDocumentsPageSpec extends PageBehaviours {

  "IncludedDocumentsPage" - {

    beRetrievable[Set[IncludedDocuments]](IncludedDocumentsPage(index))

    beSettable[Set[IncludedDocuments]](IncludedDocumentsPage(index))

    beRemovable[Set[IncludedDocuments]](IncludedDocumentsPage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Check Child Details for the same index" in {

          IncludedDocumentsPage(Index(0))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(0)))

          IncludedDocumentsPage(Index(1))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(1)))
        }
      }

      "when the current waypoint is Check Child Details" - {

        def waypoints(index: Index) =
          EmptyWaypoints.setNextWaypoint(CheckChildDetailsPage(index).waypoint)

        "to Check Child Details with the current waypoint removed" in {

          IncludedDocumentsPage(Index(0))
            .navigate(waypoints(Index(0)), emptyUserAnswers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

          IncludedDocumentsPage(Index(1))
            .navigate(waypoints(Index(1)), emptyUserAnswers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
        }
      }
    }
  }
}
