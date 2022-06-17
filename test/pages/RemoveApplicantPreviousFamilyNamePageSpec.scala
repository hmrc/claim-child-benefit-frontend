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
import models.Index
import pages.behaviours.PageBehaviours

class RemoveApplicantPreviousFamilyNamePageSpec extends PageBehaviours {

  "RemoveApplicantPreviousFamilyNamePage" - {

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Add Applicant Previous Family Name when there are some names left" in {

          val answers = emptyUserAnswers.set(ApplicantPreviousFamilyNamePage(Index(0)), "name").success.value

          RemoveApplicantPreviousFamilyNamePage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.AddApplicantPreviousFamilyNameController.onPageLoad(waypoints))
        }

        "to Applicant Has Previous Family Names when there are no names left" in {

          RemoveApplicantPreviousFamilyNamePage(Index(0))
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.ApplicantHasPreviousFamilyNameController.onPageLoad(waypoints))
        }
      }
    }
  }
}
