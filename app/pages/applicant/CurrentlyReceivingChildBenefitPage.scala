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

package pages.applicant

import controllers.applicant.routes
import models.CurrentlyReceivingChildBenefit.{GettingPayments, NotClaiming, NotGettingPayments}
import models.{CurrentlyReceivingChildBenefit, UserAnswers}
import pages.payments._
import pages.{NonEmptyWaypoints, Page, QuestionPage, RecoveryOps, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.Settable

import scala.util.{Success, Try}

case object CurrentlyReceivingChildBenefitPage extends QuestionPage[CurrentlyReceivingChildBenefit] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "currentlyReceivingChildBenefit"

  override def route(waypoints: Waypoints): Call =
    routes.CurrentlyReceivingChildBenefitController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case GettingPayments | NotGettingPayments => EldestChildNamePage
      case NotClaiming                          => CheckApplicantDetailsPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    updatedAnswers.get(this).map {
      case NotClaiming =>
        val answerAffectsTaskList =
          originalAnswers.get(this).exists {
            case NotClaiming => false
            case _ => originalAnswers.isDefined(ApplicantIncomePage) || originalAnswers.isDefined(ApplicantOrPartnerIncomePage)
          }

        if (answerAffectsTaskList) CurrentlyReceivingChangesTaskListPage else waypoints.next.page

      case other =>
        val answerAffectsTaskList =
          originalAnswers.get(this).exists {
            case x if x == other => false
            case _ => originalAnswers.isDefined(ApplicantIncomePage) || originalAnswers.isDefined(ApplicantOrPartnerIncomePage)
          }

        if (answerAffectsTaskList) {
          CurrentlyReceivingChangesTaskListPage
        } else {
          updatedAnswers.get(EldestChildNamePage)
            .map(_ => waypoints.next.page)
            .getOrElse(EldestChildNamePage)
        }
    }.orRecover

  override def cleanup(value: Option[CurrentlyReceivingChildBenefit], previousAnswers: UserAnswers, currentAnswers: UserAnswers): Try[UserAnswers] = {

    def needToRemovePaymentPages(receiving: CurrentlyReceivingChildBenefit): Boolean =
      if (previousAnswers.get(CurrentlyReceivingChildBenefitPage).contains(receiving)) {
        false
      } else {
        previousAnswers.isDefined(ApplicantIncomePage) || previousAnswers.isDefined(ApplicantOrPartnerIncomePage)
      }

    def pagesToAlwaysRemove(receiving: CurrentlyReceivingChildBenefit): Seq[Settable[_]] = {
      receiving match {
        case NotClaiming => Seq(EldestChildNamePage, EldestChildDateOfBirthPage)
        case _ => Nil
      }
    }

    value.map {
      receiving =>
        val paymentPagesToRemove = if(needToRemovePaymentPages(receiving)) paymentPages else Nil
        val pages = pagesToAlwaysRemove(receiving) ++ paymentPagesToRemove

        currentAnswers
          .set(CurrentlyReceivingChangesTaskListPage, needToRemovePaymentPages(receiving))
          .flatMap(x => removePages(x, pages))
    }.getOrElse(super.cleanup(value, currentAnswers))
  }

  private val paymentPages: Seq[Settable[_]] = Seq(
    ApplicantOrPartnerIncomePage,
    ApplicantIncomePage,
    WantToBePaidPage,
    ApplicantBenefitsPage,
    ApplicantOrPartnerBenefitsPage,
    PaymentFrequencyPage,
    WantToBePaidToExistingAccountPage,
    ApplicantHasSuitableAccountPage,
    BankAccountHolderPage,
    BankAccountDetailsPage
  )

  private def removePages(answers: UserAnswers, pages: Seq[Settable[_]]): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(answers))((acc, page) => acc.flatMap(_.remove(page)))
}
