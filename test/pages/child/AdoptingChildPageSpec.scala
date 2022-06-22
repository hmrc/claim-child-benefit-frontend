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
import models.Index
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

class AdoptingChildPageSpec extends PageBehaviours {

  "AdoptingChildPage" - {

    beRetrievable[Boolean](AdoptingChildPage(index))

    beSettable[Boolean](AdoptingChildPage(index))

    beRemovable[Boolean](AdoptingChildPage(index))

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "when the birth was registered in England" - {

          "to Check Child Details for the same index" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), England).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), England).success.value

            AdoptingChildPage(Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(0)))

            AdoptingChildPage(Index(1))
              .navigate(waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(1)))
          }
        }

        "when the birth was registered in Wales" - {

          "to Check Child Details for the same index" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Wales).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Wales).success.value

            AdoptingChildPage(Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(0)))

            AdoptingChildPage(Index(1))
              .navigate(waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(1)))
          }
        }

        "when the birth was registered in Scotland" - {

          "to Check Child Details for the same index" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Scotland).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Scotland).success.value

            AdoptingChildPage(Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(0)))

            AdoptingChildPage(Index(1))
              .navigate(waypoints, answers)
              .mustEqual(routes.CheckChildDetailsController.onPageLoad(waypoints, Index(1)))
          }
        }

        "when the birth was registered in another country" - {

          "to Included Documents for the same index" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Other).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Other).success.value

            AdoptingChildPage(Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.IncludedDocumentsController.onPageLoad(waypoints, Index(0)))

            AdoptingChildPage(Index(1))
              .navigate(waypoints, answers)
              .mustEqual(routes.IncludedDocumentsController.onPageLoad(waypoints, Index(1)))
          }
        }

        "when the birth registration country is unknown" - {

          "to Included Documents for the same index" in {

            val answers =
              emptyUserAnswers
                .set(ChildBirthRegistrationCountryPage(Index(0)), Unknown).success.value
                .set(ChildBirthRegistrationCountryPage(Index(1)), Unknown).success.value

            AdoptingChildPage(Index(0))
              .navigate(waypoints, answers)
              .mustEqual(routes.IncludedDocumentsController.onPageLoad(waypoints, Index(0)))

            AdoptingChildPage(Index(1))
              .navigate(waypoints, answers)
              .mustEqual(routes.IncludedDocumentsController.onPageLoad(waypoints, Index(1)))
          }
        }
      }
    }
  }
}
