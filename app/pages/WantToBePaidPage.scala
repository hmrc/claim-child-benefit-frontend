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
import models.Benefits.qualifyingBenefits
import models.RelationshipStatus._
import models.UserAnswers
import pages.income.ApplicantOrPartnerBenefitsPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object WantToBePaidPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "wantToBePaid"

  override def route(waypoints: Waypoints): Call =
    routes.WantToBePaidController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers.get(RelationshipStatusPage).map {
          case Married | Cohabiting =>
            if (answers.get(ApplicantOrPartnerBenefitsPage).forall(_.intersect(qualifyingBenefits).isEmpty)) {
              ApplicantHasSuitableAccountPage
            } else {
              WantToBePaidWeeklyPage
            }

          case _ =>
            WantToBePaidWeeklyPage
        }.orRecover

      case false =>
        ApplicantHasPreviousFamilyNamePage
    }.orRecover
}
