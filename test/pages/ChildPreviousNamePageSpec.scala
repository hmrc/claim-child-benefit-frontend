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
import models.{ChildPreviousName, Index}
import pages.behaviours.PageBehaviours

class ChildPreviousNamePageSpec extends PageBehaviours {

  "ChildPreviousNamePage" - {

    beRetrievable[ChildPreviousName](ChildPreviousNamePage(index, index))

    beSettable[ChildPreviousName](ChildPreviousNamePage(index, index))

    beRemovable[ChildPreviousName](ChildPreviousNamePage(index, index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Add Child Previous Name for the same child index" in {

          ChildPreviousNamePage(Index(0), Index(0))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(0)))

          ChildPreviousNamePage(Index(0), Index(1))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(0)))

          ChildPreviousNamePage(Index(1), Index(0))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(1)))

          ChildPreviousNamePage(Index(1), Index(1))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.AddChildPreviousNameController.onPageLoad(waypoints, Index(1)))
        }
      }
    }
  }
}
