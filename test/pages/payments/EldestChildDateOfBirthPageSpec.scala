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

import controllers.payments.routes
import org.scalacheck.Arbitrary
import pages.EmptyWaypoints
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class EldestChildDateOfBirthPageSpec extends PageBehaviours {

  "EldestChildDateOfBirthPage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](EldestChildDateOfBirthPage)

    beSettable[LocalDate](EldestChildDateOfBirthPage)

    beRemovable[LocalDate](EldestChildDateOfBirthPage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Want to be paid to existing account" in {

          EldestChildDateOfBirthPage
            .navigate(waypoints, emptyUserAnswers)
            .mustEqual(routes.WantToBePaidToExistingAccountController.onPageLoad(waypoints))
        }
      }
    }
  }
}
