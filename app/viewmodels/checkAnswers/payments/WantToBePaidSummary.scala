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

package viewmodels.checkAnswers.payments

import models.Income._
import models.RelationshipStatus._
import models.UserAnswers
import pages.partner.RelationshipStatusPage
import pages.payments.{ApplicantIncomePage, ApplicantOrPartnerIncomePage, WantToBePaidPage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object WantToBePaidSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] = {

    def buildRow: Option[SummaryListRow] =
      answers.get(WantToBePaidPage).map {
        answer =>

          val value = if (answer) "site.yes" else "site.no"

          SummaryListRowViewModel(
            key = "wantToBePaid.checkYourAnswersLabel",
            value = ValueViewModel(value),
            actions = Seq(
              ActionItemViewModel("site.change", WantToBePaidPage.changeLink(waypoints, sourcePage).url)
                .withVisuallyHiddenText(messages("wantToBePaid.change.hidden"))
            )
          )
      }

    answers.get(RelationshipStatusPage).flatMap {
      case Married | Cohabiting =>
        answers.get(ApplicantOrPartnerIncomePage).flatMap {
          case BelowLowerThreshold => None
          case _                   => buildRow
        }

      case Single | Separated | Divorced | Widowed =>
        answers.get(ApplicantIncomePage).flatMap {
          case BelowLowerThreshold => None
          case _                   => buildRow
        }
    }
  }
}
