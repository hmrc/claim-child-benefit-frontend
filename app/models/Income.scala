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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait Income {
  val order: Int
}

object Income extends Enumerable.Implicits {

  case object BelowLowerThreshold extends WithName("belowLowerThreshold") with Income { override val order: Int = 1 }
  case object BetweenThresholds extends WithName("betweenThresholds") with Income { override val order: Int = 2 }
  case object AboveUpperThreshold extends WithName("aboveUpperThreshold") with Income { override val order: Int = 3 }

  val values: Seq[Income] = Seq(
    BelowLowerThreshold,
    BetweenThresholds,
    AboveUpperThreshold
  )

  def options(implicit messages: Messages): Seq[RadioItem] = {
    values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"income.${value.toString}", IncomeThreshold.Lower.amount, IncomeThreshold.Upper.amount)),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }
  }

  implicit val enumerable: Enumerable[Income] =
    Enumerable(values.map(v => v.toString -> v)*)
}

object IncomeOrdering extends Ordering[Income] {
  override def compare(x: Income, y: Income): Int = x.order compare y.order
}
