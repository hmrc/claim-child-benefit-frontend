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
import pages.behaviours.PageBehaviours
import pages.{EmptyWaypoints, applicant}

class AddApplicantPreviousFamilyNamePageSpec extends PageBehaviours {

  "AddApplicantPreviousFamilyNamePage" - {

    beRetrievable[Boolean](AddApplicantPreviousFamilyNamePage)

    beSettable[Boolean](AddApplicantPreviousFamilyNamePage)

    beRemovable[Boolean](AddApplicantPreviousFamilyNamePage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Applicant Previous Family Name for the next index when the answer is yes" in {

          val answers =
            emptyUserAnswers
              .set(ApplicantPreviousFamilyNamePage(Index(0)), "name").success.value
              .set(AddApplicantPreviousFamilyNamePage, true).success.value

          AddApplicantPreviousFamilyNamePage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantPreviousFamilyNameController.onPageLoad(waypoints, Index(1)))
        }

        "to Applicant NINO Known when the answer is no" in {

          val answers =
            emptyUserAnswers
              .set(applicant.ApplicantPreviousFamilyNamePage(Index(0)), "name").success.value
              .set(AddApplicantPreviousFamilyNamePage, false).success.value

          AddApplicantPreviousFamilyNamePage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantNinoKnownController.onPageLoad(waypoints))
        }
      }
    }
  }
}
