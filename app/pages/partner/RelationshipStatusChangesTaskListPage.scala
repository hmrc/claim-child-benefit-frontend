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
import models.RelationshipStatus._
import models.UserAnswers
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.RecoveryOps

case object RelationshipStatusChangesTaskListPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "relationshipStatusChangesTaskList"

  override def route(waypoints: Waypoints): Call =
    routes.RelationshipStatusChangesTaskListController.onPageLoad(waypoints)

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page = {
    answers.get(RelationshipStatusPage).map {
      case Separated =>
        answers
          .get(SeparationDatePage)
          .map(_ => waypoints.next.page)
          .getOrElse(SeparationDatePage)

      case Cohabiting =>
        answers
          .get(CohabitationDatePage)
          .map(_ => waypoints.next.page)
          .getOrElse(CohabitationDatePage)

      case Married =>
        answers
          .get(PartnerNamePage)
          .map(_ => waypoints.next.page)
          .getOrElse(PartnerNamePage)
      case _ =>
        waypoints.next.page
    }.orRecover
  }
}
