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
import models.{Benefits, RelationshipStatus, UserAnswers}
import pages.income._
import pages.partner._
import pages.payments.PaymentFrequencyPage
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
      case Cohabiting                            => CohabitationDatePage
      case Separated                             => SeparationDatePage
      case Married | Single | Divorced | Widowed => LivedOrWorkedAbroadPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Cohabiting =>
        answers.get(CohabitationDatePage)
          .map { _ =>
            answers.get(ApplicantOrPartnerIncomePage)
              .map(_ => waypoints.next.page)
              .getOrElse(ApplicantOrPartnerIncomePage)
          }
          .getOrElse(CohabitationDatePage)

      case Separated =>
        answers.get(SeparationDatePage)
          .map { _ =>
            answers.get(ApplicantIncomePage)
              .map(_ => waypoints.next.page)
              .getOrElse(ApplicantIncomePage)
          }
          .getOrElse(SeparationDatePage)

      case Married =>
        answers.get(ApplicantOrPartnerIncomePage)
          .map(_ => waypoints.next.page)
          .getOrElse(ApplicantOrPartnerIncomePage)

      case  Single | Divorced | Widowed =>
        answers.get(ApplicantIncomePage)
        .map(_ => waypoints.next.page)
        .getOrElse(ApplicantIncomePage)

    }.orRecover

  override def cleanup(value: Option[RelationshipStatus], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case Married =>
        userAnswers.get(ApplicantOrPartnerBenefitsPage).map {
          benefits =>
            if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
              userAnswers.remove(CohabitationDatePage)
                .flatMap(_.remove(SeparationDatePage))
                .flatMap(removeApplicantIncomeSection)
                .flatMap(_.remove(PaymentFrequencyPage))
            } else {
              userAnswers.remove(CohabitationDatePage)
                .flatMap(_.remove(SeparationDatePage))
                .flatMap(removeApplicantIncomeSection)
            }
        }.getOrElse {
          userAnswers.remove(CohabitationDatePage)
            .flatMap(_.remove(SeparationDatePage))
            .flatMap(removeApplicantIncomeSection)
        }

      case Cohabiting =>
        userAnswers.get(ApplicantOrPartnerBenefitsPage).map {
          benefits =>
            if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
              userAnswers.remove(SeparationDatePage)
                .flatMap(removeApplicantIncomeSection)
                .flatMap(_.remove(PaymentFrequencyPage))
            } else {
              userAnswers.remove(SeparationDatePage)
                .flatMap(removeApplicantIncomeSection)
            }
        }.getOrElse {
          userAnswers.remove(SeparationDatePage)
            .flatMap(removeApplicantIncomeSection)
        }

      case Separated =>
        userAnswers.remove(CohabitationDatePage)
          .flatMap(removeApplicantOrPartnerIncomeSection)
          .flatMap(removePartnerSection)

      case Single | Divorced | Widowed =>
        userAnswers.remove(CohabitationDatePage)
          .flatMap(_.remove(SeparationDatePage))
          .flatMap(removeApplicantOrPartnerIncomeSection)
          .flatMap(removePartnerSection)

    }.getOrElse(super.cleanup(value, userAnswers))

  private def removeApplicantIncomeSection(answers: UserAnswers): Try[UserAnswers] =
    answers.remove(ApplicantIncomePage)
      .flatMap(_.remove(ApplicantBenefitsPage))

  private def removeApplicantOrPartnerIncomeSection(answers: UserAnswers): Try[UserAnswers] =
    answers.remove(ApplicantOrPartnerIncomePage)
      .flatMap(_.remove(ApplicantOrPartnerBenefitsPage))

  private def removePartnerSection(answers: UserAnswers): Try[UserAnswers] =
    answers.remove(PartnerNamePage)
      .flatMap(_.remove(PartnerNinoKnownPage))
      .flatMap(_.remove(PartnerNinoPage))
      .flatMap(_.remove(PartnerDateOfBirthPage))
      .flatMap(_.remove(PartnerNationalityPage))
      .flatMap(_.remove(PartnerEmploymentStatusPage))
      .flatMap(_.remove(PartnerIsHmfOrCivilServantPage))
      .flatMap(_.remove(PartnerEntitledToChildBenefitPage))
      .flatMap(_.remove(PartnerWaitingForEntitlementDecisionPage))
      .flatMap(_.remove(PartnerEldestChildNamePage))
      .flatMap(_.remove(PartnerEldestChildDateOfBirthPage))
}
