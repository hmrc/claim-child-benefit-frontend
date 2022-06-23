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

class ChildHasPreviousNamePageSpec extends PageBehaviours {

  "ChildHasPreviousNamePage" - {

    beRetrievable[Boolean](ChildHasPreviousNamePage(index))

    beSettable[Boolean](ChildHasPreviousNamePage(index))

    beRemovable[Boolean](ChildHasPreviousNamePage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Child Previous Name for the same index when the answer is yes" in {

          val answers =
            emptyUserAnswers
              .set(ChildHasPreviousNamePage(Index(0)), true).success.value
              .set(ChildHasPreviousNamePage(Index(1)), true).success.value

          ChildHasPreviousNamePage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildNameChangedByDeedPollController.onPageLoad(waypoints, Index(0)))

          ChildHasPreviousNamePage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildNameChangedByDeedPollController.onPageLoad(waypoints, Index(1)))
        }

        "to Child Biological Sex for the same index when the answer is no" in {

          val answers =
            emptyUserAnswers
              .set(ChildHasPreviousNamePage(Index(0)), false).success.value
              .set(ChildHasPreviousNamePage(Index(1)), false).success.value

          ChildHasPreviousNamePage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildBiologicalSexController.onPageLoad(waypoints, Index(0)))

          ChildHasPreviousNamePage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildBiologicalSexController.onPageLoad(waypoints, Index(1)))
        }
      }

      "when the current waypoint is Check Child Details" - {

        def waypoints(index: Index) =
          EmptyWaypoints.setNextWaypoint(CheckChildDetailsPage(index).waypoint)

        "when the answer is yes" - {

          "to Child Name Changed by Deed Poll when that question has not been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildHasPreviousNamePage(Index(0)), true).success.value
                .set(ChildHasPreviousNamePage(Index(1)), true).success.value

            val index0Waypoints = waypoints(Index(0))
            val index1Waypoints = waypoints(Index(1))

            ChildHasPreviousNamePage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.ChildNameChangedByDeedPollController.onPageLoad(index0Waypoints, Index(0)))

            ChildHasPreviousNamePage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.ChildNameChangedByDeedPollController.onPageLoad(index1Waypoints, Index(1)))
          }

          "to Check Child Details with the current waypoint removed when that question has been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildHasPreviousNamePage(Index(0)), true).success.value
                .set(ChildHasPreviousNamePage(Index(1)), true).success.value
                .set(ChildNameChangedByDeedPollPage(Index(0)), true).success.value
                .set(ChildNameChangedByDeedPollPage(Index(1)), true).success.value

            val index0Waypoints = waypoints(Index(0))
            val index1Waypoints = waypoints(Index(1))

            ChildHasPreviousNamePage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            ChildHasPreviousNamePage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }
        }

        "when the answer is no" - {

          "to Check Child Details with the current waypoint removed" in {

            val answers =
              emptyUserAnswers
                .set(ChildHasPreviousNamePage(Index(0)), false).success.value
                .set(ChildHasPreviousNamePage(Index(1)), false).success.value

            val index0Waypoints = waypoints(Index(0))
            val index1Waypoints = waypoints(Index(1))

            ChildHasPreviousNamePage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            ChildHasPreviousNamePage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }
        }
      }
    }

    "must remove all of this child's previous names when the answer is no" - {

      val childName = ChildName("first", None, "last")

      val answers =
        emptyUserAnswers
          .set(ChildPreviousNamePage(Index(0), Index(0)), childName).success.value
          .set(ChildPreviousNamePage(Index(0), Index(1)), childName).success.value
          .set(ChildPreviousNamePage(Index(1), Index(0)), childName).success.value
          .set(ChildPreviousNamePage(Index(1), Index(1)), childName).success.value

      "for index 0" in {

        val result = answers.set(ChildHasPreviousNamePage(Index(0)), false).success.value

        result.get(ChildPreviousNamePage(Index(0), Index(0))) must not be defined
        result.get(ChildPreviousNamePage(Index(0), Index(1))) must not be defined
        result.get(ChildPreviousNamePage(Index(1), Index(0))) mustBe defined
        result.get(ChildPreviousNamePage(Index(1), Index(1))) mustBe defined
      }

      "for index 1" in {

        val result = answers.set(ChildHasPreviousNamePage(Index(1)), false).success.value

        result.get(ChildPreviousNamePage(Index(0), Index(0))) mustBe defined
        result.get(ChildPreviousNamePage(Index(0), Index(1))) mustBe defined
        result.get(ChildPreviousNamePage(Index(1), Index(0))) must not be defined
        result.get(ChildPreviousNamePage(Index(1), Index(1))) must not be defined
      }
    }
  }
}
