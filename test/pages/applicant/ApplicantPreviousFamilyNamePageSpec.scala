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


class ApplicantPreviousFamilyNamePageSpec extends PageBehaviours {

  "ApplicantPreviousFamilyNamePage" - {

    beRetrievable[String](ApplicantPreviousFamilyNamePage(Index(0)))

    beSettable[String](applicant.ApplicantPreviousFamilyNamePage(Index(0)))

    beRemovable[String](applicant.ApplicantPreviousFamilyNamePage(Index(0)))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Add Previous Family name" in {

          applicant.ApplicantPreviousFamilyNamePage(Index(0))
            .navigate(waypoints, emptyUserAnswers).route
            .mustEqual(routes.AddApplicantPreviousFamilyNameController.onPageLoad(waypoints))
        }
      }
    }
  }
}
