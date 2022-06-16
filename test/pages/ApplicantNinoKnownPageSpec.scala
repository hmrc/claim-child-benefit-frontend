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

class ApplicantNinoKnownPageSpec extends PageBehaviours {

  "ApplicantNinoKnownPage" - {

    beRetrievable[Boolean](ApplicantNinoKnownPage)

    beSettable[Boolean](ApplicantNinoKnownPage)

    beRemovable[Boolean](ApplicantNinoKnownPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Applicant NINO when the answer is yes" in {

          val answers = emptyUserAnswers.set(ApplicantNinoKnownPage, true).success.value

          ApplicantNinoKnownPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantNinoController.onPageLoad(waypoints))
        }

        "to Applicant Date of Birth when the answer is no" in {

          val answers = emptyUserAnswers.set(ApplicantNinoKnownPage, false).success.value

          ApplicantNinoKnownPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantDateOfBirthController.onPageLoad(waypoints))
        }
      }
    }
  }
}
