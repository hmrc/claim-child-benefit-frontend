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

package pages

import controllers.routes
import models.RelationshipStatus._
import models.TaskListSectionChange.{PartnerDetailsRemoved, PartnerDetailsRequired, PaymentDetailsRemoved}
import models.{RelationshipStatus, TaskListSectionChange, UserAnswers}
import pages.applicant.ApplicantIsHmfOrCivilServantPage
import pages.income._
import pages.partner._
import pages.payments._
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.Settable

import scala.util.{Success, Try}

case object RelationshipStatusPage extends QuestionPage[RelationshipStatus] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "relationshipStatus"

  override def route(waypoints: Waypoints): Call =
    routes.RelationshipStatusController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Cohabiting                            => CohabitationDatePage
      case Separated                             => SeparationDatePage
      case Married | Single | Divorced | Widowed => AlwaysLivedInUkPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {

    def taskListSectionsChanging(newStatus: RelationshipStatus): Boolean =
      newStatus match {
        case Married | Cohabiting =>
          val paymentDetailsInvalid = originalAnswers.isDefined(ApplicantIncomePage)
          val partnerSectionNewlyRequired = originalAnswers.get(RelationshipStatusPage).exists {
            case Married | Cohabiting => false
            case Single | Separated | Divorced | Widowed => true
          }

          paymentDetailsInvalid || partnerSectionNewlyRequired

        case Single | Separated | Divorced | Widowed =>
          originalAnswers.isDefined(PartnerNamePage) || originalAnswers.isDefined(ApplicantOrPartnerIncomePage)
      }

    def mustUsePrintAndPost(newStatus: RelationshipStatus): Boolean = {
      newStatus match {
        case Married | Cohabiting =>
          false

        case _ =>
          updatedAnswers.get(AlwaysLivedInUkPage).contains(false) &&
            updatedAnswers.get(ApplicantIsHmfOrCivilServantPage).contains(false)
      }
    }

    def nextPage(newStatus: RelationshipStatus) =
      newStatus match {
        case Cohabiting =>
          updatedAnswers
            .get(CohabitationDatePage)
            .map(_ => waypoints.next.page)
            .getOrElse(CohabitationDatePage)

        case Separated =>
          updatedAnswers
            .get(SeparationDatePage)
            .map(_ => waypoints.next.page)
            .getOrElse(SeparationDatePage)

        case _ =>
          waypoints.next.page
      }

    updatedAnswers.get(this).map {
      status =>
        if (mustUsePrintAndPost(status)) {
          UsePrintAndPostFormPage
        } else if (taskListSectionsChanging(status)) {
          RelationshipStatusChangesTaskListPage
        } else {
          nextPage(status)
        }
    }.orRecover
  }

  override def cleanup(value: Option[RelationshipStatus], originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Try[UserAnswers] = {

    def maybeRequirePartner(newStatus: RelationshipStatus): Option[TaskListSectionChange] = {
      newStatus match {
        case Married | Cohabiting =>
          originalAnswers.get(RelationshipStatusPage).flatMap {
            case Cohabiting | Married => None
            case Single | Separated | Divorced | Widowed => Some(PartnerDetailsRequired)
          }

        case Single | Separated | Divorced | Widowed =>
          None
      }
    }

    def maybeRemovePayment(newStatus: RelationshipStatus): Option[TaskListSectionChange] =
      newStatus match {
        case Married | Cohabiting =>
          originalAnswers.get(RelationshipStatusPage).flatMap {
            case Cohabiting | Married =>
              None

            case Single | Separated | Divorced | Widowed =>
              originalAnswers.get(ApplicantIncomePage).map(_ => PaymentDetailsRemoved)
          }

        case Single | Separated | Divorced | Widowed =>
          originalAnswers.get(RelationshipStatusPage).flatMap {
            case Cohabiting | Married =>
              originalAnswers.get(ApplicantOrPartnerIncomePage).map(_ => PaymentDetailsRemoved)

            case Single | Separated | Divorced | Widowed =>
              None
          }
      }

    def maybeRemovePartner(newStatus: RelationshipStatus): Option[TaskListSectionChange] = {
      newStatus match {
        case Married | Cohabiting =>
          None

        case Single | Separated | Divorced | Widowed =>
          originalAnswers.get(PartnerNamePage).map(_ => PartnerDetailsRemoved)
      }
    }

    def pagesToAlwaysRemove(newStatus: RelationshipStatus): Seq[Settable[_]] =
      newStatus match {
        case Married =>
          Seq(CohabitationDatePage, SeparationDatePage)

        case Cohabiting =>
          Seq(SeparationDatePage)

        case Separated =>
          Seq(CohabitationDatePage, PartnerIsHmfOrCivilServantPage)

        case Single | Divorced | Widowed =>
          Seq(CohabitationDatePage, SeparationDatePage, PartnerIsHmfOrCivilServantPage)
      }

    value.map {
      status =>
        val sectionChanges = Seq(
          maybeRequirePartner(status),
          maybeRemovePartner(status),
          maybeRemovePayment(status)
        ).flatten

        val pages =
          pagesToAlwaysRemove(status) ++ sectionChanges.flatMap(pagesToRemove)

        updatedAnswers
          .set(RelationshipStatusChangesTaskListPage, sectionChanges.toSet)
          .flatMap(x => removePages(x, pages))
    }.getOrElse(super.cleanup(value, updatedAnswers))
  }

  private def partnerPages: Seq[Settable[_]] = Seq(
    PartnerNamePage,
    PartnerNinoKnownPage,
    PartnerNinoPage,
    PartnerDateOfBirthPage,
    PartnerNationalityPage,
    PartnerIsHmfOrCivilServantPage,
    PartnerClaimingChildBenefitPage,
    PartnerEldestChildNamePage,
    PartnerEldestChildDateOfBirthPage
  )

  private def paymentPages: Seq[Settable[_]] = Seq(
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

  private def pagesToRemove(sectionChange: TaskListSectionChange): Seq[Settable[_]] = sectionChange match {
    case PartnerDetailsRemoved => partnerPages
    case PaymentDetailsRemoved => paymentPages
    case _                     => Nil
  }

  private def removePages(answers: UserAnswers, pages: Seq[Settable[_]]): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(answers))((acc, page) => acc.flatMap(_.remove(page)))
}
