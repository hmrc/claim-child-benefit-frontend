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

sealed trait ApplicantRelationshipToChild

object ApplicantRelationshipToChild extends Enumerable.Implicits {

  case object BirthChild extends WithName("birthChild") with ApplicantRelationshipToChild
  case object AdoptedChild extends WithName("adoptedChild") with ApplicantRelationshipToChild
  case object StepChild extends WithName("stepChild") with ApplicantRelationshipToChild
  case object OtherRelationship extends WithName("other") with ApplicantRelationshipToChild

  val values: Seq[ApplicantRelationshipToChild] = Seq(
    BirthChild, AdoptedChild, StepChild, OtherRelationship
  )

  def options(implicit messages: Messages): Seq[RadioItem] = {

    val divider = RadioItem(divider = Some(messages("site.or")))

    values.zipWithIndex.map {
      case (value, index) =>
        RadioItem(
          content = Text(messages(s"applicantRelationshipToChild.${value.toString}")),
          value = Some(value.toString),
          id = Some(s"value_$index")
        )
    }.patch(values.size - 1, List(divider), 0)
  }

  implicit val enumerable: Enumerable[ApplicantRelationshipToChild] =
    Enumerable(values.map(v => v.toString -> v)*)
}
