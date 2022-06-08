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

import java.time.LocalDate
import controllers.routes
import models.RelationshipStatus.{Cohabiting, Separated}
import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object RelationshipStatusDatePage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "relationshipStatusDate"

  override def route(waypoints: Waypoints): Call =
    routes.RelationshipStatusDateController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(RelationshipStatusPage).map {
      case Separated  => ApplicantIncomeOver50kPage
      case Cohabiting => ApplicantOrPartnerIncomeOver50kPage
      case _          => JourneyRecoveryPage
    }.orRecover
}
