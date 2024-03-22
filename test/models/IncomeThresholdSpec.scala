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

package models

import base.SpecBase

import java.time.{Clock, Instant, LocalDate, ZoneId}

class IncomeThresholdSpec extends SpecBase {

  "IncomeThreshold" - {
    "must return correct amounts for date before 6 April 2024" in {
      val instant: Instant =
        LocalDate.of(2024, 4, 5).atStartOfDay(ZoneId.systemDefault).toInstant
      val clock: Clock =
        Clock.fixed(instant, ZoneId.systemDefault)

      IncomeThreshold.Lower.amount(clock) mustBe "50,000"
      IncomeThreshold.Upper.amount(clock) mustBe "60,000"
    }

    "must return correct amounts for date after 5 April 2024" in {
      val instant: Instant =
        LocalDate.of(2024, 4, 6).atStartOfDay(ZoneId.systemDefault).toInstant
      val clock: Clock =
        Clock.fixed(instant, ZoneId.systemDefault)

      IncomeThreshold.Lower.amount(clock) mustBe "60,000"
      IncomeThreshold.Upper.amount(clock) mustBe "80,000"
    }
  }
}
