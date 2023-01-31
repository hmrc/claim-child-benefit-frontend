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

package viewmodels.checkAnswers.partner

import models.{Index, UserAnswers}
import pages.partner.{PartnerNamePage, PartnerNationalityPage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object PartnerNationalitySummary {

  def row(answers: UserAnswers, index: Index, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    for {
      partnerName <- answers.get(PartnerNamePage)
      nationality <- answers.get(PartnerNationalityPage(index))
    } yield {

      SummaryListRowViewModel(
        key = messages("partnerNationality.checkYourAnswersLabel", partnerName.firstName),
        value = ValueViewModel(nationality.name),
        actions = Seq(
          ActionItemViewModel(
            messages("site.change"),
            PartnerNationalityPage(index).changeLink(waypoints, sourcePage).url
          ).withVisuallyHiddenText(messages("partnerNationality.change.hidden", partnerName.firstName))
        )
      )
    }
}
