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

package audit

import audit.DownloadAuditEvent._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class DownloadAuditEventSpec extends AnyFreeSpec with Matchers {

  "Weekly payment preference must serialise to JSON" - {

    "when bank account details are present" in {

      val preference = Weekly(Some(BankAccount("applicant", "name", "000000", "00000000", None)))
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "frequency" -> "weekly",
        "account"   -> Json.obj(
          "holder"        -> "applicant",
          "accountName"   -> "name",
          "sortCode"      -> "000000",
          "accountNumber" -> "00000000"
        )
      )
    }

    "when bank account details are not present" in {

      val preference = Weekly(None)
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "frequency" -> "weekly",
        "account"   -> "no suitable account"
      )
    }
  }

  "Every four weeks payment preference must serialise to JSON" - {

    "when bank account details are present" in {

      val preference = EveryFourWeeks(Some(BankAccount("applicant", "name", "000000", "00000000", None)))
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "frequency" -> "every four weeks",
        "account"   -> Json.obj(
          "holder"        -> "applicant",
          "accountName"   -> "name",
          "sortCode"      -> "000000",
          "accountNumber" -> "00000000"
        )
      )
    }

    "when bank account details are not present" in {

      val preference = EveryFourWeeks(None)
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "frequency" -> "every four weeks",
        "account"   -> "no suitable account"
      )
    }
  }

  "Existing frequency payment preference must serialise to JSON" - {

    "when bank account details are present" in {

      val dateOfBirth = LocalDate.of(2020, 12, 31)

      val preference = ExistingFrequency(
        bankAccount = Some(BankAccount("applicant", "name", "000000", "00000000", None)),
        eldestChild = EldestChild(ChildName("first", None, "last"), dateOfBirth)
      )

      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "frequency" -> "use existing frequency",
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName"  -> "last"
          ),
          "dateOfBirth" -> "2020-12-31"
        ),
        "account"   -> Json.obj(
          "holder"        -> "applicant",
          "accountName"   -> "name",
          "sortCode"      -> "000000",
          "accountNumber" -> "00000000"
        )
      )
    }

    "when bank account details are not present" in {

      val dateOfBirth = LocalDate.of(2020, 12, 31)

      val preference = ExistingFrequency(
        bankAccount = None,
        eldestChild = EldestChild(ChildName("first", None, "last"), dateOfBirth)
      )

      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "frequency" -> "use existing frequency",
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName"  -> "last"
          ),
          "dateOfBirth" -> "2020-12-31"
        ),
        "account" -> "no suitable account"
      )
    }
  }
}
