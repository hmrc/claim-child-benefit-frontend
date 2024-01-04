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

package pages.partner

import controllers.partner.routes
import models.{Index, UserAnswers}
import pages.{AddItemPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Call
import queries.{Derivable, DeriveNumberOfCountriesPartnerReceivedBenefits}

final case class AddCountryPartnerReceivedBenefitsPage(override val index: Option[Index] = None) extends AddItemPage(index) with QuestionPage[Boolean] {

  override def isTheSamePage(other: Page): Boolean = other match {
    case _: AddCountryPartnerReceivedBenefitsPage => true
    case _ => false
  }

  override val normalModeUrlFragment: String = "add-country-your-partner-received-benefits"
  override val checkModeUrlFragment: String = "change-country-your-partner-received-benefits"

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addCountryPartnerReceivedBenefits"

  override def route(waypoints: Waypoints): Call =
    routes.AddCountryPartnerReceivedBenefitsController.onPageLoad(waypoints)

  override def deriveNumberOfItems: Derivable[Seq[JsObject], Int] = DeriveNumberOfCountriesPartnerReceivedBenefits

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        index
        .map(i => CountryPartnerReceivedBenefitsPage(Index(i.position + 1)))
        .getOrElse {
          answers
            .get(deriveNumberOfItems)
            .map(n => CountryPartnerReceivedBenefitsPage(Index(n)))
            .orRecover
        }

      case false =>
        PartnerClaimingChildBenefitPage
    }.orRecover
}
