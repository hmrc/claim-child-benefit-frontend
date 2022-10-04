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

import cats.data.{Ior, IorNec, NonEmptyChain, NonEmptyList}
import cats.implicits._
import models.ApplicantRelationshipToChild.AdoptedChild
import models.ChildBirthRegistrationCountry._
import models.DocumentType.{AdoptionCertificate, BirthCertificate, TravelDocument}
import models.JourneyModel._
import models.{ChildBirthRegistrationCountry => RegistrationCountry}
import pages._
import pages.applicant._
import pages.child._
import pages.income._
import pages.partner._
import pages.payments._
import queries.{AllChildPreviousNames, AllChildSummaries, AllPreviousFamilyNames, Query}

import java.time.LocalDate

final case class JourneyModel(
                               applicant: Applicant,
                               relationship: Relationship,
                               children: NonEmptyList[Child],
                               benefits: Set[Benefits],
                               paymentPreference: PaymentPreference,
                               additionalInformation: AdditionalInformation
                             ) {

  val allRequiredDocuments: List[RequiredDocument] =
  children.toList.flatMap { child =>
    child.requiredDocuments.map(doc => RequiredDocument(child.name, doc))
  }

  val anyDocumentsRequired: Boolean = allRequiredDocuments.nonEmpty
}

object JourneyModel {

  final case class Relationship(status: RelationshipStatus, since: Option[LocalDate], partner: Option[Partner])
  final case class EldestChild(name: ChildName, dateOfBirth: LocalDate)

  final case class BankAccount(holder: BankAccountHolder, details: BankAccountDetails)

  sealed trait PaymentPreference

  object PaymentPreference {

    final case class Weekly(bankAccount: Option[BankAccount], eldestChild: Option[EldestChild]) extends PaymentPreference
    final case class EveryFourWeeks(bankAccount: Option[BankAccount], eldestChild: Option[EldestChild]) extends PaymentPreference
    final case class ExistingAccount(eldestChild: EldestChild, frequency: PaymentFrequency) extends PaymentPreference
    final case class DoNotPay(eldestChild: Option[EldestChild]) extends PaymentPreference
  }

  final case class Applicant(
                              name: AdultName,
                              previousFamilyNames: List[String],
                              dateOfBirth: LocalDate,
                              nationalInsuranceNumber: Option[String],
                              currentAddress: Address,
                              previousAddress: Option[Address],
                              telephoneNumber: String,
                              nationality: String,
                              alwaysLivedInUk: Boolean,
                              memberOfHMForcesOrCivilServantAbroad: Option[Boolean],
                              currentlyReceivingChildBenefit: CurrentlyReceivingChildBenefit
                            )

  final case class Partner(
                            name: AdultName,
                            dateOfBirth: LocalDate,
                            nationality: String,
                            nationalInsuranceNumber: Option[String],
                            memberOfHMForcesOrCivilServantAbroad: Option[Boolean],
                            currentlyClaimingChildBenefit: PartnerClaimingChildBenefit,
                            eldestChild: Option[EldestChild]
                          )

  final case class Child(
                          name: ChildName,
                          nameChangedByDeedPoll: Option[Boolean],
                          previousNames: List[ChildName],
                          biologicalSex: ChildBiologicalSex,
                          dateOfBirth: LocalDate,
                          countryOfRegistration: ChildBirthRegistrationCountry,
                          birthCertificateNumber: Option[String],
                          relationshipToApplicant: ApplicantRelationshipToChild,
                          adoptingThroughLocalAuthority: Boolean,
                          previousClaimant: Option[PreviousClaimant]
                        ) {

    private val adoptionCertificate =
      if (relationshipToApplicant == AdoptedChild) Some(AdoptionCertificate) else None

    private val (birthCertificate, travelDocument) = countryOfRegistration match {
      case England | Scotland | Wales      => (None, None)
      case _ if previousClaimant.isDefined => (None, None)
      case NorthernIreland                 => (Some(BirthCertificate), None)
      case _                               => (Some(BirthCertificate), Some(TravelDocument))
    }

    val requiredDocuments: Seq[DocumentType] =
      Seq(birthCertificate, travelDocument, adoptionCertificate).flatten
  }

  final case class PreviousClaimant(name: AdultName, address: Address)

  def from(answers: UserAnswers): IorNec[Query, JourneyModel] =
    (
      getApplicant(answers),
      getRelationship(answers),
      getChildren(answers),
      getBenefits(answers),
      getPaymentPreference(answers),
      answers.getIor(AdditionalInformationPage)
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

  private def getChildren(answers: UserAnswers): IorNec[Query, NonEmptyList[Child]] = {

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

      def getBirthCertificateNumber: IorNec[Query, Option[String]] =
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
                answers.getIor(ChildScottishBirthCertificateDetailsPage(index)).map(x => Some(x.toString))
              case false =>
                Ior.Right(None)
            }

          case _ =>
            Ior.Right(None)
        }

      def getPreviousClaimant: IorNec[Query, Option[PreviousClaimant]] =
        answers.getIor(AnyoneClaimedForChildBeforePage(index)).flatMap {
          case true =>
            (
              answers.getIor(PreviousClaimantNamePage(index)),
              getPreviousClaimantAddress
            ).parMapN(PreviousClaimant.apply).map(Some(_))

          case false =>
            Ior.Right(None)
        }

      def getPreviousClaimantAddress: IorNec[Query, Address] =
        answers.getIor(PreviousClaimantAddressInUkPage(index)).flatMap {
          case true  => answers.getIor(PreviousClaimantUkAddressPage(index))
          case false => answers.getIor(PreviousClaimantInternationalAddressPage(index))
        }

      (
        answers.getIor(ChildNamePage(index)),
        getNameChangedByDeedPoll,
        getPreviousNames,
        answers.getIor(ChildBiologicalSexPage(index)),
        answers.getIor(ChildDateOfBirthPage(index)),
        answers.getIor(ChildBirthRegistrationCountryPage(index)),
        getBirthCertificateNumber,
        answers.getIor(ApplicantRelationshipToChildPage(index)),
        answers.getIor(AdoptingThroughLocalAuthorityPage(index)),
        getPreviousClaimant
      ).parMapN(Child.apply)
    }

    answers.getIor(AllChildSummaries).getOrElse(Nil).indices.toList.parTraverse { i =>
      getChild(Index(i))
    }.flatMap { children =>
      NonEmptyList.fromList(children).toRightIor(NonEmptyChain.one(AllChildSummaries))
    }
  }

  private def getApplicant(answers: UserAnswers): IorNec[Query, Applicant] = {

    def getNino: IorNec[Query, Option[String]] =
      answers.getIor(ApplicantNinoKnownPage).flatMap {
        case true  => answers.getIor(ApplicantNinoPage).map(nino => Some(nino.value))
        case false => Ior.Right(None)
      }

    def getPreviousAddress: IorNec[Query, Option[Address]] =
      answers.getIor(ApplicantNinoKnownPage).flatMap {
        case true =>
          Ior.Right(None)

        case false =>
          answers.getIor(ApplicantLivedAtCurrentAddressOneYearPage).flatMap {
            case true =>
              Ior.Right(None)

            case false =>
              answers.getIor(AlwaysLivedInUkPage).flatMap {
                case true =>
                  answers.getIor(ApplicantPreviousUkAddressPage).map(Some(_))

                case false =>
                  answers.getIor(ApplicantPreviousAddressInUkPage).flatMap {
                    case true  => answers.getIor(ApplicantPreviousUkAddressPage).map(Some(_))
                    case false => answers.getIor(ApplicantPreviousInternationalAddressPage).map(Some(_))
                  }
              }
          }
      }

    def getCurrentAddress: IorNec[Query, Address] =
      if (answers.get(ApplicantCurrentAddressInUkPage).getOrElse(true)) {
        answers.getIor(ApplicantCurrentUkAddressPage)
      } else {
        answers.getIor(ApplicantCurrentInternationalAddressPage)
      }

    def getPreviousFamilyNames: IorNec[Query, List[String]] =
      answers.getIor(ApplicantHasPreviousFamilyNamePage).flatMap {
        case true  => answers.getIor(AllPreviousFamilyNames)
        case false => Ior.Right(Nil)
      }

    def getHmForces: IorNec[Query, Option[Boolean]] =
      answers.get(ApplicantIsHmfOrCivilServantPage)
        .map(x => Ior.Right(Some(x)))
        .getOrElse(Ior.Right(None))

    (
      answers.getIor(ApplicantNamePage),
      getPreviousFamilyNames,
      answers.getIor(ApplicantDateOfBirthPage),
      getNino,
      getCurrentAddress,
      getPreviousAddress,
      answers.getIor(ApplicantPhoneNumberPage),
      answers.getIor(ApplicantNationalityPage),
      answers.getIor(AlwaysLivedInUkPage),
      getHmForces,
      answers.getIor(CurrentlyReceivingChildBenefitPage)
    ).parMapN(Applicant.apply)
  }

  private def getPartner(answers: UserAnswers): IorNec[Query, Partner] = {

    import models.PartnerClaimingChildBenefit._

    def getPartnerNino: IorNec[Query, Option[String]] =
      answers.getIor(PartnerNinoKnownPage).flatMap {
        case true  => answers.getIor(PartnerNinoPage).map(nino => Some(nino.value))
        case false => Ior.Right(None)
      }

    def getPartnerEldestChild: IorNec[Query, Option[EldestChild]] = {

      def getDetails: IorNec[Query, Some[EldestChild]] = (
        answers.getIor(PartnerEldestChildNamePage),
        answers.getIor(PartnerEldestChildDateOfBirthPage)
      ).parMapN { (name, dateOfBirth) => Some(EldestChild(name, dateOfBirth)) }

      answers.getIor(PartnerClaimingChildBenefitPage).flatMap {
        case GettingPayments | NotGettingPayments | WaitingToHear =>
          getDetails

        case NotClaiming =>
          Ior.Right(None)
      }
    }

    def getHmForces: IorNec[Query, Option[Boolean]] =
      answers.get(PartnerIsHmfOrCivilServantPage)
        .map(x => Ior.Right(Some(x)))
        .getOrElse(Ior.Right(None))

    (
      answers.getIor(PartnerNamePage),
      answers.getIor(PartnerDateOfBirthPage),
      answers.getIor(PartnerNationalityPage),
      getPartnerNino,
      getHmForces,
      answers.getIor(PartnerClaimingChildBenefitPage),
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

    import CurrentlyReceivingChildBenefit._
    import PaymentPreference._

    def getBankAccount: IorNec[Query, Option[BankAccount]] =
      answers.getIor(ApplicantHasSuitableAccountPage).flatMap {
        case true  =>
          (
            answers.getIor(BankAccountHolderPage),
            answers.getIor(BankAccountDetailsPage)
          ).parMapN(BankAccount.apply).map(Some(_))
        case false =>
          Ior.Right(None)
      }

    def getEldestChild: IorNec[Query, EldestChild] =
      (
        answers.getIor(EldestChildNamePage),
        answers.getIor(EldestChildDateOfBirthPage)
      ).parMapN(EldestChild.apply)

    def getWeeklyOrEveryFourWeeksWithChild: IorNec[Query, PaymentPreference] =
      answers.get(PaymentFrequencyPage) match {
        case Some(PaymentFrequency.Weekly) =>
          (
            getBankAccount,
            getEldestChild
            ).parMapN((bank, child) => Weekly(bank, Some(child)))

        case _ =>
          (
            getBankAccount,
            getEldestChild
            ).parMapN((bank, child) => EveryFourWeeks(bank, Some(child)))
      }

    answers.getIor(CurrentlyReceivingChildBenefitPage).flatMap {
      case GettingPayments =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            answers.getIor(WantToBePaidToExistingAccountPage).flatMap {
              case true =>
                getEldestChild
                  .map {
                    child =>
                      ExistingAccount(
                        child,
                        answers.get(PaymentFrequencyPage)
                          .getOrElse(PaymentFrequency.EveryFourWeeks))
                  }

              case false =>
                getWeeklyOrEveryFourWeeksWithChild
            }

          case false =>
            getEldestChild.map(x => DoNotPay(Some(x)))
        }

      case NotClaiming =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            answers.get(PaymentFrequencyPage) match {
              case Some(PaymentFrequency.Weekly) => getBankAccount.map(bank => Weekly(bank, None))
              case _                             => getBankAccount.map(bank => EveryFourWeeks(bank, None))
            }

          case false =>
            Ior.Right(DoNotPay(None))
        }

      case NotGettingPayments =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            getWeeklyOrEveryFourWeeksWithChild

          case false =>
            getEldestChild.map(child => DoNotPay(Some(child)))
        }
    }
  }
}

