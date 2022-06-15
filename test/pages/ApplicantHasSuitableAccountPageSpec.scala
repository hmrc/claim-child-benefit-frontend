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

class ApplicantHasSuitableAccountPageSpec extends PageBehaviours {

  "ApplicantHasSuitableAccountPage" - {

    beRetrievable[Boolean](ApplicantHasSuitableAccountPage)

    beSettable[Boolean](ApplicantHasSuitableAccountPage)

    beRemovable[Boolean](ApplicantHasSuitableAccountPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Account in Applicant Name when the answer is yes" in {

          val answers = emptyUserAnswers.set(ApplicantHasSuitableAccountPage, true).success.value

          ApplicantHasSuitableAccountPage
            .navigate(waypoints, answers)
            .mustEqual(routes.AccountInApplicantsNameController.onPageLoad(waypoints))
        }

        // TODO: Update when next section is available
        "to Index when the answer is no" in {

          val answers = emptyUserAnswers.set(ApplicantHasSuitableAccountPage, false).success.value

          ApplicantHasSuitableAccountPage
            .navigate(waypoints, answers)
            .mustEqual(routes.IndexController.onPageLoad)
        }
      }
    }
  }
}
