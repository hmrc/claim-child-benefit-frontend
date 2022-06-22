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

package viewmodels.checkAnswers.child

import controllers.child.routes
import models.{Index, UserAnswers}
import pages.child.{AddChildPreviousNamePage, ChildPreviousNamePage}
import pages.{AddItemPage, CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import queries.AllChildPreviousNames
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AddChildPreviousNameSummary {

  def rows(answers: UserAnswers, childIndex: Index, waypoints: Waypoints, sourcePage: AddItemPage)
          (implicit messages: Messages): Seq[ListItem] =
    answers.get(AllChildPreviousNames(childIndex)).getOrElse(Nil).zipWithIndex.map {
      case (previousName, index) =>

        ListItem(
          name = previousName.fullName,
          changeUrl = ChildPreviousNamePage(childIndex, Index(index)).changeLink(waypoints, sourcePage).url,
          removeUrl = routes.RemoveChildPreviousNameController.onPageLoad(waypoints, childIndex, Index(index)).url
        )
    }

  def checkAnswersRow(answers: UserAnswers, childIndex: Index, waypoints: Waypoints, sourcePage: CheckAnswersPage)
                     (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(AllChildPreviousNames(childIndex)).map {
      names =>

        val value = names.map(_.fullName).mkString("<br/>")

        SummaryListRowViewModel(
          key = "addChildPreviousName.checkYourAnswersLabel",
          value = ValueViewModel(value),
          actions = Seq(
            ActionItemViewModel("site.change", AddChildPreviousNamePage(childIndex).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("addChildPreviousName.change.hidden"))
          )
        )
    }
}
