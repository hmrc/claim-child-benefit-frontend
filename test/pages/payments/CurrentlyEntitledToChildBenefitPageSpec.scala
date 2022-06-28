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

package pages.payments

import controllers.payments.routes
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class CurrentlyEntitledToChildBenefitPageSpec extends PageBehaviours {

  "CurrentlyEntitledToChildBenefitPage" - {

    beRetrievable[Boolean](CurrentlyEntitledToChildBenefitPage)

    beSettable[Boolean](CurrentlyEntitledToChildBenefitPage)

    beRemovable[Boolean](CurrentlyEntitledToChildBenefitPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Currently Receiving Child Benefit when the answer is yes" in {

          val answers = emptyUserAnswers.set(CurrentlyEntitledToChildBenefitPage, true).success.value

          CurrentlyEntitledToChildBenefitPage
            .navigate(waypoints, answers).route
            .mustEqual(routes.CurrentlyReceivingChildBenefitController.onPageLoad(waypoints))
        }

        "to Tax Charge Explanation when the answer is no" in {

          val answers = emptyUserAnswers.set(CurrentlyEntitledToChildBenefitPage, false).success.value

          CurrentlyEntitledToChildBenefitPage
            .navigate(waypoints, answers).route
            .mustEqual(routes.TaxChargeExplanationController.onPageLoad(waypoints))
        }
      }
    }
  }
}
