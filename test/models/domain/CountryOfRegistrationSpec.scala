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
import play.api.libs.json.{JsString, Json}

class CountryOfRegistrationSpec extends AnyFreeSpec with Matchers {

  ".writes" - {

    "must write England / Wales" in {

      Json.toJson[CountryOfRegistration](CountryOfRegistration.EnglandWales) mustEqual JsString("ENGLAND_WALES")
    }

    "must write Scotland" in {

      Json.toJson[CountryOfRegistration](CountryOfRegistration.Scotland) mustEqual JsString("SCOTLAND")
    }

    "must write Northern Ireland" in {

      Json.toJson[CountryOfRegistration](CountryOfRegistration.NorthernIreland) mustEqual JsString("NORTHERN_IRELAND")
    }

    "must write Abroad" in {

      Json.toJson[CountryOfRegistration](CountryOfRegistration.Abroad) mustEqual JsString("ABROAD")
    }
  }
}
