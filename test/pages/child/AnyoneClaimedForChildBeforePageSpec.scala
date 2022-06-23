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
import models.AnyoneClaimedForChildBefore._
import models.{AnyoneClaimedForChildBefore, Index, PreviousClaimantAddress, PreviousClaimantName}
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class AnyoneClaimedForChildBeforePageSpec extends PageBehaviours {

  private val previousClaimantName    = PreviousClaimantName(None, "first", None, "last")
  private val previousClaimantAddress = PreviousClaimantAddress("line 1", None, None, "postcode")

  "AnyoneClaimedForChildBeforePage" - {

    beRetrievable[AnyoneClaimedForChildBefore](AnyoneClaimedForChildBeforePage(index))

    beSettable[AnyoneClaimedForChildBefore](AnyoneClaimedForChildBeforePage(index))

    beRemovable[AnyoneClaimedForChildBefore](AnyoneClaimedForChildBeforePage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Previous Claimant Name with the same index when the answer is Someone Else" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), SomeoneElse).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), SomeoneElse).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.PreviousClaimantNameController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.PreviousClaimantNameController.onPageLoad(waypoints, Index(1)))
        }

        "to Adopting Child with the same index when the answer is Applicant" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), Applicant).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), Applicant).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(1)))
        }

        "to Adopting Child with the same index when the answer is Partner" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), Partner).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), Partner).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(1)))
        }

        "to Adopting Child with the same index when the answer is No" in {

          val answers =
            emptyUserAnswers
              .set(AnyoneClaimedForChildBeforePage(Index(0)), No).success.value
              .set(AnyoneClaimedForChildBeforePage(Index(1)), No).success.value

          AnyoneClaimedForChildBeforePage(Index(0))
            .navigate(waypoints, answers)
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(0)))

          AnyoneClaimedForChildBeforePage(Index(1))
            .navigate(waypoints, answers)
            .mustEqual(routes.AdoptingChildController.onPageLoad(waypoints, Index(1)))
        }
      }

      "when the current waypoint is Check Child Details" - {

        def waypoints(index: Index) =
          EmptyWaypoints.setNextWaypoint(CheckChildDetailsPage(index).waypoint)

        "when the answer is Applicant" - {

          "to Check Child Details with the current waypoint removed" in {

            val answers =
              emptyUserAnswers
                .set(AnyoneClaimedForChildBeforePage(Index(0)), Applicant).success.value
                .set(AnyoneClaimedForChildBeforePage(Index(1)), Applicant).success.value

            AnyoneClaimedForChildBeforePage(Index(0))
              .navigate(waypoints(Index(0)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            AnyoneClaimedForChildBeforePage(Index(1))
              .navigate(waypoints(Index(1)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }
        }

        "when the answer is Partner" - {

          "to Check Child Details with the current waypoint removed" in {

            val answers =
              emptyUserAnswers
                .set(AnyoneClaimedForChildBeforePage(Index(0)), Partner).success.value
                .set(AnyoneClaimedForChildBeforePage(Index(1)), Partner).success.value

            AnyoneClaimedForChildBeforePage(Index(0))
              .navigate(waypoints(Index(0)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            AnyoneClaimedForChildBeforePage(Index(1))
              .navigate(waypoints(Index(1)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }
        }

        "when the answer is Someone Else" - {

          "to Check Child Details with the current waypoint removed when Previous ClaimantName has been answered" in {

            val answers =
              emptyUserAnswers
                .set(AnyoneClaimedForChildBeforePage(Index(0)), Applicant).success.value
                .set(AnyoneClaimedForChildBeforePage(Index(1)), Applicant).success.value
                .set(PreviousClaimantNamePage(Index(0)), previousClaimantName).success.value
                .set(PreviousClaimantNamePage(Index(1)), previousClaimantName).success.value

            AnyoneClaimedForChildBeforePage(Index(0))
              .navigate(waypoints(Index(0)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            AnyoneClaimedForChildBeforePage(Index(1))
              .navigate(waypoints(Index(1)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }

          "to Previous Claimant Name when it has not already been answered" in {

            val answers =
              emptyUserAnswers
                .set(AnyoneClaimedForChildBeforePage(Index(0)), SomeoneElse).success.value
                .set(AnyoneClaimedForChildBeforePage(Index(1)), SomeoneElse).success.value

            AnyoneClaimedForChildBeforePage(Index(0))
              .navigate(waypoints(Index(0)), answers)
              .mustEqual(routes.PreviousClaimantNameController.onPageLoad(waypoints(Index(0)), Index(0)))

            AnyoneClaimedForChildBeforePage(Index(1))
              .navigate(waypoints(Index(1)), answers)
              .mustEqual(routes.PreviousClaimantNameController.onPageLoad(waypoints(Index(1)), Index(1)))
          }
        }

        "when the answer is No" - {

          "to Check Child Details with the current waypoint removed" in {

            val answers =
              emptyUserAnswers
                .set(AnyoneClaimedForChildBeforePage(Index(0)), No).success.value
                .set(AnyoneClaimedForChildBeforePage(Index(1)), No).success.value

            AnyoneClaimedForChildBeforePage(Index(0))
              .navigate(waypoints(Index(0)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(0)))

            AnyoneClaimedForChildBeforePage(Index(1))
              .navigate(waypoints(Index(1)), answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(EmptyWaypoints, Index(1)))
          }
        }
      }
    }

    "must remove Previous Claimant details when the answer is Applicant" - {

      val answers =
        emptyUserAnswers
          .set(PreviousClaimantNamePage(Index(0)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(0)), previousClaimantAddress).success.value
          .set(PreviousClaimantNamePage(Index(1)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(1)), previousClaimantAddress).success.value

      "for index 0" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(0)), Applicant).success.value

        result.get(PreviousClaimantNamePage(Index(0))) must not be defined
        result.get(PreviousClaimantAddressPage(Index(0))) must not be defined
        result.get(PreviousClaimantNamePage(Index(1))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(1))) mustBe defined
      }

      "for index 1" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(1)), Applicant).success.value

        result.get(PreviousClaimantNamePage(Index(0))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(0))) mustBe defined
        result.get(PreviousClaimantNamePage(Index(1))) must not be defined
        result.get(PreviousClaimantAddressPage(Index(1))) must not be defined
      }
    }

    "must remove Previous Claimant details when the answer is Partner" - {

      val answers =
        emptyUserAnswers
          .set(PreviousClaimantNamePage(Index(0)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(0)), previousClaimantAddress).success.value
          .set(PreviousClaimantNamePage(Index(1)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(1)), previousClaimantAddress).success.value

      "for index 0" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(0)), Partner).success.value

        result.get(PreviousClaimantNamePage(Index(0))) must not be defined
        result.get(PreviousClaimantAddressPage(Index(0))) must not be defined
        result.get(PreviousClaimantNamePage(Index(1))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(1))) mustBe defined
      }

      "for index 1" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(1)), Partner).success.value

        result.get(PreviousClaimantNamePage(Index(0))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(0))) mustBe defined
        result.get(PreviousClaimantNamePage(Index(1))) must not be defined
        result.get(PreviousClaimantAddressPage(Index(1))) must not be defined
      }
    }

    "must remove Previous Claimant details when the answer is No" - {

      val answers =
        emptyUserAnswers
          .set(PreviousClaimantNamePage(Index(0)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(0)), previousClaimantAddress).success.value
          .set(PreviousClaimantNamePage(Index(1)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(1)), previousClaimantAddress).success.value

      "for index 0" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(0)), No).success.value

        result.get(PreviousClaimantNamePage(Index(0))) must not be defined
        result.get(PreviousClaimantAddressPage(Index(0))) must not be defined
        result.get(PreviousClaimantNamePage(Index(1))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(1))) mustBe defined
      }

      "for index 1" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(1)), No).success.value

        result.get(PreviousClaimantNamePage(Index(0))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(0))) mustBe defined
        result.get(PreviousClaimantNamePage(Index(1))) must not be defined
        result.get(PreviousClaimantAddressPage(Index(1))) must not be defined
      }
    }

    "must not remove Previous Claimant details when the answer is Someone Else" - {

      val answers =
        emptyUserAnswers
          .set(PreviousClaimantNamePage(Index(0)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(0)), previousClaimantAddress).success.value
          .set(PreviousClaimantNamePage(Index(1)), previousClaimantName).success.value
          .set(PreviousClaimantAddressPage(Index(1)), previousClaimantAddress).success.value

      "for index 0" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(0)), SomeoneElse).success.value

        result.get(PreviousClaimantNamePage(Index(0))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(0))) mustBe defined
        result.get(PreviousClaimantNamePage(Index(1))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(1))) mustBe defined
      }

      "for index 1" in {

        val result = answers.set(AnyoneClaimedForChildBeforePage(Index(1)), SomeoneElse).success.value

        result.get(PreviousClaimantNamePage(Index(0))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(0))) mustBe defined
        result.get(PreviousClaimantNamePage(Index(1))) mustBe defined
        result.get(PreviousClaimantAddressPage(Index(1))) mustBe defined
      }
    }
  }
}
