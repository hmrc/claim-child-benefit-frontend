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
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object AccountIsJointPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "accountIsJoint"

  override def route(waypoints: Waypoints): Call =
    routes.AccountIsJointController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true => AccountHolderNamesPage
      case false => AccountHolderNamePage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers.get(AccountHolderNamesPage)
          .map(_ => waypoints.next.page)
          .getOrElse(AccountHolderNamesPage)

      case false =>
        answers.get(AccountHolderNamePage)
          .map(_ => waypoints.next.page)
          .getOrElse(AccountHolderNamePage)
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case true  => userAnswers.remove(AccountHolderNamePage)
      case false => userAnswers.remove(AccountHolderNamesPage)
    }.getOrElse(super.cleanup(value, userAnswers))
}
