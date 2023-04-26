/*
 * Copyright 2023 HM Revenue & Customs
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

package audit

import models.BankAccountInsightsResponseModel
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class AccountDetailsSpec extends AnyFreeSpec with Matchers {

  "bank accounts must serialise to JSON" in {

    val account = BankAccount("a", "b", "c", "d", "e", Some(BankAccountInsightsResponseModel("f", 0, "h")))
    val json = Json.toJson(account)

    json mustEqual Json.obj(
      "holder"-> "a",
      "firstName"-> "b",
      "lastName"-> "c",
      "sortCode"-> "d",
      "accountNumber"-> "e",
      "bankAccountInsightsResult" -> Json.obj(
        "correlationId" -> "f",
        "riskScore" -> 0,
        "reason" -> "h"
      )
    )
  }

  "building societies must serialise to JSON" in {

    val account = BuildingSociety("a", "b", "c", "d", "e")
    val json = Json.toJson(account)

    json mustEqual Json.obj(
      "holder" -> "a",
      "firstName" -> "b",
      "lastName" -> "c",
      "buildingSociety" -> "d",
      "rollNumber" -> "e"
    )
  }
}
