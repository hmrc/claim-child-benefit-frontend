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
import models.Benefits.qualifyingBenefits
import models.RelationshipStatus._
import models.UserAnswers
import pages.applicant.ApplicantHasPreviousFamilyNamePage
import pages.income.ApplicantOrPartnerBenefitsPage
import pages.{Page, QuestionPage, RelationshipStatusPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

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

          case Single | Separated | Divorced | Widowed =>
            WantToBePaidWeeklyPage
        }.orRecover

      case false =>
        ApplicantHasPreviousFamilyNamePage
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers.remove(WantToBePaidWeeklyPage)
        .flatMap(_.remove(ApplicantHasSuitableAccountPage))
        .flatMap(_.remove(AccountInApplicantsNamePage))
        .flatMap(_.remove(AccountIsJointPage))
        .flatMap(_.remove(AccountHolderNamePage))
        .flatMap(_.remove(AccountHolderNamesPage))
        .flatMap(_.remove(BankAccountTypePage))
        .flatMap(_.remove(BankAccountDetailsPage))
        .flatMap(_.remove(BuildingSocietyAccountDetailsPage))
    } else {
      super.cleanup(value, userAnswers)
    }
}
