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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait Benefits

object Benefits extends Enumerable.Implicits {

  case object IncomeSupport extends WithName("incomeSupport") with Benefits
  case object JobseekersAllowance extends WithName("jobseekersAllowance") with Benefits
  case object PensionCredit extends WithName("pensionCredit") with Benefits
  case object EmploymentSupport extends WithName("employmentSupport") with Benefits
  case object UniversalCredit extends WithName("universalCredit") with Benefits
  case object NoneOfTheAbove extends WithName("none") with Benefits

  val values: Seq[Benefits] = Seq(
    JobseekersAllowance,
    EmploymentSupport,
    IncomeSupport,
    PensionCredit,
    UniversalCredit,
    NoneOfTheAbove
  )

  val qualifyingBenefits: Set[Benefits] = values.toSet - NoneOfTheAbove

  def checkboxItems(implicit messages: Messages): Seq[CheckboxItem] = {

    val divider = CheckboxItem(divider = Some(messages("site.or")))

    values.zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"benefits.${value.toString}")),
          fieldId = "value",
          index = index,
          value = value.toString
        )
    }.patch(qualifyingBenefits.size, List(divider), 0)
  }

  implicit val enumerable: Enumerable[Benefits] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
