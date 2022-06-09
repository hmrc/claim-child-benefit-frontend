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
import models.BankAccountType
import pages.behaviours.PageBehaviours

class BankAccountTypeSpec extends PageBehaviours {

  "BankAccountTypePage" - {

    beRetrievable[BankAccountType](BankAccountTypePage)

    beSettable[BankAccountType](BankAccountTypePage)

    beRemovable[BankAccountType](BankAccountTypePage)

    "must navigate" - {

      "when there are no waypoints" - {

        val waypoints = EmptyWaypoints

        "to Bank Account Details when the answer is Bank" in {

          val answers = emptyUserAnswers.set(BankAccountTypePage, BankAccountType.Bank).success.value

          BankAccountTypePage
            .navigate(waypoints, answers)
            .mustEqual(routes.BankAccountDetailsController.mustEqual(waypoints))
        }

        "to Building Society Account Details when the answer is Bank" in {

          val answers = emptyUserAnswers.set(BankAccountTypePage, BankAccountType.BuildingSociety).success.value

          BankAccountTypePage
            .navigate(waypoints, answers)
            .mustEqual(routes.BuildingSocietyAccountDetailsController.mustEqual(waypoints))
        }
      }
    }
  }
}
