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

package viewmodels.checkAnswers.applicant

import controllers.applicant.routes
import models.{Index, UserAnswers}
import pages.applicant.{AddApplicantPreviousFamilyNamePage, ApplicantPreviousFamilyNamePage}
import pages.{AddItemPage, CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.AllPreviousFamilyNames
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AddApplicantPreviousFamilyNameSummary {

  def rows(answers: UserAnswers, waypoints: Waypoints, sourcePage: AddItemPage): Seq[ListItem] =
    answers.get(AllPreviousFamilyNames).getOrElse(List.empty).zipWithIndex.map {
      case (name, index) =>

        ListItem(
          name = name.lastName,
          changeUrl = ApplicantPreviousFamilyNamePage(Index(index)).changeLink(waypoints, sourcePage).url,
          removeUrl = routes.RemoveApplicantPreviousFamilyNameController.onPageLoad(waypoints, Index(index)).url
        )
    }


  def checkAnswersRow(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
                     (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllPreviousFamilyNames).map {
      names =>

        val value = names.map(n => HtmlFormat.escape(n.lastName)).mkString("<br>")

        SummaryListRowViewModel(
          key = "addApplicantPreviousFamilyName.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              AddApplicantPreviousFamilyNamePage().changeLink(waypoints, sourcePage).url
            ).withVisuallyHiddenText(messages("addApplicantPreviousFamilyName.checkAnswers.change.hidden"))
          )
        )
    }
}
