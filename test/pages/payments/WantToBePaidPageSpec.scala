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

import controllers.applicant.{routes => applicantRoutes}
import controllers.payments.routes
import models.Benefits
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours
import pages.income.ApplicantOrPartnerBenefitsPage
import pages.{EmptyWaypoints, RelationshipStatusPage}

class WantToBePaidPageSpec extends PageBehaviours {

  private val qualifyingBenefitsGen = Gen.oneOf(Benefits.qualifyingBenefits)

  "WantToBePaidPage" - {

    beRetrievable[Boolean](WantToBePaidPage)

    beSettable[Boolean](WantToBePaidPage)

    beRemovable[Boolean](WantToBePaidPage)

    "must navigate" - {

      import models.RelationshipStatus._

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "and the answer is yes" - {

          val baseAnswers = emptyUserAnswers.set(WantToBePaidPage, true).success.value

          "to Want to be paid weekly" - {

            "when the user is Single" in {

              val answers = baseAnswers.set(RelationshipStatusPage, Single).success.value

              WantToBePaidPage
                .navigate(waypoints, answers)
                .mustEqual(routes.WantToBePaidWeeklyController.onPageLoad(waypoints))
            }

            "when the user is Divorced" in {

              val answers = baseAnswers.set(RelationshipStatusPage, Divorced).success.value

              WantToBePaidPage
                .navigate(waypoints, answers)
                .mustEqual(routes.WantToBePaidWeeklyController.onPageLoad(waypoints))
            }

            "when the user is Widowed" in {

              val answers = baseAnswers.set(RelationshipStatusPage, Widowed).success.value

              WantToBePaidPage
                .navigate(waypoints, answers)
                .mustEqual(routes.WantToBePaidWeeklyController.onPageLoad(waypoints))
            }

            "when the user is Separated" in {

              val answers = baseAnswers.set(RelationshipStatusPage, Separated).success.value

              WantToBePaidPage
                .navigate(waypoints, answers)
                .mustEqual(routes.WantToBePaidWeeklyController.onPageLoad(waypoints))
            }

            "when the user is Married and in receipt of qualifying benefits" in {

              val benefits = Set(qualifyingBenefitsGen.sample.value)

              val answers =
                baseAnswers
                  .set(RelationshipStatusPage, Married).success.value
                  .set(ApplicantOrPartnerBenefitsPage, benefits).success.value

              WantToBePaidPage
                .navigate(waypoints, answers)
                .mustEqual(routes.WantToBePaidWeeklyController.onPageLoad(waypoints))
            }
            
            "when the user is Cohabiting and in receipt of qualifying benefits" in {

              val benefits = Set(qualifyingBenefitsGen.sample.value)

              val answers =
                baseAnswers
                  .set(RelationshipStatusPage, Cohabiting).success.value
                  .set(ApplicantOrPartnerBenefitsPage, benefits).success.value

              WantToBePaidPage
                .navigate(waypoints, answers)
                .mustEqual(routes.WantToBePaidWeeklyController.onPageLoad(waypoints))
            }
          }
        }

        "when the answer is no" - {

          "to Applicant Has Previous Family Name" in {

            val answers = emptyUserAnswers.set(WantToBePaidPage, false).success.value

            WantToBePaidPage
              .navigate(waypoints, answers)
              .mustEqual(applicantRoutes.ApplicantHasPreviousFamilyNameController.onPageLoad(waypoints))
          }
        }
      }
    }
  }
}
