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

package viewmodels.checkAnswers.applicant

import models.UserAnswers
import pages.Waypoints
import pages.applicant.{DesignatoryAddressInUkPage, DesignatoryInternationalAddressPage, DesignatoryUkAddressPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object DesignatoryAddressSummary {

  def row(answers: UserAnswers, waypoints: Waypoints)(implicit messages: Messages): Option[SummaryListRow] =
    answers.designatoryDetails.flatMap { designatoryDetails =>
      (
        answers.get(DesignatoryUkAddressPage) orElse
        answers.get(DesignatoryInternationalAddressPage) orElse
        designatoryDetails.residentialAddress
      ).map {
        address =>

          val value =
            address.lines
              .map(HtmlFormat.escape(_).toString)
              .mkString("<br/>")

          SummaryListRowViewModel(
            key = "designatoryAddress.checkYourAnswersLabel",
            value = ValueViewModel(HtmlContent(value)),
            actions = Seq(
              ActionItemViewModel("site.change", DesignatoryAddressInUkPage.route(waypoints).url)
                .withVisuallyHiddenText(messages("designatoryAddress.change.hidden"))
            )
          )
      }
    }

  def checkApplicantDetailsRow(answers: UserAnswers, waypoints: Waypoints)
                              (implicit messages: Messages): Option[SummaryListRow] =
    if (answers.notDefined(DesignatoryUkAddressPage) && answers.notDefined(DesignatoryInternationalAddressPage)) {
      answers.designatoryDetails.flatMap { designatoryDetails =>
        designatoryDetails.residentialAddress.map { address =>

          val value =
            address.lines
              .map(HtmlFormat.escape(_).toString)
              .mkString("<br/>")

          SummaryListRowViewModel(
            key = "designatoryAddress.checkYourAnswersLabel",
            value = ValueViewModel(HtmlContent(value)),
            actions = Nil
          )
        }
      }
    } else {
      None
    }
}
