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

import audit.DownloadAuditEvent._
import models.PaymentFrequency
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class DownloadAuditEventSpec extends AnyFreeSpec with Matchers {

  private val dateOfBirth = LocalDate.of(2020, 12, 31)

  "Weekly payment preference must serialise to JSON" - {

    "when bank account details and eldest child are present" in {

      val preference = Weekly(
        Some(BankAccount("applicant", "first", "last", "000000", "00000000")),
        Some(EldestChild(ChildName("first", None, "last"), dateOfBirth))
      )
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.Weekly.toString,
        "account"   -> Json.obj(
          "holder"        -> "applicant",
          "firstName"     -> "first",
          "lastName"      -> "last",
          "sortCode"      -> "000000",
          "accountNumber" -> "00000000"
        ),
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName"  -> "last"
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
        "frequency"    -> PaymentFrequency.Weekly.toString,
        "account"      -> "no suitable account"
      )
    }
  }

  "Every four weeks payment preference must serialise to JSON" - {

    "when bank account details and eldest child are present" in {

      val preference = EveryFourWeeks(
        Some(BankAccount("applicant", "first", "last", "000000", "00000000")),
        Some(EldestChild(ChildName("first", None, "last"), dateOfBirth))
      )
      val json = Json.toJson(preference)

      json mustEqual Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.EveryFourWeeks.toString,
        "account"   -> Json.obj(
          "holder"        -> "applicant",
          "firstName"     -> "first",
          "lastName"      -> "last",
          "sortCode"      -> "000000",
          "accountNumber" -> "00000000"
        ),
        "eldestChild" -> Json.obj(
          "name" -> Json.obj(
            "firstName" -> "first",
            "lastName"  -> "last"
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
        "account"   -> "no suitable account"
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
            "lastName"  -> "last"
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

  "Existing account payment preference must serialise to JSON" in {

    val preference = ExistingAccount(EldestChild(ChildName("first", None, "last"), dateOfBirth), PaymentFrequency.Weekly.toString)
    val json = Json.toJson(preference)

    json mustEqual Json.obj(
      "wantsToBePaid" -> true,
      "eldestChild" -> Json.obj(
        "name" -> Json.obj(
          "firstName" -> "first",
          "lastName"  -> "last"
        ),
        "dateOfBirth" -> "2020-12-31"
      ),
      "frequency" -> PaymentFrequency.Weekly.toString,
      "account" -> "use existing account"
    )

  }

  "Lived In Uk And Abroad must serialise to JSON" - {

    "when optional values are not present" in {

      val model = Residency.LivedInUkAndAbroad(None, None, Set.empty[String], Nil, Nil)

      Json.toJson(model) mustEqual Json.obj(
        "alwaysLivedInUk" -> false,
        "usuallyLivesInUk" -> true
      )
    }

    "when optional values are present" in {

      val model = Residency.LivedInUkAndAbroad(
        Some("country 1"),
        Some(LocalDate.of(2022, 12, 31)),
        Set("employment status"),
        List("country 2"),
        List("country 3")
      )

      val json = Json.toJson(model)

      json mustEqual Json.obj(
        "alwaysLivedInUk" -> false,
        "usuallyLivesInUk" -> false,
        "usualCountryOfResidence" -> "country 1",
        "arrivalDate" -> "2022-12-31",
        "countriesRecentlyWorked" -> Json.arr("country 2"),
        "countriesRecentlyReceivedBenefits" -> Json.arr("country 3"),
        "employmentStatus" -> Json.arr("employment status")
      )
    }
  }

  "Always lived abroad must serialise to JSON" - {

    "when optional values are not present" in {

      val model = Residency.AlwaysLivedAbroad("country 1", Set.empty, Nil, Nil)

      val json = Json.toJson(model)

      json mustEqual Json.obj(
        "alwaysLivedInUk" -> false,
        "usuallyLivesInUk" -> false,
        "usualCountryOfResidence" -> "country 1"
      )
    }

    "when optional values are present" in {

      val model = Residency.AlwaysLivedAbroad("country 1", Set("employment status"), List("country 2"), List("country 3"))

      val json = Json.toJson(model)

      json mustEqual Json.obj(
        "alwaysLivedInUk" -> false,
        "usuallyLivesInUk" -> false,
        "usualCountryOfResidence" -> "country 1",
        "countriesRecentlyWorked" -> Json.arr("country 2"),
        "countriesRecentlyReceivedBenefits" -> Json.arr("country 3"),
        "employmentStatus" -> Json.arr("employment status")
      )
    }
  }
}
