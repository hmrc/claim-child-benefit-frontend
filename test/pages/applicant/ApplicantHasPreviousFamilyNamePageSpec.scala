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

package pages.applicant

import controllers.applicant.routes
import models.Index
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class ApplicantHasPreviousFamilyNamePageSpec extends PageBehaviours {

  "ApplicantHasPreviousFamilyNamePage" - {

    beRetrievable[Boolean](ApplicantHasPreviousFamilyNamePage)

    beSettable[Boolean](ApplicantHasPreviousFamilyNamePage)

    beRemovable[Boolean](ApplicantHasPreviousFamilyNamePage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Applicant Previous Family Name when the answer is yes" in {

          val answers = emptyUserAnswers.set(ApplicantHasPreviousFamilyNamePage, true).success.value

          ApplicantHasPreviousFamilyNamePage
            .navigate(waypoints, answers).route
            .mustEqual(routes.ApplicantPreviousFamilyNameController.onPageLoad(waypoints, Index(0)))
        }

        "to Applicant Nino Known when the answer is no" in {

          val answers = emptyUserAnswers.set(ApplicantHasPreviousFamilyNamePage, false).success.value

          ApplicantHasPreviousFamilyNamePage
            .navigate(waypoints, answers).route
            .mustEqual(routes.ApplicantNinoKnownController.onPageLoad(waypoints))
        }
      }
    }
  }
}
