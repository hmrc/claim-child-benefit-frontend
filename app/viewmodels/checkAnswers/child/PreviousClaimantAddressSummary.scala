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

import models.{Index, UserAnswers}
import pages.child.PreviousClaimantAddressPage
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PreviousClaimantAddressSummary {

  def row(answers: UserAnswers, index: Index, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    answers.get(PreviousClaimantAddressPage(index)).map {
      answer =>

        val value =
          Seq(Some(answer.line1), answer.line2, answer.line3, Some(answer.postcode))
            .flatten
            .map(HtmlFormat.escape(_).toString)
            .mkString("<br/>")

        SummaryListRowViewModel(
          key = "previousClaimantAddress.checkYourAnswersLabel",
          value = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", PreviousClaimantAddressPage(index).changeLink(waypoints, sourcePage).url)
              .withVisuallyHiddenText(messages("previousClaimantAddress.change.hidden"))
          )
        )
    }
}
