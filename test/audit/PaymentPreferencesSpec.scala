/*
 * Copyright 2024 HM Revenue & Customs
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

import models.{BankAccountInsightsResponseModel, PaymentFrequency}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class PaymentPreferencesSpec extends AnyFreeSpec with Matchers {

  private val dateOfBirth = LocalDate.of(2020, 12, 31)

  "Weekly payment preference must serialise to JSON" - {

    "when bank account details and eldest child are present" in {

      val preference = Weekly(
        Some(BankAccount("applicant", "first", "last", "000000", "00000000", Some(BankAccountInsightsResponseModel("correlation", 0, "foo")))),
        Some(EldestChild(ChildName("first", None, "last"), dateOfBirth))
      )
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.Weekly.toString,
        "account" -> Json.obj(
          "holder" -> "applicant",
          "firstName" -> "first",
          "lastName" -> "last",
          "sortCode" -> "000000",
          "accountNumber" -> "00000000",
          "bankAccountInsightsResult" -> Json.obj(
            "correlationId" -> "correlation",
            "riskScore" -> 0,
            "reason" -> "foo"
          )
        ),
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName" -> "last"
          ),
          "dateOfBirth" -> "2020-12-31"
        )
      )
    }

    "when bank account details and eldest child are not present" in {

      val preference = Weekly(None, None)
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.Weekly.toString,
        "account" -> "no suitable account"
      )
    }
  }

  "Every four weeks payment preference must serialise to JSON" - {

    "when bank account details and eldest child are present" in {

      val preference = EveryFourWeeks(
        Some(BankAccount("applicant", "first", "last", "000000", "00000000", Some(BankAccountInsightsResponseModel("correlation", 0, "foo")))),
        Some(EldestChild(ChildName("first", None, "last"), dateOfBirth))
      )
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.EveryFourWeeks.toString,
        "account" -> Json.obj(
          "holder" -> "applicant",
          "firstName" -> "first",
          "lastName" -> "last",
          "sortCode" -> "000000",
          "accountNumber" -> "00000000",
          "bankAccountInsightsResult" -> Json.obj(
            "correlationId" -> "correlation",
            "riskScore" -> 0,
            "reason" -> "foo"
          )
        ),
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName" -> "last"
          ),
          "dateOfBirth" -> "2020-12-31"
        )
      )
    }

    "when bank account details and eldest child are not present" in {

      val preference = EveryFourWeeks(None, None)
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.EveryFourWeeks.toString,
        "account" -> "no suitable account"
      )
    }
  }

  "Do not pay payment preference must serialise to JSON" - {

    "when eldest child is present" in {

      val preference = DoNotPay(Some(EldestChild(ChildName("first", None, "last"), dateOfBirth)))

      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> false,
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName" -> "last"
          ),
          "dateOfBirth" -> "2020-12-31"
        )
      )
    }

    "when eldest child is not present" in {

      val preference = DoNotPay(None)

      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> false
      )
    }
  }

  "Existing account payment preference must serialise to JSON" - {

    "when eldest child details are present" in {

      val preference = ExistingAccount(Some(EldestChild(ChildName("first", None, "last"), dateOfBirth)))
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName" -> "last"
          ),
          "dateOfBirth" -> "2020-12-31"
        ),
        "account" -> "use existing account"
      )
    }

    "when eldest child details are not present" in {

      val preference = ExistingAccount(None)
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "account" -> "use existing account"
      )
    }
  }
}
