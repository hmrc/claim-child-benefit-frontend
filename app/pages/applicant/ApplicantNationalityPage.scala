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
import models.RelationshipStatus._
import models.{Index, Nationality, UserAnswers}
import pages.child.ChildNamePage
import pages.partner.PartnerNamePage
import pages.{CannotUseServiceNationalityPage, Page, QuestionPage, RelationshipStatusPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object ApplicantNationalityPage extends QuestionPage[Nationality] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "applicantNationality"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantNationalityController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Nationality.British | Nationality.DualWithBritish =>
        answers.get(RelationshipStatusPage).map {
          case Married | Cohabiting =>
            PartnerNamePage

          case Single | Divorced | Separated | Widowed =>
            ChildNamePage(Index(0))
        }.orRecover

      case Nationality.Other =>
        CannotUseServiceNationalityPage
    }.orRecover
}
