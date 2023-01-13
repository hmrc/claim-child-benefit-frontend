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

package pages.payments

import controllers.payments.routes
import models.UserAnswers
import pages.applicant.ApplicantHasPreviousFamilyNamePage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object WantToBePaidToExistingAccountPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "wantToBePaidToExistingAccount"

  override def route(waypoints: Waypoints): Call =
    routes.WantToBePaidToExistingAccountController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true  => ApplicantHasPreviousFamilyNamePage
      case false => ApplicantHasSuitableAccountPage
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(true)) {
      userAnswers.remove(ApplicantHasSuitableAccountPage)
        .flatMap(_.remove(BankAccountHolderPage))
        .flatMap(_.remove(BankAccountDetailsPage))
    } else {
      super.cleanup(value, userAnswers)
    }
}
