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

package pages.payments

import controllers.payments.routes
import models.UserAnswers
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object ApplicantHasSuitableAccountPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "applicantHasSuitableAccount"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantHasSuitableAccountController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true  => BankAccountHolderPage
      case false => CheckPaymentDetailsPage
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers
        .remove(BankAccountHolderPage)
        .flatMap(_.remove(AccountTypePage))
        .flatMap(_.remove(BankAccountDetailsPage))
        .flatMap(_.remove(BuildingSocietyDetailsPage))
    } else {
      super.cleanup(value, userAnswers)
    }
}
