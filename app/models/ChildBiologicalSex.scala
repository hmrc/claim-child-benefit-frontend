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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.govuk.hint._
import viewmodels.govuk.radios._
sealed trait ChildBiologicalSex

object ChildBiologicalSex extends Enumerable.Implicits {

  case object Female extends WithName("female") with ChildBiologicalSex
  case object Male extends WithName("male") with ChildBiologicalSex
  case object Unspecified extends WithName("unspecified") with ChildBiologicalSex

  val values: Seq[ChildBiologicalSex] = Seq(
    Female, Male, Unspecified
  )

  def options(implicit messages: Messages): Seq[RadioItem] = Seq(
    RadioItem(
      content = Text(messages(s"childBiologicalSex.${Female.toString}")),
      value   = Some(Female.toString),
      id      = Some("value_0")
    ),
    RadioItem(
      content = Text(messages(s"childBiologicalSex.${Male.toString}")),
      value = Some(Male.toString),
      id = Some("value_1")
    ),
    RadioItem(
      content = Text(messages(s"childBiologicalSex.${Unspecified.toString}")),
      value = Some(Unspecified.toString),
      id = Some("value_2")
    ).withHint(HintViewModel(Text(messages("childBiologicalSex.unspecified.hint"))))
  )

  implicit val enumerable: Enumerable[ChildBiologicalSex] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
