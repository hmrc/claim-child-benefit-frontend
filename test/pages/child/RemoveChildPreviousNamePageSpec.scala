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

class RemoveChildPreviousNamePageSpec extends PageBehaviours {

  "RemoveChildPreviousNamePage" - {

    "must navigate" - {

      val childName = ChildName("first", None, "last")

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "when there is one child" - {

          "to Add Child Previous Name when there is at least one previous name left" in {

            val answers = emptyUserAnswers.set(ChildPreviousNamePage(Index(0), Index(0)), childName).success.value

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(0)))
          }

          "to Child Has Previous Name when there are no previous names left" in {

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, emptyUserAnswers)
              .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(0)))
          }
        }

        "when there is more than one child" - {

          "to Add Child Previous Name when there is at least one previous name left" in {

            val answers =
              emptyUserAnswers
                .set(ChildPreviousNamePage(Index(0), Index(0)), childName).success.value
                .set(ChildPreviousNamePage(Index(1), Index(0)), childName).success.value

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(0)))

            RemoveChildPreviousNamePage(Index(1), Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(1)))
          }

          "to Child Has Previous Name when there are no previous names left" in {

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, emptyUserAnswers)
              .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(0)))

            RemoveChildPreviousNamePage(Index(1), Index(0))
              .navigate(waypoints, emptyUserAnswers)
              .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(1)))
          }
        }
      }

      "when the current waypoint is Check Child Details" - {

        val waypoints = EmptyWaypoints.setNextWaypoint(CheckChildDetailsPage(Index(0)).waypoint)

        "when there is one child" - {

          "to Add Child Previous Name when there is at least one previous name left" in {

            val answers = emptyUserAnswers.set(ChildPreviousNamePage(Index(0), Index(0)), childName).success.value

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(0)))
          }

          "to Child Has Previous Name when there are no previous names left" in {

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, emptyUserAnswers)
              .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(0)))
          }
        }

        "when there is more than one child" - {

          "to Add Child Previous Name when there is at least one previous name left" in {

            val answers =
              emptyUserAnswers
                .set(ChildPreviousNamePage(Index(0), Index(0)), childName).success.value
                .set(ChildPreviousNamePage(Index(1), Index(0)), childName).success.value

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(0)))

            RemoveChildPreviousNamePage(Index(1), Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(1)))
          }

          "to Child Has Previous Name when there are no previous names left" in {

            RemoveChildPreviousNamePage(Index(0), Index(0))
              .navigate(waypoints, emptyUserAnswers)
              .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(0)))

            RemoveChildPreviousNamePage(Index(1), Index(0))
              .navigate(waypoints, emptyUserAnswers)
              .mustEqual(routes.ChildHasPreviousNameController.onPageLoad(waypoints, Index(1)))
          }
        }
      }
    }
  }
}
