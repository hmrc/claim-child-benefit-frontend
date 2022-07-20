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

package models

import cats.data.{Ior, IorNec}
import cats.implicits._
import cats.Monad
import models.{ChildBirthRegistrationCountry => Country}
import models.JourneyModel._
import pages._
import pages.applicant._
import pages.child._
import pages.income._
import pages.partner._
import pages.payments._
import queries.{AllChildPreviousNames, AllChildSummaries, AllPreviousFamilyNames, Query}

import java.time.LocalDate
import scala.language.higherKinds

final case class JourneyModel(
                               applicant: Applicant,
                               relationship: Relationship,
                               children: List[Child],
                               benefits: Set[Benefits],
                               paymentPreference: PaymentPreference
                             )

object JourneyModel {

  final case class Relationship(status: RelationshipStatus, since: Option[LocalDate], partner: Option[Partner])
  final case class EldestChild(name: ChildName, dateOfBirth: LocalDate)
  final case class PaymentDetails(wantToBePaid: Boolean, wantToBePaidWeekly: Option[Boolean])

  sealed trait PaymentPreference

  object PaymentPreference {

    final case class  Weekly(bankAccountDetails: Option[BankAccountDetails]) extends PaymentPreference
    final case class  EveryFourWeeks(bankAccountDetails: Option[BankAccountDetails]) extends PaymentPreference
    final case class  ExistingAccount(eldestChild: EldestChild) extends PaymentPreference
    final case class  ExistingFrequency(bankAccountDetails: Option[BankAccountDetails], eldestChild: EldestChild) extends PaymentPreference
    final case object DoNotPay extends PaymentPreference
  }

  final case class Applicant(
                              name: AdultName,
                              previousFamilyNames: List[String],
                              dateOfBirth: LocalDate,
                              nationalInsuranceNumber: Option[String],
                              currentAddress: Address,
                              previousAddress: Option[Address],
                              telephoneNumber: String,
                              bestTimeToContact: Set[BestTimeToContact],
                              nationality: String,
                              employmentStatus: Set[ApplicantEmploymentStatus]
                            )

  final case class Partner(
                            name: AdultName,
                            dateOfBirth: LocalDate,
                            nationality: String,
                            nationalInsuranceNumber: Option[String],
                            currentlyEntitledToChildBenefit: Boolean,
                            waitingToHearAboutEntitlement: Option[Boolean],
                            eldestChild: Option[EldestChild]
                          )

  final case class Child(
                          name: ChildName,
                          nameChangedByDeedPoll: Option[Boolean],
                          previousNames: List[ChildName],
                          biologicalSex: ChildBiologicalSex,
                          countryOfRegistration: ChildBirthRegistrationCountry,
                          birtCertificateSystemNumber: Option[String],
                          scottishBirthCertificateDetails: Option[ChildScottishBirthCertificateDetails],
                          relationshipToApplicant: ApplicantRelationshipToChild,
                          adoptingThroughLocalAuthority: Option[Boolean],
                          previousClaimant: Option[PreviousClaimant],
                          documents: Set[IncludedDocuments]
                        )

  final case class PreviousClaimant(name: AdultName, address: Address)

  def from(answers: UserAnswers): IorNec[Query, JourneyModel] =
    (
      getApplicant(answers),
      getRelationship(answers),
      getChildren(answers),
      getBenefits(answers),
      getPaymentPreference(answers)
    ).parMapN(JourneyModel.apply)

  private def getBenefits(answers: UserAnswers): IorNec[Query, Set[Benefits]] = {

    import models.RelationshipStatus._

    answers.getIor(RelationshipStatusPage).flatMap {
      case Married | Cohabiting =>
        answers.getIor(ApplicantOrPartnerBenefitsPage)

      case Single | Separated | Divorced | Widowed =>
        answers.getIor(ApplicantBenefitsPage)
    }
  }

  private def getChildren(answers: UserAnswers): IorNec[Query, List[Child]] = {

    def getChild(index: Index): IorNec[Query, Child] = {

      def getNameChangedByDeedPoll: IorNec[Query, Option[Boolean]] =
        answers.getIor(ChildHasPreviousNamePage(index)).flatMap {
          case true => answers.getIor(ChildNameChangedByDeedPollPage(index)).map(Some(_))
          case false => Ior.Right(None)
        }

      def getPreviousNames: IorNec[Query, List[ChildName]] =
        answers.getIor(ChildHasPreviousNamePage(index)).flatMap {
          case true  => answers.getIor(AllChildPreviousNames(index))
          case false => Ior.Right(Nil)
        }

      def getSystemNumber: IorNec[Query, Option[String]] =
        answers.getIor(ChildBirthRegistrationCountryPage(index)).flatMap {
          case Country.England | Country.Wales => answers.getIor(ChildBirthCertificateSystemNumberPage(index)).map(Some(_))
          case _                               => Ior.Right(None)
        }

      def getScottishDetails: IorNec[Query, Option[ChildScottishBirthCertificateDetails]] =
        answers.getIor(ChildBirthRegistrationCountryPage(index)).flatMap {
          case Country.Scotland  => answers.getIor(ChildScottishBirthCertificateDetailsPage(index)).map(Some(_))
          case _                 => Ior.Right(None)
        }

      def getPreviousClaimant: IorNec[Query, Option[PreviousClaimant]] =
        answers.getIor(AnyoneClaimedForChildBeforePage(index)).flatMap {
          case true =>
            (
              answers.getIor(PreviousClaimantNamePage(index)),
              answers.getIor(PreviousClaimantAddressPage(index))
            ).parMapN(PreviousClaimant.apply).map(Some(_))

          case false =>
            Ior.Right(None)
        }

      def getDocuments: IorNec[Query, Set[IncludedDocuments]] =
        answers.getIor(ChildBirthRegistrationCountryPage(index)).flatMap {
          case Country.Other | Country.Unknown => answers.getIor(IncludedDocumentsPage(index))
          case _                               => Ior.Right(Set.empty)
        }

      def getAdoptingThroughLocalAuthority: IorNec[Query, Option[Boolean]] =
        answers.getIor(ApplicantRelationshipToChildPage(index)).flatMap {
          case ApplicantRelationshipToChild.AdoptingChild => answers.getIor(AdoptingThroughLocalAuthorityPage(index)).map(Some(_))
          case _                                          => Ior.Right(None)
        }

      (
        answers.getIor(ChildNamePage(index)),
        getNameChangedByDeedPoll,
        getPreviousNames,
        answers.getIor(ChildBiologicalSexPage(index)),
        answers.getIor(ChildBirthRegistrationCountryPage(index)),
        getSystemNumber,
        getScottishDetails,
        answers.getIor(ApplicantRelationshipToChildPage(index)),
        getAdoptingThroughLocalAuthority,
        getPreviousClaimant,
        getDocuments
      ).parMapN(Child.apply)
    }

    answers.get(AllChildSummaries).getOrElse(Nil).indices.toList.parTraverse { i =>
      getChild(Index(i))
    }
  }

  private def getApplicant(answers: UserAnswers): IorNec[Query, Applicant] = {

    def getNino: IorNec[Query, Option[String]] =
      answers.getIor(ApplicantNinoKnownPage).flatMap {
        case true  => answers.getIor(ApplicantNinoPage).map(nino => Some(nino.value))
        case false => Ior.Right(None)
      }

    def getPreviousAddress: IorNec[Query, Option[Address]] =
      answers.getIor(ApplicantLivedAtCurrentAddressOneYearPage).flatMap {
        case true  => Ior.Right(None)
        case false => answers.getIor(ApplicantPreviousAddressPage).map(Some(_))
      }

    def getPreviousFamilyNames: IorNec[Query, List[String]] =
      answers.getIor(ApplicantHasPreviousFamilyNamePage).flatMap {
        case true  => answers.getIor(AllPreviousFamilyNames)
        case false => Ior.Right(Nil)
      }

    (
      answers.getIor(ApplicantNamePage),
      getPreviousFamilyNames,
      answers.getIor(ApplicantDateOfBirthPage),
      getNino,
      answers.getIor(ApplicantCurrentAddressPage),
      getPreviousAddress,
      answers.getIor(ApplicantPhoneNumberPage),
      answers.getIor(BestTimeToContactPage),
      answers.getIor(ApplicantNationalityPage),
      answers.getIor(ApplicantEmploymentStatusPage)
    ).parMapN(Applicant.apply)
  }

  private def getPartner(answers: UserAnswers): IorNec[Query, Partner] = {

    def getPartnerNino: IorNec[Query, Option[String]] =
      answers.getIor(PartnerNinoKnownPage).flatMap {
        case true  => answers.getIor(PartnerNinoPage).map(nino => Some(nino.value))
        case false => Ior.Right(None)
      }

    def getPartnerWaitingToHear: IorNec[Query, Option[Boolean]] =
      answers.getIor(PartnerEntitledToChildBenefitPage).flatMap {
        case false => answers.getIor(PartnerWaitingForEntitlementDecisionPage).map(Some(_))
        case true  => Ior.Right(None)
      }

    def getPartnerEldestChild: IorNec[Query, Option[EldestChild]] = {

      def getDetails: IorNec[Query, Some[EldestChild]] = (
        answers.getIor(PartnerEldestChildNamePage),
        answers.getIor(PartnerEldestChildDateOfBirthPage)
      ).parMapN { (name, dateOfBirth) => Some(EldestChild(name, dateOfBirth)) }

      answers.getIor(PartnerEntitledToChildBenefitPage).flatMap {
        case true =>
          getDetails

        case false =>
          answers.getIor(PartnerWaitingForEntitlementDecisionPage).flatMap {
            case true  => getDetails
            case false => Ior.Right(None)
          }
      }
    }

    (
      answers.getIor(PartnerNamePage),
      answers.getIor(PartnerDateOfBirthPage),
      answers.getIor(PartnerNationalityPage),
      getPartnerNino,
      answers.getIor(PartnerEntitledToChildBenefitPage),
      getPartnerWaitingToHear,
      getPartnerEldestChild
    ).parMapN(Partner.apply)
  }

  private def getRelationship(answers: UserAnswers): IorNec[Query, Relationship] = {

    import models.RelationshipStatus._

    answers.getIor(RelationshipStatusPage).flatMap {
      case Married =>
        getPartner(answers).flatMap { partner =>
          Ior.Right(Relationship(Married, None, Some(partner)))
        }

      case Cohabiting =>
        (
          answers.getIor(CohabitationDatePage).map(Some(_)),
          getPartner(answers).map(Some(_))
        ).parMapN(Relationship(Cohabiting, _, _))

      case Separated =>
        answers.getIor(SeparationDatePage).flatMap { separationDate =>
          Ior.Right(Relationship(Separated, Some(separationDate), None))
        }

      case Single =>
        Ior.Right(Relationship(Single, None, None))

      case Divorced =>
        Ior.Right(Relationship(Divorced, None, None))

      case Widowed =>
        Ior.Right(Relationship(Widowed, None, None))
    }
  }

  private def getPaymentPreference(answers: UserAnswers): IorNec[Query, PaymentPreference] = {

    import PaymentPreference._

    def getAccountDetails: IorNec[Query, Option[BankAccountDetails]] =
      answers.getIor(ApplicantHasSuitableAccountPage).flatMap {
        case true  => answers.getIor(BankAccountDetailsPage).map(Some(_))
        case false => Ior.Right(None)
      }

    def getEldestChild: IorNec[Query, EldestChild] =
      (
        answers.getIor(EldestChildNamePage),
        answers.getIor(EldestChildDateOfBirthPage)
      ).parMapN(EldestChild.apply)

    def getPaymentDetails: IorNec[Query, PaymentPreference] =
      answers.getIor(WantToBePaidPage).flatMap {
        case true =>
          answers.get(WantToBePaidWeeklyPage) match {
            case Some(true) => getAccountDetails.map(Weekly)
            case _          => getAccountDetails.map(EveryFourWeeks)
          }

        case false =>
          Ior.Right(DoNotPay)
      }

    (
      answers.getIor(ClaimedChildBenefitBeforePage) &&
      answers.getIor(CurrentlyEntitledToChildBenefitPage) &&
      answers.getIor(CurrentlyReceivingChildBenefitPage)
    ).flatMap {
      case true =>
        answers.getIor(WantToBePaidToExistingAccountPage).flatMap {
          case true  => getEldestChild.map(ExistingAccount)
          case false => (getAccountDetails, getEldestChild).parMapN(ExistingFrequency.apply)
        }

      case false =>
        getPaymentDetails
    }
  }

  implicit class BooleanMonadSyntax[F[_]](fa: F[Boolean])(implicit m: Monad[F]) {

    def &&(that: F[Boolean]): F[Boolean] = fa.flatMap {
      case false => m.pure(false)
      case true  => that
    }
  }
}

