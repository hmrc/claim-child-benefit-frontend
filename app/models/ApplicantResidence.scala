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

sealed trait ApplicantResidence

object ApplicantResidence extends Enumerable.Implicits {

  case object AlwaysUk extends WithName("alwaysUk") with ApplicantResidence
  case object CurrentlyUk extends WithName("currentlyUk") with ApplicantResidence
  case object CurrentlyAbroad extends WithName("currentlyAbroad") with ApplicantResidence
  case object AlwaysAbroad extends WithName("alwaysAbroad") with ApplicantResidence

  val values: Seq[ApplicantResidence] = Seq(
    AlwaysUk, CurrentlyUk, CurrentlyAbroad, AlwaysAbroad
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"applicantResidenc.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[ApplicantResidence] =
    Enumerable(values.map(v => v.toString -> v): _*)
}