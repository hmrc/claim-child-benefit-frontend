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

package pages.partner

import controllers.partner.routes
import models.{Index, UserAnswers}
import pages.child.ChildNamePage
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object PartnerWaitingForEntitlementDecisionPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "partnerWaitingForEntitlementDecision"

  override def route(waypoints: Waypoints): Call =
    routes.PartnerWaitingForEntitlementDecisionController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true => PartnerEldestChildNamePage
      case false => ChildNamePage(Index(0))
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers.get(PartnerEldestChildNamePage)
          .map(_ => waypoints.next.page)
          .getOrElse(PartnerEldestChildNamePage)

      case false =>
        waypoints.next.page
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers.remove(PartnerEldestChildNamePage)
        .flatMap(_.remove(PartnerEldestChildDateOfBirthPage))
    } else {
      super.cleanup(value, userAnswers)
    }
}
