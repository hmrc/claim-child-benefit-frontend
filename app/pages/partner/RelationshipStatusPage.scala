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

package pages.partner

import controllers.partner.routes
import models.RelationshipStatus._
import models.{RelationshipStatus, UserAnswers}
import pages.payments._
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.{AllCountriesPartnerReceivedBenefits, AllCountriesPartnerWorked, AllPartnerNationalities, Settable}

import scala.util.{Success, Try}

case object RelationshipStatusPage extends QuestionPage[RelationshipStatus] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "relationshipStatus"

  override def route(waypoints: Waypoints): Call =
    routes.RelationshipStatusController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Married                     => PartnerNamePage
      case Cohabiting                  => CohabitationDatePage
      case Separated                   => SeparationDatePage
      case Single | Divorced | Widowed => CheckPartnerDetailsPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page = {

    def taskListSectionsChanging(oldStatus: RelationshipStatus, newStatus: RelationshipStatus): Boolean =
      newStatus match {
        case Married | Cohabiting =>
          Seq(Single, Separated, Divorced, Widowed).contains(oldStatus) &&
            originalAnswers.isDefined(ApplicantIncomePage)

        case Single | Separated | Divorced | Widowed =>
          Seq(Married, Cohabiting).contains(oldStatus) &&
            originalAnswers.isDefined(ApplicantIncomePage)
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

        case Married =>
          updatedAnswers
            .get(PartnerNamePage)
            .map(_ => waypoints.next.page)
            .getOrElse(PartnerNamePage)

        case _ =>
          waypoints.next.page
      }

    originalAnswers.get(this).map {
      oldStatus =>
        updatedAnswers.get(this).map {
          newStatus =>
            if (taskListSectionsChanging(oldStatus, newStatus)) {
              RelationshipStatusChangesTaskListPage
            } else {
              nextPage(newStatus)
            }
        }.orRecover

    }.orRecover
  }

  override def cleanup(value: Option[RelationshipStatus], originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Try[UserAnswers] = {

    def needToRemovePaymentPages(newStatus: RelationshipStatus): Boolean =
      newStatus match {
        case Married | Cohabiting =>
          originalAnswers.get(RelationshipStatusPage).exists {
            case Cohabiting | Married =>
              false

            case Single | Separated | Divorced | Widowed =>
              originalAnswers.isDefined(ApplicantIncomePage)
          }

        case Single | Separated | Divorced | Widowed =>
          originalAnswers.get(RelationshipStatusPage).exists {
            case Cohabiting | Married =>
              originalAnswers.isDefined(ApplicantIncomePage)

            case Single | Separated | Divorced | Widowed =>
              false
          }
      }

    def pagesToAlwaysRemove(newStatus: RelationshipStatus): Seq[Settable[_]] =
      newStatus match {
        case Married =>
          Seq(CohabitationDatePage, SeparationDatePage)

        case Cohabiting =>
          Seq(SeparationDatePage)

        case Separated =>
          partnerPages :+ CohabitationDatePage

        case Single | Divorced | Widowed =>
          partnerPages ++ Seq(CohabitationDatePage, SeparationDatePage)
      }

    value.map {
      status =>
        val paymentPagesToRemove = if(needToRemovePaymentPages(status)) paymentPages else Nil

        val pages =
          pagesToAlwaysRemove(status) ++ paymentPagesToRemove

        updatedAnswers
          .set(RelationshipStatusChangesTaskListPage, needToRemovePaymentPages(status))
          .flatMap(x => removePages(x, pages))
    }.getOrElse(super.cleanup(value, updatedAnswers))
  }

  private val partnerPages: Seq[Settable[_]] = Seq(
    PartnerNamePage,
    PartnerNinoKnownPage,
    PartnerNinoPage,
    PartnerDateOfBirthPage,
    AllPartnerNationalities,
    PartnerIsHmfOrCivilServantPage,
    PartnerEmploymentStatusPage,
    PartnerWorkedAbroadPage,
    AllCountriesPartnerWorked,
    PartnerReceivedBenefitsAbroadPage,
    AllCountriesPartnerReceivedBenefits,
    PartnerClaimingChildBenefitPage,
    PartnerEldestChildNamePage,
    PartnerEldestChildDateOfBirthPage
  )

  private val paymentPages: Seq[Settable[_]] = Seq(
    AccountTypePage,
    ApplicantIncomePage,
    PartnerIncomePage,
    WantToBePaidPage,
    ApplicantBenefitsPage,
    ApplicantOrPartnerBenefitsPage,
    PaymentFrequencyPage,
    ApplicantHasSuitableAccountPage,
    BankAccountHolderPage,
    BankAccountDetailsPage,
    BuildingSocietyDetailsPage
  )

  private def removePages(answers: UserAnswers, pages: Seq[Settable[_]]): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(answers))((acc, page) => acc.flatMap(_.remove(page)))
}
