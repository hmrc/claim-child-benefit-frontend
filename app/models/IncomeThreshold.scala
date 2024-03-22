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

import java.time.{Clock, LocalDate}

sealed trait IncomeThreshold {
  def amount(clock: Clock): String
}

object IncomeThreshold {

  private def taxYear2324(clock: Clock): Boolean =
    LocalDate.now(clock).isBefore(LocalDate.of(2024, 4, 6))

  case object Lower extends IncomeThreshold {
    override def amount(clock: Clock): String = if (taxYear2324(clock)) "50,000" else "60,000"
  }

  case object Upper extends IncomeThreshold {
    override def amount(clock: Clock): String = if (taxYear2324(clock)) "60,000" else "80,000"
  }
}
