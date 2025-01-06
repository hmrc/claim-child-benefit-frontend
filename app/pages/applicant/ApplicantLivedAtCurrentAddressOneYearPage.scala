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
import models.{ApplicantResidence, UserAnswers}
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.RecoveryOps

import scala.util.Try

case object ApplicantLivedAtCurrentAddressOneYearPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "applicantLivedAtCurrentAddressOneYear"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantLivedAtCurrentAddressOneYearController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers.get(ApplicantResidencePage).map {
          case ApplicantResidence.AlwaysUk =>
            ApplicantIsHmfOrCivilServantPage

          case _ =>
            ApplicantEmploymentStatusPage
        }.orRecover

      case false =>
        answers.get(ApplicantResidencePage).map {
          case ApplicantResidence.AlwaysUk     => ApplicantPreviousUkAddressPage
          case ApplicantResidence.UkAndAbroad  => ApplicantPreviousAddressInUkPage
          case ApplicantResidence.AlwaysAbroad => ApplicantPreviousInternationalAddressPage
        }.orRecover
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(true)) {
      userAnswers
        .remove(ApplicantPreviousAddressInUkPage)
        .flatMap(_.remove(ApplicantPreviousUkAddressPage))
        .flatMap(_.remove(ApplicantPreviousInternationalAddressPage))
    } else {
      super.cleanup(value, userAnswers)
    }
}
