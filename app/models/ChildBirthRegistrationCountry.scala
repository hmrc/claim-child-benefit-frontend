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

sealed trait ChildBirthRegistrationCountry

object ChildBirthRegistrationCountry extends Enumerable.Implicits {

  case object England extends WithName("england") with ChildBirthRegistrationCountry
  case object Scotland extends WithName("scotland") with ChildBirthRegistrationCountry
  case object Wales extends WithName("wales") with ChildBirthRegistrationCountry
  case object NorthernIreland extends WithName("northernIreland") with ChildBirthRegistrationCountry
  case object OtherCountry extends WithName("other") with ChildBirthRegistrationCountry
  case object UnknownCountry extends WithName("unknown") with ChildBirthRegistrationCountry

  val values: Seq[ChildBirthRegistrationCountry] = Seq(
    England, Scotland, Wales, NorthernIreland, OtherCountry, UnknownCountry
  )

  def options(implicit messages: Messages): Seq[RadioItem] = {

    val divider = RadioItem(divider = Some(messages("site.or")))

    values.zipWithIndex.map {

      case (value, index) =>
        RadioItem(
          content = Text(messages(s"childBirthRegistrationCountry.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }.patch(values.size - 1, List(divider), 0)
  }

  implicit val enumerable: Enumerable[ChildBirthRegistrationCountry] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
