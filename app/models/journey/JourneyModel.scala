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

package models.journey

import cats.data._
import cats.implicits._
import models.ReasonNotToSubmit._
import models.{Benefits, CurrentlyReceivingChildBenefit, OtherEligibilityFailReason, ReasonNotToSubmit, RequiredDocument, UserAnswers}
import pages.{AdditionalInformationPage, IncludeAdditionalInformationPage}
import pages.applicant.CurrentlyReceivingChildBenefitPage
import pages.partner.RelationshipStatusPage
import pages.payments.{ApplicantBenefitsPage, ApplicantOrPartnerBenefitsPage, WantToBePaidPage}
import queries.Query

import java.time.LocalDate

final case class JourneyModel(
                               applicant: Applicant,
                               relationship: Relationship,
                               children: NonEmptyList[Child],
                               benefits: Option[Set[Benefits]],
                               paymentPreference: PaymentPreference,
                               additionalInformation: Option[String],
                               userAuthenticated: Boolean
                             ) {

  val allRequiredDocuments: List[RequiredDocument] =
    children.toList.flatMap { child =>
      child.requiredDocuments.map(doc => RequiredDocument(child.name, doc))
    }

  val anyDocumentsRequired: Boolean = allRequiredDocuments.nonEmpty

  lazy val reasonsNotToSubmit: Seq[ReasonNotToSubmit] = {

    val userUnauthenticated =          if (userAuthenticated) None else Some(UserUnauthenticated)
    val childOverSixMonths =           if (children.exists(_.dateOfBirth.isBefore(LocalDate.now.minusMonths(6)))) Some(ChildOverSixMonths) else None
    val documentsRequired =            if (anyDocumentsRequired) Some(DocumentsRequired) else None
    val designatoryDetailsChanged =    if (applicant.changedDesignatoryDetails.contains(true)) Some(DesignatoryDetailsChanged) else None
    val partnerNinoMissing =           if (relationship.partner.exists(p => p.eldestChild.nonEmpty && p.nationalInsuranceNumber.isEmpty)) Some(PartnerNinoMissing) else None
    val additionalInformationPresent = if (additionalInformation.nonEmpty) Some(AdditionalInformationPresent) else None

    Seq(
      userUnauthenticated,
      childOverSixMonths,
      documentsRequired,
      designatoryDetailsChanged,
      partnerNinoMissing,
      additionalInformationPresent
    ).flatten
  }

  lazy val otherEligibilityFailureReasons: Seq[OtherEligibilityFailReason] = {

    import OtherEligibilityFailReason._

    val applicantWorkedAbroad = applicant.residency match {
      case Residency.LivedInUkAndAbroad(_, _, _, countries, _) if countries.nonEmpty => Some(ApplicantWorkedAbroad)
      case _ => None
    }

    val applicantReceivedBenefitsAbroad = applicant.residency match {
      case Residency.LivedInUkAndAbroad(_, _, _, _, countries) if countries.nonEmpty => Some(ApplicantReceivedBenefitsAbroad)
      case _ => None
    }

    val partnerWorkedAbroad = relationship.partner.flatMap { partner =>
      if (partner.countriesWorked.nonEmpty) Some(PartnerWorkedAbroad) else None
    }

    val partnerReceivedBenefitsAbroad = relationship.partner.flatMap { partner =>
      if (partner.countriesReceivedBenefits.nonEmpty) Some(PartnerReceivedBenefitsAbroad) else None
    }

    val childRecentlyLivedElsewhere = children match {
      case x if x.exists(_.possiblyLivedAbroadSeparately) => Some(ChildRecentlyLivedElsewhere)
      case _ => None
    }

    val bankAccountRisk = paymentPreference.accountDetails match {
      case Some(x: BankAccountWithHolder) if x.risk.exists(_.riskAboveTolerance) => Some(BankAccountInsightsRisk)
      case _ => None
    }

    val childPossiblyRecentlyUnderLocalAuthorityCare = children match {
      case x if x.exists(_.possiblyRecentlyCaredForByLocalAuthority) => Some(ChildPossiblyRecentlyUnderLocalAuthorityCare)
      case _ => None
    }

    Seq(
      applicantWorkedAbroad,
      applicantReceivedBenefitsAbroad,
      partnerWorkedAbroad,
      partnerReceivedBenefitsAbroad,
      childRecentlyLivedElsewhere,
      bankAccountRisk,
      childPossiblyRecentlyUnderLocalAuthorityCare
    ).flatten
  }
}

object JourneyModel {

  def build(answers: UserAnswers): IorNec[Query, JourneyModel] =
    (
      Applicant.build(answers),
      Relationship.build(answers),
      Child.buildChildren(answers),
      getBenefits(answers),
      PaymentPreference.build(answers),
      getAdditionalInformation(answers)
    ).parMapN(JourneyModel(_, _, _, _, _, _, answers.isAuthenticated))


  private def getAdditionalInformation(answers: UserAnswers): IorNec[Query, Option[String]] =
    answers.getIor(IncludeAdditionalInformationPage).flatMap {
      case true => answers.getIor(AdditionalInformationPage).map(Some(_))
      case false => Ior.Right(None)
    }

  private def getBenefits(answers: UserAnswers): IorNec[Query, Option[Set[Benefits]]] = {

    import models.RelationshipStatus._

    answers.getIor(RelationshipStatusPage).flatMap {
      case Married | Cohabiting =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            answers.getIor(CurrentlyReceivingChildBenefitPage).flatMap {
              case CurrentlyReceivingChildBenefit.GettingPayments =>
                Ior.Right(None)

              case _ =>
                answers.getIor(ApplicantOrPartnerBenefitsPage).map(Some(_))
            }

          case false =>
            Ior.Right(None)
        }

      case Single | Separated | Divorced | Widowed =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            answers.getIor(CurrentlyReceivingChildBenefitPage).flatMap {
              case CurrentlyReceivingChildBenefit.GettingPayments =>
                Ior.Right(None)

              case _ =>
                answers.getIor(ApplicantBenefitsPage).map(Some(_))
            }

          case false =>
            Ior.Right(None)
        }
    }
  }
}
