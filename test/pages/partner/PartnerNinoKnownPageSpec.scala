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

package pages.partner

import controllers.partner.routes
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class PartnerNinoKnownPageSpec extends PageBehaviours {

  "PartnerNinoKnownPage" - {

    beRetrievable[Boolean](PartnerNinoKnownPage)

    beSettable[Boolean](PartnerNinoKnownPage)

    beRemovable[Boolean](PartnerNinoKnownPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Partner NINO when the answer is yes" in {

          val answers = emptyUserAnswers.set(PartnerNinoKnownPage, true).success.value

          PartnerNinoKnownPage
            .navigate(waypoints, answers).route
            .mustEqual(routes.PartnerNinoController.onPageLoad(waypoints))
        }

        "to Partner Date of Birth when the answer is no" in {

          val answers = emptyUserAnswers.set(PartnerNinoKnownPage, false).success.value

          PartnerNinoKnownPage
            .navigate(waypoints, answers).route
            .mustEqual(routes.PartnerDateOfBirthController.onPageLoad(waypoints))
        }
      }
    }
  }
}
