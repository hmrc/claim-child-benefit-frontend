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
import pages.behaviours.PageBehaviours

class AnyChildLivedWithOthersPageSpec extends PageBehaviours {

  "AnyChildLivedWithOthersPage" - {

    beRetrievable[Boolean](AnyChildLivedWithOthersPage)

    beSettable[Boolean](AnyChildLivedWithOthersPage)

    beRemovable[Boolean](AnyChildLivedWithOthersPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Use Print and Post Form when the answer is yes" in {

          val answers = emptyUserAnswers.set(AnyChildLivedWithOthersPage, true).success.value

          AnyChildLivedWithOthersPage
            .navigate(waypoints, answers)
            .mustEqual(routes.UsePrintAndPostFormController.onPageLoad(waypoints))
        }

        "to Any Child Lived With Others when the answer is no" in {

          val answers = emptyUserAnswers.set(AnyChildLivedWithOthersPage, false).success.value

          AnyChildLivedWithOthersPage
            .navigate(waypoints, answers)
            .mustEqual(routes.RelationshipStatusController.onPageLoad(waypoints))
        }
      }
    }
  }
}
