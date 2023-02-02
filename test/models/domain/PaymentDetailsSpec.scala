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

package models.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class PaymentDetailsSpec extends AnyFreeSpec with Matchers {

  ".writes" - {

    "must write Bank Details" in {

      val accountHolder = ClaimantAccountHolder
      val bankAccount = BankAccount("123456", "12345678")
      val bankDetails = BankDetails(accountHolder, bankAccount)

      val expectedJson = Json.obj(
        "accountHolder" -> Json.obj(
          "accountHolderType" -> "CLAIMANT"
        ),
        "bankAccount" -> Json.obj(
          "sortCode" -> "123456",
          "accountNumber" -> "12345678"
        )
      )

      Json.toJson[PaymentDetails](bankDetails) mustEqual expectedJson
    }

    "must write Building Society Details" in {

      val accountHolder = ClaimantAccountHolder
      val buildingSociety = BuildingSocietyAccount("1234", "roll number")
      val buildingSocietyDetails = BuildingSocietyDetails(accountHolder, buildingSociety)

      val expectedJson = Json.obj(
        "accountHolder" -> Json.obj(
          "accountHolderType" -> "CLAIMANT"
        ),
        "buildingSocietyDetails" -> Json.obj(
          "buildingSociety" -> "1234",
          "rollNumber" -> "roll number"
        )
      )

      Json.toJson[PaymentDetails](buildingSocietyDetails) mustEqual expectedJson
    }
  }
}
