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

import controllers.partner.routes
import models.{Index, UserAnswers}
import pages.partner.{AddCountryPartnerReceivedBenefitsPage, PartnerNamePage, CountryPartnerReceivedBenefitsPage}
import pages.{AddItemPage, CheckAnswersPage, Waypoints}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import queries.AllCountriesPartnerReceivedBenefits
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.ListItem
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object AddCountryPartnerReceivedBenefitsSummary {

  def rows(answers: UserAnswers, waypoints: Waypoints, sourcePage: AddItemPage): Seq[ListItem] =
    answers.get(AllCountriesPartnerReceivedBenefits).getOrElse(List.empty).zipWithIndex.map {
      case (country, index) =>

        ListItem(
          name = country.name,
          changeUrl = CountryPartnerReceivedBenefitsPage(Index(index)).changeLink(waypoints, sourcePage).url,
          removeUrl = routes.RemoveCountryPartnerReceivedBenefitsController.onPageLoad(waypoints, Index(index)).url
        )
    }

  def checkAnswersRow(answers: UserAnswers, waypoints: Waypoints, sourcePage: CheckAnswersPage)
                     (implicit messages: Messages): Option[SummaryListRow] =
    for {
      countries <- answers.get(AllCountriesPartnerReceivedBenefits)
      partnerName <- answers.get(PartnerNamePage)
    } yield {

      val value = countries.map(n => n.name).mkString("<br>")

      SummaryListRowViewModel(
        key = messages("addCountryPartnerReceivedBenefits.checkYourAnswersLabel", partnerName.firstName),
        value = ValueViewModel(HtmlContent(value)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            AddCountryPartnerReceivedBenefitsPage().changeLink(waypoints, sourcePage).url
          ).withVisuallyHiddenText(messages("addCountryPartnerReceivedBenefits.checkAnswers.change.hidden", partnerName.firstName))
        )
      )
    }
}
