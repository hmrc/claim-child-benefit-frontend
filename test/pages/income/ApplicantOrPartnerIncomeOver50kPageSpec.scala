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

package pages.income

import controllers.income.routes
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class ApplicantOrPartnerIncomeOver50kPageSpec extends PageBehaviours {

  "ApplicantOrPartnerIncomeOver50kPage" - {

    beRetrievable[Boolean](ApplicantOrPartnerIncomeOver50kPage)

    beSettable[Boolean](ApplicantOrPartnerIncomeOver50kPage)

    beRemovable[Boolean](ApplicantOrPartnerIncomeOver50kPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Applicant or Partner Income Over 60k when the answer is yes" in {

          val answers = emptyUserAnswers.set(ApplicantOrPartnerIncomeOver50kPage, true).success.value

          ApplicantOrPartnerIncomeOver50kPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantOrPartnerIncomeOver60kController.onPageLoad(waypoints))
        }

        "to Applicant or Partner Benefits when the answer is no" in {

          val answers = emptyUserAnswers.set(ApplicantOrPartnerIncomeOver50kPage, false).success.value

          ApplicantOrPartnerIncomeOver50kPage
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantOrPartnerBenefitsController.onPageLoad(waypoints))
        }
      }
    }
  }
}
