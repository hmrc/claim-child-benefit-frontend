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

package pages.payments

import controllers.payments.routes
import models.CurrentlyReceivingChildBenefit.GettingPayments
import models.RelationshipStatus._
import models.UserAnswers
import pages.applicant.CurrentlyReceivingChildBenefitPage
import pages.partner.RelationshipStatusPage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.RecoveryOps

import scala.util.Try

case object WantToBePaidPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "wantToBePaid"

  override def route(waypoints: Waypoints): Call =
    routes.WantToBePaidController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
    answers.relationshipDetails.map {
      case x if x.hasClaimedChildBenefit =>
        answers.get(this).map {
          case true  => PaidToExistingAccountPage
          case false => CheckPaymentDetailsPage
        }.orRecover

      case _ =>
        answers.get(this).map {
          case true =>
            goToBenefits(answers)

          case false =>
            CheckPaymentDetailsPage
        }.orRecover
    }.getOrElse {
      answers.get(this).map {
        case true =>
          answers.get(CurrentlyReceivingChildBenefitPage).map {
            case GettingPayments =>
              PaidToExistingAccountPage

            case _ =>
              goToBenefits(answers)
          }.orRecover

        case false =>
          CheckPaymentDetailsPage
      }.orRecover
    }
  }

  private def goToBenefits(answers: UserAnswers): Page =
    answers.get(RelationshipStatusPage).map {
      case Married | Cohabiting => ApplicantOrPartnerBenefitsPage
      case _                    => ApplicantBenefitsPage
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers.remove(PaymentFrequencyPage)
        .flatMap(_.remove(ApplicantBenefitsPage))
        .flatMap(_.remove(ApplicantOrPartnerBenefitsPage))
        .flatMap(_.remove(ApplicantHasSuitableAccountPage))
        .flatMap(_.remove(BankAccountHolderPage))
        .flatMap(_.remove(AccountTypePage))
        .flatMap(_.remove(BankAccountDetailsPage))
        .flatMap(_.remove(BuildingSocietyDetailsPage))
    } else {
      super.cleanup(value, userAnswers)
    }
}
