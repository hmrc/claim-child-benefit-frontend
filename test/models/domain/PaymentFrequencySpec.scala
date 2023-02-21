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

class PaymentFrequencySpec extends AnyFreeSpec with Matchers {

  ".build" - {

    "must return Weekly when given Weekly" in {

      PaymentFrequency.build(models.PaymentFrequency.Weekly) mustEqual PaymentFrequency.Weekly
    }

    "must return Every Four Weeks when given Every Four Weeks" in {

      PaymentFrequency.build(models.PaymentFrequency.EveryFourWeeks) mustEqual PaymentFrequency.EveryFourWeeks
    }
  }
  ".writes" - {

    "must write Weekly" in {

      Json.toJson[PaymentFrequency](PaymentFrequency.Weekly) mustEqual JsString("ONCE_A_WEEK")
    }

    "must write Every Four Weeks" in {

      Json.toJson[PaymentFrequency](PaymentFrequency.EveryFourWeeks) mustEqual JsString("EVERY_4_WEEKS")
    }
  }
}
