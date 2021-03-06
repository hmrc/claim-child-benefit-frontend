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
import models.UserAnswers
import pages.income.ApplicantOrPartnerIncomeOver50kPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import java.time.LocalDate

case object CohabitationDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "cohabitationDate"

  override def route(waypoints: Waypoints): Call =
    routes.CohabitationDateController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    ApplicantOrPartnerIncomeOver50kPage

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(ApplicantOrPartnerIncomeOver50kPage)
      .map(_ => waypoints.next.page)
      .getOrElse(ApplicantOrPartnerIncomeOver50kPage)
}
