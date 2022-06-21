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
import models.{ChildName, Index}
import pages.behaviours.PageBehaviours

class AddChildPageSpec extends PageBehaviours {

  "AddChildPage" - {

    beRetrievable[Boolean](AddChildPage)

    beSettable[Boolean](AddChildPage)

    beRemovable[Boolean](AddChildPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints
        val childName = ChildName("first", None, "last")

        "when the answer is yes" - {

          "to Child Name for index 1 when there are already details for one child" in {

            val answers =
              emptyUserAnswers
                .set(ChildNamePage(Index(0)), childName).success.value
                .set(AddChildPage, true).success.value

            AddChildPage
              .navigate(waypoints, answers)
              .mustEqual(routes.ChildNameController.onPageLoad(waypoints, Index(1)))
          }

          "to Child Name for index 2 when there are already details for two children" in {

            val answers =
              emptyUserAnswers
                .set(ChildNamePage(Index(0)), childName).success.value
                .set(ChildNamePage(Index(1)), childName).success.value
                .set(AddChildPage, true).success.value

            AddChildPage
              .navigate(waypoints, answers)
              .mustEqual(routes.ChildNameController.onPageLoad(waypoints, Index(2)))
          }
        }

        "when the answer is no" - {

          "to Check Your Answers" in {

            val answers =emptyUserAnswers.set(AddChildPage, false).success.value

            AddChildPage
              .navigate(waypoints, answers)
              .mustEqual(routes.CheckYourAnswersController.onPageLoad)
          }
        }
      }
    }
  }
}
