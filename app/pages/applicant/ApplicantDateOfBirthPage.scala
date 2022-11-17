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
import models.UserAnswers
import pages.partner.PartnerIsHmfOrCivilServantPage
import pages.{AlwaysLivedInUkPage, Page, QuestionPage, RelationshipStatusPage, UsePrintAndPostFormPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import utils.MonadOps._

import java.time.LocalDate

case object ApplicantDateOfBirthPage extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "applicantDateOfBirth"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantDateOfBirthController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(AlwaysLivedInUkPage).map {
      case true =>
        ApplicantCurrentUkAddressPage

      case false =>
        val partnerRelationships = Seq(Married, Cohabiting)

        val canContinue =
          answers.get(ApplicantIsHmfOrCivilServantPage) ||
            (answers.get(RelationshipStatusPage).map(partnerRelationships.contains)
              && answers.get(PartnerIsHmfOrCivilServantPage)
            )

        canContinue.map {
          case true  => ApplicantCurrentAddressInUkPage
          case false => UsePrintAndPostFormPage
        }.orRecover
    }.orRecover
}
