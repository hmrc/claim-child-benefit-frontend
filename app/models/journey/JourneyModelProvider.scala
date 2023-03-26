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

  private def getChildren(answers: UserAnswers)(implicit hc: HeaderCarrier): IorT[Future, NonEmptyChain[Query], NonEmptyList[Child]] = {

    def getChild(index: Index): IorT[Future, NonEmptyChain[Query], Child] = {

      def getNameChangedByDeedPoll: IorNec[Query, Option[Boolean]] =
        answers.getIor(ChildHasPreviousNamePage(index)).flatMap {
          case true  => answers.getIor(ChildNameChangedByDeedPollPage(index)).map(Some(_))
          case false => Ior.Right(None)
        }

      def getPreviousNames: IorNec[Query, List[ChildName]] =
        answers.getIor(ChildHasPreviousNamePage(index)).flatMap {
          case true  => answers.getIor(AllChildPreviousNames(index))
          case false => Ior.Right(Nil)
        }

      def getBirthCertificateNumber: IorNec[Query, Option[BirthCertificateNumber]] =
        answers.getIor(ChildBirthRegistrationCountryPage(index)).flatMap {
          case RegistrationCountry.England | RegistrationCountry.Wales =>
            answers.getIor(BirthCertificateHasSystemNumberPage(index)).flatMap {
              case true =>
                answers.getIor(ChildBirthCertificateSystemNumberPage(index)).map(Some(_))
              case false =>
                Ior.Right(None)
            }

          case RegistrationCountry.Scotland =>
            answers.getIor(ScottishBirthCertificateHasNumbersPage(index)).flatMap {
              case true =>
                answers.getIor(ChildScottishBirthCertificateDetailsPage(index)).map(x => Some(x))
              case false =>
                Ior.Right(None)
            }

          case _ =>
            Ior.Right(None)
        }

      def matchBirthCertificateDetails: IorT[Future, NonEmptyChain[Query], BirthRegistrationMatchingResult] =
        (
          IorT.fromIor[Future](getBirthCertificateNumber),
          IorT.fromIor[Future](answers.getIor(ChildNamePage(index))),
          IorT.fromIor[Future](answers.getIor(ChildDateOfBirthPage(index))),
          IorT.fromIor[Future](answers.getIor(ChildBirthRegistrationCountryPage(index)))
        ).parMapN(BirthRegistrationMatchingRequest.apply).flatMap {
          maybeRequest =>
            IorT.liftF[Future, NonEmptyChain[Query], BirthRegistrationMatchingResult](
              brmsService.matchChild(maybeRequest)
            )
        }

      def getPreviousClaimant: IorNec[Query, Option[PreviousClaimant]] =
        PreviousClaimant.build(answers, index)

      def getGuardian: IorNec[Query, Option[Guardian]] =
        Guardian.build(answers, index)

      def getPreviousGuardian: IorNec[Query, Option[PreviousGuardian]] =
        PreviousGuardian.build(answers, index)

      def getDateChildStartedLivingWithApplicant: IorNec[Query, Option[LocalDate]] =
        answers.getIor(ChildLivesWithApplicantPage(index)).flatMap {
          case true =>
            answers.getIor(ChildLivedWithAnyoneElsePage(index)).flatMap {
              case true =>
                answers.getIor(DateChildStartedLivingWithApplicantPage(index)).map(Some(_))

              case false =>
                Ior.Right(None)
            }

          case false =>
            Ior.Right(None)
        }

      (
        IorT.fromIor[Future](answers.getIor(ChildNamePage(index))),
        IorT.fromIor[Future](getNameChangedByDeedPoll),
        IorT.fromIor[Future](getPreviousNames),
        IorT.fromIor[Future](answers.getIor(ChildBiologicalSexPage(index))),
        IorT.fromIor[Future](answers.getIor(ChildDateOfBirthPage(index))),
        IorT.fromIor[Future](answers.getIor(ChildBirthRegistrationCountryPage(index))),
        IorT.fromIor[Future](getBirthCertificateNumber),
        matchBirthCertificateDetails,
        IorT.fromIor[Future](answers.getIor(ApplicantRelationshipToChildPage(index))),
        IorT.fromIor[Future](answers.getIor(AdoptingThroughLocalAuthorityPage(index))),
        IorT.fromIor[Future](getPreviousClaimant),
        IorT.fromIor[Future](getGuardian),
        IorT.fromIor[Future](getPreviousGuardian),
        IorT.fromIor[Future](getDateChildStartedLivingWithApplicant)
      ).parMapN(Child.apply)
    }

    answers.getIor(AllChildSummaries).getOrElse(Nil).indices.toList.parTraverse { i =>
      getChild(Index(i))
    }.flatMap { children =>
      IorT.fromIor[Future](NonEmptyList.fromList(children).toRightIor(NonEmptyChain.one(AllChildSummaries)))
    }
  }

  private def getApplicant(answers: UserAnswers): IorNec[Query, Applicant] =
    Applicant.build(answers)

  private def getRelationship(answers: UserAnswers): IorNec[Query, Relationship] =
    Relationship.build(answers)

  private def getPaymentPreference(answers: UserAnswers): IorNec[Query, PaymentPreference] =
    PaymentPreference.build(answers)
}
