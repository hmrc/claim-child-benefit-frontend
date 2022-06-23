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
import models.{Index, PreviousClaimantAddress, PreviousClaimantName}
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class PreviousClaimantNamePageSpec extends PageBehaviours {

  "PreviousClaimantNamePage" - {

    beRetrievable[PreviousClaimantName](PreviousClaimantNamePage(index))

    beSettable[PreviousClaimantName](PreviousClaimantNamePage(index))

    beRemovable[PreviousClaimantName](PreviousClaimantNamePage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Previous Claimant Address" in {

          PreviousClaimantNamePage(Index(0))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.PreviousClaimantAddressController.onPageLoad(waypoints, Index(0)))

          PreviousClaimantNamePage(Index(1))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.PreviousClaimantAddressController.onPageLoad(waypoints, Index(1)))
        }
      }

      "when the current waypoint is Check Child Details" - {

        def waypoints(index: Index) =
          EmptyWaypoints.setNextWaypoint(CheckChildDetailsPage(index).waypoint)

        "to Check Child Details with the current waypoint removed when Previous Claimant Address has been answered" in {

          val address = PreviousClaimantAddress("line 1", None, None, "postcode")

          val answers =
            emptyUserAnswers
              .set(PreviousClaimantAddressPage(Index(0)), address).success.value
              .set(PreviousClaimantAddressPage(Index(1)), address).success.value

          PreviousClaimantNamePage(Index(0))
            .navigate(waypoints(Index(0)), answers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

          PreviousClaimantNamePage(Index(1))
            .navigate(waypoints(Index(1)), answers)
            .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
        }

        "to Previous Claimant Address when it has not already been answered" in {

          PreviousClaimantNamePage(Index(0))
            .navigate(waypoints(Index(0)), emptyUserAnswers)
            .mustEqual(routes.PreviousClaimantAddressController.onPageLoad(waypoints(Index(0)), Index(0)))

          PreviousClaimantNamePage(Index(1))
            .navigate(waypoints(Index(1)), emptyUserAnswers)
            .mustEqual(routes.PreviousClaimantAddressController.onPageLoad(waypoints(Index(1)), Index(1)))
        }
      }
    }
  }
}
