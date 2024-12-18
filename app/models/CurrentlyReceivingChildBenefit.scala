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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait CurrentlyReceivingChildBenefit

object CurrentlyReceivingChildBenefit extends Enumerable.Implicits {

  case object GettingPayments extends WithName("gettingPayments") with CurrentlyReceivingChildBenefit
  case object NotGettingPayments extends WithName("notGettingPayments") with CurrentlyReceivingChildBenefit
  case object NotClaiming extends WithName("notClaiming") with CurrentlyReceivingChildBenefit

  val values: Seq[CurrentlyReceivingChildBenefit] = Seq(
    GettingPayments, NotGettingPayments, NotClaiming
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"currentlyReceivingChildBenefit.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[CurrentlyReceivingChildBenefit] =
    Enumerable(values.map(v => v.toString -> v)*)
}
