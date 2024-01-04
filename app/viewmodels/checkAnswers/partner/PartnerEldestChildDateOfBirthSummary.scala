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

package viewmodels.checkAnswers.partner

import models.UserAnswers
import pages.partner.{PartnerEldestChildDateOfBirthPage, PartnerEldestChildNamePage}
import pages.{CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

import java.time.format.DateTimeFormatter

object PartnerEldestChildDateOfBirthSummary {

  def row(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
         (implicit messages: Messages): Option[SummaryListRow] =
    for {
      eldestChildName <- answers.get(PartnerEldestChildNamePage)
      dateOfBirth <- answers.get(PartnerEldestChildDateOfBirthPage)
    } yield {

      val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

      SummaryListRowViewModel(
        key = messages("partnerEldestChildDateOfBirth.checkYourAnswersLabel", eldestChildName.firstName),
        value = ValueViewModel(dateOfBirth.format(dateFormatter)),
        actions = Seq(
          ActionItemViewModel(
            messages("site.change"),
            PartnerEldestChildDateOfBirthPage.changeLink(waypoints, sourcePage).url
          ).withVisuallyHiddenText(messages("partnerEldestChildDateOfBirth.change.hidden", eldestChildName.firstName))
        )
      )
    }
}
