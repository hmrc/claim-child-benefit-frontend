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

class AddChildPreviousNamePageSpec extends PageBehaviours {

  "AddChildPreviousNamePage" - {

    beRetrievable[Boolean](AddChildPreviousNamePage(index))

    beSettable[Boolean](AddChildPreviousNamePage(index))

    beRemovable[Boolean](AddChildPreviousNamePage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints
        val childPreviousName = ChildName("first", None, "last")

        "when the answer is yes" - {

          "and there is a single child with a single previous name" - {

            "to Child Previous Name for child index 0, and name index 1" in {

              val answers =
                emptyUserAnswers
                  .set(ChildPreviousNamePage(Index(0), Index(0)), childPreviousName).success.value
                  .set(AddChildPreviousNamePage(Index(0)), true).success.value

              AddChildPreviousNamePage(Index(0))
                .navigate(waypoints, answers)
                .mustEqual(routes.ChildPreviousNameController.onPageLoad(waypoints, Index(0), Index(1)))
            }
          }

          "and there is a single child with two previous names" - {

            "to Child Previous Name for child index 0, and name index 2" in {

              val answers =
                emptyUserAnswers
                  .set(ChildPreviousNamePage(Index(0), Index(0)), childPreviousName).success.value
                  .set(ChildPreviousNamePage(Index(0), Index(1)), childPreviousName).success.value
                  .set(AddChildPreviousNamePage(Index(0)), true).success.value

              AddChildPreviousNamePage(Index(0))
                .navigate(waypoints, answers)
                .mustEqual(routes.ChildPreviousNameController.onPageLoad(waypoints, Index(0), Index(2)))
            }
          }

          "and there are two children, the second with a single previous name" - {

            "to Child Previous Name for child index 1, and name index 1" in {

              val answers =
                emptyUserAnswers
                  .set(ChildPreviousNamePage(Index(0), Index(0)), childPreviousName).success.value
                  .set(ChildPreviousNamePage(Index(1), Index(0)), childPreviousName).success.value
                  .set(AddChildPreviousNamePage(Index(1)), true).success.value

              AddChildPreviousNamePage(Index(1))
                .navigate(waypoints, answers)
                .mustEqual(routes.ChildPreviousNameController.onPageLoad(waypoints, Index(1), Index(1)))
            }
          }

          "and there are two children, the second with two previous names" - {

            "to Child Previous Name for child index 1, and name index 2" in {

              val answers =
                emptyUserAnswers
                  .set(ChildPreviousNamePage(Index(0), Index(0)), childPreviousName).success.value
                  .set(ChildPreviousNamePage(Index(1), Index(0)), childPreviousName).success.value
                  .set(ChildPreviousNamePage(Index(1), Index(1)), childPreviousName).success.value
                  .set(AddChildPreviousNamePage(Index(1)), true).success.value

              AddChildPreviousNamePage(Index(1))
                .navigate(waypoints, answers)
                .mustEqual(routes.ChildPreviousNameController.onPageLoad(waypoints, Index(1), Index(2)))
            }
          }
        }

        "when the answer is no" - {

          "to Child Biological Sex for the same child index" in {

            val answers =
              emptyUserAnswers
                .set(AddChildPreviousNamePage(Index(0)), false).success.value
                .set(AddChildPreviousNamePage(Index(1)), false).success.value

            AddChildPreviousNamePage(Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.ChildBiologicalSexController.onPageLoad(waypoints, Index(0)))

            AddChildPreviousNamePage(Index(1))
              .navigate(waypoints, answers)
              .mustEqual(routes.ChildBiologicalSexController.onPageLoad(waypoints, Index(1)))
          }
        }
      }
    }
  }
}