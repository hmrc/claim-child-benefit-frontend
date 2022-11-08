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

package viewmodels.checkAnswers.applicant

import models.RelationshipStatus._
import models.UserAnswers
import pages.applicant.AlwaysLivedInUkPage
import pages.{CheckAnswersPage, RelationshipStatusPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AlwaysLivedInUkSummary  {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    for {
      alwaysLivedInUk <- answers.get(AlwaysLivedInUkPage)
      relationshipStatus  <- answers.get(RelationshipStatusPage)
    } yield {

      val singleOrCouple = relationshipStatus match {
        case Married | Cohabiting                    => "couple"
        case Single | Separated | Widowed | Divorced => "single"
      }

      val value = if (alwaysLivedInUk) "site.yes" else "site.no"

      SummaryListRowViewModel(
        key     = s"alwaysLivedInUk.$singleOrCouple.checkYourAnswersLabel",
        value   = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel("site.change", AlwaysLivedInUkPage.changeLink(waypoints, sourcePage).url)
            .withVisuallyHiddenText(messages(s"alwaysLivedInUk.$singleOrCouple.change.hidden"))
        )
      )
    }
}
