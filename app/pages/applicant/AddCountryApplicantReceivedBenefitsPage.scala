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

package pages.applicant

import controllers.applicant.routes
import models.{Index, UserAnswers}
import pages.{AddItemPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Call
import queries.{Derivable, DeriveNumberOfCountriesApplicantReceivedBenefits}

final case class AddCountryApplicantReceivedBenefitsPage(override val index: Option[Index] = None) extends AddItemPage(index) with QuestionPage[Boolean] {

  override def isTheSamePage(other: Page): Boolean = other match {
    case _: AddCountryApplicantReceivedBenefitsPage => true
    case _ => false
  }

  override val normalModeUrlFragment: String = "add-country-you-received-benefits"
  override val checkModeUrlFragment: String = "change-country-you-received-benefits"

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addCountryApplicantReceivedBenefits"

  override def route(waypoints: Waypoints): Call =
    routes.AddCountryApplicantReceivedBenefitsController.onPageLoad(waypoints)

  override def deriveNumberOfItems: Derivable[Seq[JsObject], Int] = DeriveNumberOfCountriesApplicantReceivedBenefits

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        index
          .map(i => CountryApplicantReceivedBenefitsPage(Index(i.position + 1)))
          .getOrElse {
            answers
              .get(deriveNumberOfItems)
              .map(n => CountryApplicantReceivedBenefitsPage(Index(n)))
              .orRecover
          }

      case false =>
        ApplicantIsHmfOrCivilServantPage
    }.orRecover
}
