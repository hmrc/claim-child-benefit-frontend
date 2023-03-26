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
import logging.Logging
import models.ApplicantResidence._
import models.{Address, AdultName, ApplicantPreviousName, Benefits, BirthCertificateNumber, BirthRegistrationMatchingRequest, BirthRegistrationMatchingResult, ChildName, Country, CurrentlyReceivingChildBenefit, Index, Nationality, PaymentFrequency, UserAnswers, ChildBirthRegistrationCountry => RegistrationCountry}
import pages._
import pages.applicant._
import pages.child._
import pages.partner._
import pages.payments._
import queries._
import services.BrmsService
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//scalastyle:off
class JourneyModelProvider @Inject()(brmsService: BrmsService)(implicit ec: ExecutionContext) extends Logging {

  def buildFromUserAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[IorNec[Query, JourneyModel]] =
      (
        IorT.fromIor[Future](getApplicant(answers)),
        IorT.fromIor[Future](getRelationship(answers)),
        getChildren(answers),
        IorT.fromIor[Future](getBenefits(answers)),
        IorT.fromIor[Future](getPaymentPreference(answers)),
        IorT.fromIor[Future](getAdditionalInformation(answers))
      ).parMapN(JourneyModel(_, _, _, _, _, _, answers.isAuthenticated)).value

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

  private def getChildren(answers: UserAnswers)(implicit hc: HeaderCarrier): IorT[Future, NonEmptyChain[Query], NonEmptyList[Child]] =
    IorT.fromIor[Future](Child.buildChildren(answers))

  private def getApplicant(answers: UserAnswers): IorNec[Query, Applicant] =
    Applicant.build(answers)

  private def getRelationship(answers: UserAnswers): IorNec[Query, Relationship] =
    Relationship.build(answers)

  private def getPaymentPreference(answers: UserAnswers): IorNec[Query, PaymentPreference] =
    PaymentPreference.build(answers)
}
