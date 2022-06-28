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

class RemoveChildPageSpec extends PageBehaviours {

  "RemoveChildPage" - {

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Add Child when there is at least one child left" - {

          val answers = emptyUserAnswers.set(ChildNamePage(Index(0)), ChildName("first", None, "last")).success.value

          RemoveChildPage(Index(0))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AddChildController.onPageLoad(waypoints))

          RemoveChildPage(Index(1))
            .navigate(waypoints, answers).route
            .mustEqual(routes.AddChildController.onPageLoad(waypoints))
        }

        "to Child Name for index 0 when there are no children left" in {

          RemoveChildPage(Index(0))
            .navigate(waypoints, emptyUserAnswers).route
            .mustEqual(routes.ChildNameController.onPageLoad(waypoints, Index(0)))
        }
      }
    }
  }
}
