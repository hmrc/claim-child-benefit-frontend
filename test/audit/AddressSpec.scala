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

import models.Country
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class AddressSpec extends AnyFreeSpec with Matchers {

  "UK addresses must serialise to JSON" - {

    "when optional data is present" in {

      val address = UkAddress("a", Some("b"), "c", Some("d"), "e")
      val json = Json.toJson(address)

      json mustEqual Json.obj(
        "line1" -> "a",
        "line2" -> "b",
        "townOrCity" -> "c",
        "county" -> "d",
        "postcode" -> "e"
      )
    }

    "when optional data is missing" in {

      val address = UkAddress("a", None, "c", None, "e")
      val json = Json.toJson(address)

      json mustEqual Json.obj(
        "line1" -> "a",
        "townOrCity" -> "c",
        "postcode" -> "e"
      )
    }
  }

  "international addresses must serialise to JSON" - {

    "when optional data is present" in {

      val address = InternationalAddress("a", Some("b"), "c", Some("d"), Some("e"), Country("f", "g"))
      val json = Json.toJson(address)

      json mustEqual Json.obj(
        "line1" -> "a",
        "line2" -> "b",
        "townOrCity" -> "c",
        "stateOrRegion" -> "d",
        "postcode" -> "e",
        "country" -> Json.obj(
          "code" -> "f",
          "name" -> "g"
        )
      )
    }

    "when optional data is missing" in {

      val address = InternationalAddress("a", None, "c", None, None, Country("f", "g"))
      val json = Json.toJson(address)

      json mustEqual Json.obj(
        "line1" -> "a",
        "townOrCity" -> "c",
        "country" -> Json.obj(
          "code" -> "f",
          "name" -> "g"
        )
      )
    }
  }

  "NPS addresses must serialise to JSON" - {

    "when optional data is present" in {

      val address = NPSAddress("a", Some("b"), Some("c"), Some("d"), Some("e"), Some("f"), Some(Country("g", "h")))
      val json = Json.toJson(address)

      json mustEqual Json.obj(
        "line1" -> "a",
        "line2" -> "b",
        "line3" -> "c",
        "line4" -> "d",
        "line5" -> "e",
        "postcode" -> "f",
        "country" -> Json.obj(
          "code" -> "g",
          "name" -> "h"
        )
      )
    }

    "when optional data is missing" in {

      val address = NPSAddress("a", None, None, None, None, None, None)
      val json = Json.toJson(address)

      json mustEqual Json.obj(
        "line1" -> "a"
      )
    }
  }
}
