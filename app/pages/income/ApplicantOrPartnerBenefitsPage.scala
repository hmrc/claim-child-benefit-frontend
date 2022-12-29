/*
 * Copyright 2023 HM Revenue & Customs
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
import models.CurrentlyReceivingChildBenefit.NotClaiming
import models.PaymentFrequency.{EveryFourWeeks, Weekly}
import models.{Benefits, UserAnswers}
import pages.partner.PartnerNamePage
import pages.payments.{ApplicantHasSuitableAccountPage, CurrentlyReceivingChildBenefitPage, PaymentFrequencyPage, WantToBePaidPage, WantToBePaidToExistingAccountPage}
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
    answers.get(this).map {
      case benefits if benefits.intersect(Benefits.qualifyingBenefits).nonEmpty =>
        PaymentFrequencyPage

      case _ =>
        answers.get(CurrentlyReceivingChildBenefitPage).map {
          case NotClaiming => ApplicantHasSuitableAccountPage
          case _           => WantToBePaidToExistingAccountPage
        }.orRecover
    }.orRecover

//  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {
//    originalAnswers.get(PaymentFrequencyPage).map {
//      case Weekly =>
//        updatedAnswers.get(this).map {
//          benefits =>
//            if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
//              CannotBePaidWeeklyPage
//            } else {
//              WantToBePaidPage
//            }
//        }.orRecover
//
//      case EveryFourWeeks =>
//        WantToBePaidPage
//
//    }.getOrElse {
//      updatedAnswers.get(this).map {
//        benefits =>
//          if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
//            updatedAnswers.get(PartnerNamePage)
//              .map(_ => waypoints.next.page)
//              .getOrElse(PartnerNamePage)
//          } else {
//            updatedAnswers.get(WantToBePaidPage).map {
//              case true =>
//                PaymentFrequencyPage
//
//              case false =>
//                updatedAnswers.get(PartnerNamePage)
//                  .map(_ => waypoints.next.page)
//                  .getOrElse(PartnerNamePage)
//            }.orRecover
//          }
//      }.orRecover
//    }
//  }
//
//  override def cleanup(value: Option[Set[Benefits]], userAnswers: UserAnswers): Try[UserAnswers] =
//    value.map {
//      benefits =>
//        if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
//          userAnswers.remove(PaymentFrequencyPage)
//        } else {
//          super.cleanup(value, userAnswers)
//        }
//    }.getOrElse(super.cleanup(value, userAnswers))
}
