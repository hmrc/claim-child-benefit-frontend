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
import models.ApplicantName
import pages.behaviours.PageBehaviours

class ApplicantNamePageSpec extends PageBehaviours {

  "ApplicantNamePage" - {

    beRetrievable[ApplicantName](ApplicantNamePage)

    beSettable[ApplicantName](ApplicantNamePage)

    beRemovable[ApplicantName](ApplicantNamePage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Applicant Employment Status" in {

          ApplicantNamePage
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.RelationshipStatusController.onPageLoad(waypoints))
        }
      }
    }
  }
}
