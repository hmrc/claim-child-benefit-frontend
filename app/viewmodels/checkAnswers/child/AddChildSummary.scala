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

package viewmodels.checkAnswers.child

import controllers.child.routes
import models.{Index, UserAnswers}
import pages.child.{AddChildPage, CheckChildDetailsPage}
import pages.{AddItemPage, CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.AllChildSummaries
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AddChildSummary {

  def rows(answers: UserAnswers, waypoints: Waypoints, sourcePage: AddItemPage): Seq[ListItem] =
    answers.get(AllChildSummaries).getOrElse(Nil).zipWithIndex.map {
      case (summary, index) =>

        ListItem(
          name = summary.childName.fullName,
          changeUrl = CheckChildDetailsPage(Index(index)).changeLink(waypoints, sourcePage).url,
          removeUrl = routes.RemoveChildController.onPageLoad(waypoints, Index(index)).url
        )
    }

  def checkAnswersRow(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
                     (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllChildSummaries).map {
      summaries =>

        val value = summaries.map(summary => HtmlFormat.escape(summary.childName.fullName).toString).mkString("<br/>")

        SummaryListRowViewModel(
          key = "addChild.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", AddChildPage(None).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("addChild.checkAnswers.change.hidden"))
          )
        )
    }
}
