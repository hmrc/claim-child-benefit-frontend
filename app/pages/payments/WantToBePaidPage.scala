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

package pages.payments

import controllers.payments.routes
import models.CurrentlyReceivingChildBenefit.{GettingPayments, NotClaiming, NotGettingPayments}
import models.RelationshipStatus._
import models.{Benefits, PaymentFrequency, UserAnswers}
import pages.applicant.ApplicantHasPreviousFamilyNamePage
import pages.income.{ApplicantBenefitsPage, ApplicantOrPartnerBenefitsPage}
import pages.partner.PartnerNamePage
import pages.{CannotBePaidWeeklyPage, NonEmptyWaypoints, Page, QuestionPage, RelationshipStatusPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.WeeklyPaymentEligibilityQuery

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
          case Married | Cohabiting => ApplicantOrPartnerBenefitsPage
          case _                    => ApplicantBenefitsPage
        }.orRecover

      case false =>
        CheckPaymentDetailsPage
    }.orRecover

//  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page = {
//
//    val getPartnerDetailsIfMissing =
//      answers.get(PartnerNamePage)
//        .map(_ => waypoints.next.page)
//        .getOrElse(PartnerNamePage)
//
//    answers.get(RelationshipStatusPage).map {
//      case Married | Cohabiting =>
//        answers.get(this).map {
//          case true =>
//            answers.get(PaymentFrequencyPage).map {
//              case PaymentFrequency.Weekly =>
//                answers.get(ApplicantOrPartnerBenefitsPage).map {
//                  benefits =>
//                    if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
//                      CannotBePaidWeeklyPage
//                    } else {
//                      getPartnerDetailsIfMissing
//                    }
//                }.orRecover
//
//              case PaymentFrequency.EveryFourWeeks =>
//                getPartnerDetailsIfMissing
//            }.getOrElse {
//                answers.get(ApplicantOrPartnerBenefitsPage).map {
//                  benefits =>
//                    if (benefits.intersect(Benefits.qualifyingBenefits).isEmpty) {
//                      answers.get(CurrentlyReceivingChildBenefitPage).map {
//                        case GettingPayments =>
//                          answers.get(WantToBePaidToExistingAccountPage)
//                            .map(_ => getPartnerDetailsIfMissing)
//                            .getOrElse(WantToBePaidToExistingAccountPage)
//
//                        case NotGettingPayments | NotClaiming =>
//                          answers.get(ApplicantHasSuitableAccountPage)
//                            .map(_ => getPartnerDetailsIfMissing)
//                            .getOrElse(ApplicantHasSuitableAccountPage)
//                      }.orRecover
//                    } else {
//                      PaymentFrequencyPage
//                    }
//                }.orRecover
//              }
//
//          case false =>
//            getPartnerDetailsIfMissing
//        }.orRecover
//
//
//      case Single | Divorced | Separated | Widowed =>
//        answers.get(this).map {
//          case true =>
//            answers.get(PaymentFrequencyPage)
//              .map(_ => waypoints.next.page)
//              .getOrElse(PaymentFrequencyPage)
//
//          case false =>
//            waypoints.next.page
//        }.orRecover
//    }.orRecover
//  }
//
//  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
//    if (value.contains(false)) {
//      userAnswers.remove(PaymentFrequencyPage)
//        .flatMap(_.remove(WantToBePaidToExistingAccountPage))
//        .flatMap(_.remove(ApplicantHasSuitableAccountPage))
//        .flatMap(_.remove(BankAccountHolderPage))
//        .flatMap(_.remove(BankAccountDetailsPage))
//    } else {
//      super.cleanup(value, userAnswers)
//    }
}
