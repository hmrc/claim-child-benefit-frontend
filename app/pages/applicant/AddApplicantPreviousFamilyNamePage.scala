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

package pages.applicant

import controllers.applicant.routes
import models.{Index, UserAnswers}
import pages.{AddItemPage, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.DeriveNumberOfPreviousFamilyNames

case object AddApplicantPreviousFamilyNamePage extends QuestionPage[Boolean] with AddItemPage {

  override val normalModeUrlFragment: String = "add-other-name"
  override val checkModeUrlFragment: String = "change-other-name"

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addApplicantPreviousFamilyName"

  override def route(waypoints: Waypoints): Call =
    routes.AddApplicantPreviousFamilyNameController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers
          .get(DeriveNumberOfPreviousFamilyNames)
          .map(n => ApplicantPreviousFamilyNamePage(Index(n)))
          .orRecover

      case false =>
        ApplicantNinoKnownPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers
          .get(DeriveNumberOfPreviousFamilyNames)
        .map(n => ApplicantPreviousFamilyNamePage(Index(n)))
        .orRecover

      case false =>
        waypoints.next.page
    }.orRecover
}
