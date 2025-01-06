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
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.AllCountriesApplicantWorked
import pages.RecoveryOps

import scala.util.Try

case object ApplicantWorkedAbroadPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "applicantWorkedAbroad"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantWorkedAbroadController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true  => CountryApplicantWorkedPage(Index(0))
      case false => ApplicantReceivedBenefitsAbroadPage
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case true =>
        super.cleanup(value, userAnswers)

      case false =>
        userAnswers
          .remove(AllCountriesApplicantWorked)
          .flatMap(_.remove(AddCountryApplicantWorkedPage()))
    }.getOrElse(super.cleanup(value, userAnswers))
}
