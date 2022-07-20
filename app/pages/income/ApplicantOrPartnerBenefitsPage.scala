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

package pages.income

import controllers.income.routes
import models.{Benefits, UserAnswers}
import pages.partner.PartnerNamePage
import pages.payments.{WantToBePaidPage, WantToBePaidWeeklyPage}
import pages.{CannotBePaidWeeklyPage, NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object ApplicantOrPartnerBenefitsPage extends QuestionPage[Set[Benefits]] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "applicantOrPartnerBenefits"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantOrPartnerBenefitsController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    TaxChargeExplanationPage

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
    originalAnswers.get(WantToBePaidWeeklyPage).map {
      case true =>
        updatedAnswers.get(ApplicantOrPartnerBenefitsPage).map {
          benefits =>
            if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
              CannotBePaidWeeklyPage
            } else {
              TaxChargeExplanationPage
            }
        }.orRecover

      case false =>
        TaxChargeExplanationPage
    }.getOrElse {
      updatedAnswers.get(WantToBePaidPage).map {
        case true =>
          TaxChargeExplanationPage
        case false =>
          updatedAnswers.get(PartnerNamePage)
            .map(_ => waypoints.next.page)
            .getOrElse(PartnerNamePage)
      }.orRecover
    }
  }

  override def cleanup(value: Option[Set[Benefits]], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      benefits =>
        if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
          userAnswers.remove(WantToBePaidWeeklyPage)
        } else {
          super.cleanup(value, userAnswers)
        }
    }.getOrElse(super.cleanup(value, userAnswers))
}
