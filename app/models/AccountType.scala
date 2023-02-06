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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.govuk.hint._

sealed trait AccountType

object AccountType extends Enumerable.Implicits {

  case object SortCodeAccountNumber extends WithName("sortCodeAndAccountNumber") with AccountType
  case object BuildingSocietyRollNumber extends WithName("buildingSocietyRollNumber") with AccountType

  val values: Seq[AccountType] = Seq(SortCodeAccountNumber, BuildingSocietyRollNumber)

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"accountType.${value.toString}")),
        value = Some(value.toString),
        id = Some(s"value_$index"),
        hint = Some(HintViewModel(Text(messages(s"accountType.${value.toString}.hint"))))
      )
  }

  implicit val enumerable: Enumerable[AccountType] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
