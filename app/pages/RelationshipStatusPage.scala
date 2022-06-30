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

package pages

import controllers.routes
import models.RelationshipStatus._
import models.{RelationshipStatus, UserAnswers}
import pages.income.{ApplicantIncomeOver50kPage, ApplicantOrPartnerIncomeOver50kPage}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object RelationshipStatusPage extends QuestionPage[RelationshipStatus] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "relationshipStatus"

  override def route(waypoints: Waypoints): Call =
    routes.RelationshipStatusController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Cohabiting                  => CohabitationDatePage
      case Separated                   => SeparationDatePage
      case Married                     => ApplicantOrPartnerIncomeOver50kPage
      case Single | Divorced | Widowed => ApplicantIncomeOver50kPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Separated | Cohabiting =>
        answers.get(RelationshipStatusDatePage)
          .map(_ => waypoints.next.page)
          .getOrElse(RelationshipStatusDatePage)

      case Married | Single | Divorced | Widowed =>
        waypoints.next.page
    }.orRecover

  override def cleanup(value: Option[RelationshipStatus], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case Married | Single | Divorced | Widowed =>
        userAnswers.remove(RelationshipStatusDatePage)

      case Cohabiting | Separated =>
        super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))
}
