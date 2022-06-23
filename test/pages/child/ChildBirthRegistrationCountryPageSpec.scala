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
import models.{ChildBirthRegistrationCountry, ChildScottishBirthCertificateDetails, Index}
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class ChildBirthRegistrationCountryPageSpec extends PageBehaviours {

  private val scottishBirthCertificateDetails = ChildScottishBirthCertificateDetails("123", "2022", "456")

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

      "when the current waypoint is Check Child Details" - {

        def waypoints(index: Index) =
          EmptyWaypoints.setNextWaypoint(CheckChildDetailsPage(index).waypoint)

        val index0Waypoints = waypoints(Index(0))
        val index1Waypoints = waypoints(Index(1))

        "and the answer is England" - {

          "to Check Child Details with the current waypoint removed when Birth Certificate System Number has been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), England).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), England).success.value
                .set(ChildBirthCertificateSystemNumberPage(Index(0)), "000000000").success.value
                .set(ChildBirthCertificateSystemNumberPage(Index(1)), "000000000").success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }

          "to Birth Certificate System Number when it has not already been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), England).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), England).success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(index0Waypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(index1Waypoints, Index(1)))
          }
        }

        "and the answer is Wales" - {

          "to Check Child Details with the current waypoint removed when Birth Certificate System Number has been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Wales).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Wales).success.value
                .set(ChildBirthCertificateSystemNumberPage(Index(0)), "000000000").success.value
                .set(ChildBirthCertificateSystemNumberPage(Index(1)), "000000000").success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }

          "to Birth Certificate System Number when it has not already been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Wales).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Wales).success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(index0Waypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.ChildBirthCertificateSystemNumberController.onPageLoad(index1Waypoints, Index(1)))
          }
        }

        "and the answer is Scotland" - {

          "to Check Child Details with the current waypoint removed when Scottish Birth Certificate Details has been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Scotland).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Scotland).success.value
                .set(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBirthCertificateDetails).success.value
                .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }

          "to Birth Certificate System Number when it has not already been answered" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Scotland).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Scotland).success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.ChildScottishBirthCertificateDetailsController.onPageLoad(index0Waypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.ChildScottishBirthCertificateDetailsController.onPageLoad(index1Waypoints, Index(1)))
          }
        }

        "and the answer is Other" - {

          "to Check Child Details with the current waypoint removed" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Other).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Other).success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }
        }

        "and the answer is Unknown" - {

          "to Check Child Details with the current waypoint removed" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Unknown).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Unknown).success.value

            ChildBirthRegistrationCountryPage(Index(0))
              .navigate(index0Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            ChildBirthRegistrationCountryPage(Index(1))
              .navigate(index1Waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }
        }
      }
    }

    "must remove Scottish Birth Certificate Details when the answer is England" in {

      val answers =
        emptyUserAnswers
          .set(ChildBirthCertificateSystemNumberPage(Index(0)), "000000000").success.value
          .set(ChildBirthCertificateSystemNumberPage(Index(1)), "000000000").success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBirthCertificateDetails).success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value

      val result =
        answers
          .set(ChildBirthRegistrationCountryPage(Index(0)), England).success.value
          .set(ChildBirthRegistrationCountryPage(Index(1)), England).success.value

      result.get(ChildBirthCertificateSystemNumberPage(Index(0))) mustBe defined
      result.get(ChildBirthCertificateSystemNumberPage(Index(1))) mustBe defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(0))) must not be defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(1))) must not be defined
    }

    "must remove Scottish Birth Certificate Details when the answer is Wales" in {

      val answers =
        emptyUserAnswers
          .set(ChildBirthCertificateSystemNumberPage(Index(0)), "000000000").success.value
          .set(ChildBirthCertificateSystemNumberPage(Index(1)), "000000000").success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBirthCertificateDetails).success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value

      val result =
        answers
          .set(ChildBirthRegistrationCountryPage(Index(0)), Wales).success.value
          .set(ChildBirthRegistrationCountryPage(Index(1)), Wales).success.value

      result.get(ChildBirthCertificateSystemNumberPage(Index(0))) mustBe defined
      result.get(ChildBirthCertificateSystemNumberPage(Index(1))) mustBe defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(0))) must not be defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(1))) must not be defined
    }

    "must remove Birth Certificate System Number when the answer is Scotland" in {

      val answers =
        emptyUserAnswers
          .set(ChildBirthCertificateSystemNumberPage(Index(0)), "000000000").success.value
          .set(ChildBirthCertificateSystemNumberPage(Index(1)), "000000000").success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBirthCertificateDetails).success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value

      val result =
        answers
          .set(ChildBirthRegistrationCountryPage(Index(0)), Scotland).success.value
          .set(ChildBirthRegistrationCountryPage(Index(1)), Scotland).success.value

      result.get(ChildBirthCertificateSystemNumberPage(Index(0))) must not be defined
      result.get(ChildBirthCertificateSystemNumberPage(Index(1))) must not be defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(0))) mustBe defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(1))) mustBe defined
    }

    "must remove Birth Certificate System Number and Scottish Birth Certificate Details when the answer is Other" in {

      val answers =
        emptyUserAnswers
          .set(ChildBirthCertificateSystemNumberPage(Index(0)), "000000000").success.value
          .set(ChildBirthCertificateSystemNumberPage(Index(1)), "000000000").success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBirthCertificateDetails).success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value

      val result =
        answers
          .set(ChildBirthRegistrationCountryPage(Index(0)), Other).success.value
          .set(ChildBirthRegistrationCountryPage(Index(1)), Other).success.value

      result.get(ChildBirthCertificateSystemNumberPage(Index(0))) must not be defined
      result.get(ChildBirthCertificateSystemNumberPage(Index(1))) must not be defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(0))) must not be defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(1))) must not be defined
    }

    "must remove Birth Certificate System Number and Scottish Birth Certificate Details when the answer is Unknown" in {

      val answers =
        emptyUserAnswers
          .set(ChildBirthCertificateSystemNumberPage(Index(0)), "000000000").success.value
          .set(ChildBirthCertificateSystemNumberPage(Index(1)), "000000000").success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBirthCertificateDetails).success.value
          .set(ChildScottishBirthCertificateDetailsPage(Index(1)), scottishBirthCertificateDetails).success.value

      val result =
        answers
          .set(ChildBirthRegistrationCountryPage(Index(0)), Unknown).success.value
          .set(ChildBirthRegistrationCountryPage(Index(1)), Unknown).success.value

      result.get(ChildBirthCertificateSystemNumberPage(Index(0))) must not be defined
      result.get(ChildBirthCertificateSystemNumberPage(Index(1))) must not be defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(0))) must not be defined
      result.get(ChildScottishBirthCertificateDetailsPage(Index(1))) must not be defined
    }
  }
}
