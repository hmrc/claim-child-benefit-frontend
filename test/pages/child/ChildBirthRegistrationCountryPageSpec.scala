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

package pages.child

import controllers.child.routes
import models.ChildBirthRegistrationCountry._
import models.{ChildBirthRegistrationCountry, Index}
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class ChildBirthRegistrationCountryPageSpec extends PageBehaviours {

  "ChildBirthRegistrationCountryPage" - {

    beRetrievable[ChildBirthRegistrationCountry](ChildBirthRegistrationCountryPage(index))

    beSettable[ChildBirthRegistrationCountry](ChildBirthRegistrationCountryPage(index))

    beRemovable[ChildBirthRegistrationCountry](ChildBirthRegistrationCountryPage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Child Birth Certificate System Number for the same index when the answer is England" in {

          val answers =
            emptyUserAnswers
              .set(ChildBirthRegistrationCountryPage(Index(0)), England).success.value
              .set(ChildBirthRegistrationCountryPage(Index(1)), England).success.value

          ChildBirthRegistrationCountryPage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(waypoints, Index(0)))

          ChildBirthRegistrationCountryPage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(waypoints, Index(1)))
        }

        "to Child Birth Certificate System Number for the same index when the answer is Wales" in {

          val answers =
            emptyUserAnswers
              .set(ChildBirthRegistrationCountryPage(Index(0)), Wales).success.value
              .set(ChildBirthRegistrationCountryPage(Index(1)), Wales).success.value

          ChildBirthRegistrationCountryPage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(waypoints, Index(0)))

          ChildBirthRegistrationCountryPage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(waypoints, Index(1)))
        }

        "to Child Scottish Birth Certificate Details for the same index when the answer is Scotland" in {

          val answers =
            emptyUserAnswers
              .set(ChildBirthRegistrationCountryPage(Index(0)), Scotland).success.value
              .set(ChildBirthRegistrationCountryPage(Index(1)), Scotland).success.value

          ChildBirthRegistrationCountryPage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildScottishBirthCertificateDetailsController.onPageLoad(waypoints, Index(0)))

          ChildBirthRegistrationCountryPage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.ChildScottishBirthCertificateDetailsController.onPageLoad(waypoints, Index(1)))
        }

        "to Applicant Relationship to Child for the same index when the answer is Other" in {

          val answers =
            emptyUserAnswers
              .set(ChildBirthRegistrationCountryPage(Index(0)), Other).success.value
              .set(ChildBirthRegistrationCountryPage(Index(1)), Other).success.value

          ChildBirthRegistrationCountryPage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantRelationshipToChildController.onPageLoad(waypoints, Index(0)))

          ChildBirthRegistrationCountryPage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantRelationshipToChildController.onPageLoad(waypoints, Index(1)))
        }


        "to Applicant Relationship to Child for the same index when the answer is Unknown" in {

          val answers =
            emptyUserAnswers
              .set(ChildBirthRegistrationCountryPage(Index(0)), Unknown).success.value
              .set(ChildBirthRegistrationCountryPage(Index(1)), Unknown).success.value

          ChildBirthRegistrationCountryPage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantRelationshipToChildController.onPageLoad(waypoints, Index(0)))

          ChildBirthRegistrationCountryPage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.ApplicantRelationshipToChildController.onPageLoad(waypoints, Index(1)))
        }
      }
    }
  }
}
