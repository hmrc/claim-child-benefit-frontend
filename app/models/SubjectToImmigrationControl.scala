/*
 * Copyright 2022 HM Revenue & Customs
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

sealed trait SubjectToImmigrationControl

object SubjectToImmigrationControl extends Enumerable.Implicits {

  case object InLastThreeMonths extends WithName("inLastThreeMonths") with SubjectToImmigrationControl
  case object InLastSixMonths extends WithName("inLastSixMonths") with SubjectToImmigrationControl
  case object MoreThanSixMonthsAgo extends WithName("moreThanSixMonthsAgo") with SubjectToImmigrationControl
  case object No extends WithName("no") with SubjectToImmigrationControl

  val values: Seq[SubjectToImmigrationControl] = Seq(
    InLastThreeMonths, InLastSixMonths, MoreThanSixMonthsAgo, No
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"subjectToImmigrationControl.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index")
      )
  }

  implicit val enumerable: Enumerable[SubjectToImmigrationControl] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
