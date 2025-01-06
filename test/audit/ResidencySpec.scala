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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class ResidencySpec extends AnyFreeSpec with Matchers {

  "Lived In Uk And Abroad must serialise to JSON" - {

    "when optional values are not present" in {

      val model = Residency.LivedInUkAndAbroad(None, None, Set.empty[String], Nil, Nil)

      Json.toJson(model) `mustEqual` Json.obj(
        "alwaysLivedInUk"  -> false,
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

      json `mustEqual` Json.obj(
        "alwaysLivedInUk"                   -> false,
        "usuallyLivesInUk"                  -> false,
        "usualCountryOfResidence"           -> "country 1",
        "arrivalDate"                       -> "2022-12-31",
        "countriesRecentlyWorked"           -> Json.arr("country 2"),
        "countriesRecentlyReceivedBenefits" -> Json.arr("country 3"),
        "employmentStatus"                  -> Json.arr("employment status")
      )
    }
  }

  "Always lived abroad must serialise to JSON" - {

    "when optional values are not present" in {

      val model = Residency.AlwaysLivedAbroad("country 1", Set.empty, Nil, Nil)

      val json = Json.toJson(model)

      json `mustEqual` Json.obj(
        "alwaysLivedInUk"         -> false,
        "usuallyLivesInUk"        -> false,
        "usualCountryOfResidence" -> "country 1"
      )
    }

    "when optional values are present" in {

      val model =
        Residency.AlwaysLivedAbroad("country 1", Set("employment status"), List("country 2"), List("country 3"))

      val json = Json.toJson(model)

      json `mustEqual` Json.obj(
        "alwaysLivedInUk"                   -> false,
        "usuallyLivesInUk"                  -> false,
        "usualCountryOfResidence"           -> "country 1",
        "countriesRecentlyWorked"           -> Json.arr("country 2"),
        "countriesRecentlyReceivedBenefits" -> Json.arr("country 3"),
        "employmentStatus"                  -> Json.arr("employment status")
      )
    }
  }
}
