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

package models

import cats.data._
import cats.implicits._
import logging.Logging
import models.ApplicantResidence._
import models.JourneyModel._
import models.{ChildBirthRegistrationCountry => RegistrationCountry}
import pages._
import pages.applicant._
import pages.child._
import pages.partner._
import pages.payments._
import queries._
import services.BrmsService
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyModelProvider @Inject()(brmsService: BrmsService)(implicit ec: ExecutionContext) extends Logging {

  def buildFromUserAnswers(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[IorNec[Query, JourneyModel]] =
      (
        IorT.fromIor[Future](getApplicant(answers)),
        IorT.fromIor[Future](getRelationship(answers)),
        getChildren(answers),
        IorT.fromIor[Future](getBenefits(answers)),
        IorT.fromIor[Future](getPaymentPreference(answers)),
        IorT.fromIor[Future](answers.getIor(AdditionalInformationPage))
      ).parMapN(JourneyModel.apply).value

  private def getBenefits(answers: UserAnswers): IorNec[Query, Option[Set[Benefits]]] = {

    import models.RelationshipStatus._

    answers.getIor(RelationshipStatusPage).flatMap {
      case Married | Cohabiting =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true => answers.getIor(ApplicantOrPartnerBenefitsPage).map(Some(_))
          case false => Ior.Right(None)
        }

      case Single | Separated | Divorced | Widowed =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true => answers.getIor(ApplicantBenefitsPage).map(Some(_))
          case false => Ior.Right(None)
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
        answers.getIor(AnyoneClaimedForChildBeforePage(index)).flatMap {
          case true =>
            val name = answers.getIor(PreviousClaimantNameKnownPage(index)).flatMap {
              case true => answers.getIor(PreviousClaimantNamePage(index)).map(Some(_))
              case false => Ior.Right(None)
            }

            val address = answers.getIor(PreviousClaimantNameKnownPage(index)).flatMap {
              case true => getPreviousClaimantAddress
              case false => Ior.Right(None)
            }
            (
              name,
              address
            ).parMapN(PreviousClaimant.apply).map(Some(_))

          case false =>
            Ior.Right(None)
        }

      def getPreviousClaimantAddress: IorNec[Query, Option[Address]] = {
        answers.getIor(PreviousClaimantAddressKnownPage(index)).flatMap {
          case true =>
            answers.getIor(PreviousClaimantAddressInUkPage(index)).flatMap {
              case true => answers.getIor(PreviousClaimantUkAddressPage(index)).map(Some(_))
              case false => answers.getIor(PreviousClaimantInternationalAddressPage(index)).map(Some(_))
            }

          case false =>
            Ior.Right(None)
        }
      }

      def getGuardian: IorNec[Query, Option[Guardian]] =
        answers.getIor(ChildLivesWithApplicantPage(index)).flatMap {
          case true =>
            Ior.Right(None)

          case false =>
            val name = answers.getIor(GuardianNameKnownPage(index)).flatMap {
              case true => answers.getIor(GuardianNamePage(index)).map(Some(_))
              case false => Ior.Right(None)
            }
            val address = answers.getIor(GuardianNameKnownPage(index)).flatMap {
              case true => getGuardianAddress
              case false => Ior.Right(None)
            }
            (
              name,
              address
            ).parMapN(Guardian.apply).map(Some(_))
        }

      def getGuardianAddress: IorNec[Query, Option[Address]] = {
        answers.getIor(GuardianAddressKnownPage(index)).flatMap {
          case true =>
            answers.getIor(GuardianAddressInUkPage(index)).flatMap {
              case true => answers.getIor(GuardianUkAddressPage(index)).map(Some(_))
              case false => answers.getIor(GuardianInternationalAddressPage(index)).map(Some(_))
            }

          case false =>
            Ior.Right(None)
        }
      }

      def getPreviousGuardian: IorNec[Query, Option[PreviousGuardian]] =
        answers.getIor(ChildLivesWithApplicantPage(index)).flatMap {
          case true =>
            answers.getIor(ChildLivedWithAnyoneElsePage(index)).flatMap {
              case true =>

                val name = answers.getIor(PreviousGuardianNameKnownPage(index)).flatMap {
                  case true => answers.getIor(PreviousGuardianNamePage(index)).map(Some(_))
                  case false => Ior.Right(None)
                }
                val address = answers.getIor(PreviousGuardianNameKnownPage(index)).flatMap {
                  case true  => getPreviousGuardianAddress
                  case false => Ior.Right(None)
                }
                val phoneNumber = answers.getIor(PreviousGuardianNameKnownPage(index)).flatMap {
                  case true  => getPreviousGuardianPhoneNumber
                  case false => Ior.Right(None)
                }

                (
                  name,
                  address,
                  phoneNumber
                ).parMapN(PreviousGuardian.apply).map(Some(_))

              case false =>
                Ior.Right(None)
            }

          case false =>
            Ior.Right(None)
        }

      def getPreviousGuardianAddress: IorNec[Query, Option[Address]] = {
        answers.getIor(PreviousGuardianAddressKnownPage(index)).flatMap {
          case true =>
            answers.getIor(PreviousGuardianAddressInUkPage(index)).flatMap {
              case true => answers.getIor(PreviousGuardianUkAddressPage(index)).map(Some(_))
              case false => answers.getIor(PreviousGuardianInternationalAddressPage(index)).map(Some(_))
            }

          case false =>
            Ior.Right(None)
        }
      }

      def getPreviousGuardianPhoneNumber: IorNec[Query, Option[String]] = {
        answers.getIor(PreviousGuardianPhoneNumberKnownPage(index)).flatMap {
          case true =>
              answers.getIor(PreviousGuardianPhoneNumberPage(index)).map(Some(_))

          case false =>
            Ior.Right(None)
        }
      }

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

  private def getApplicant(answers: UserAnswers): IorNec[Query, Applicant] = {

    def getNino: IorNec[Query, Option[String]] =
      answers.getIor(ApplicantNinoKnownPage).flatMap {
        case true => answers.getIor(ApplicantNinoPage).map(nino => Some(nino.value))
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
              answers.getIor(ApplicantResidencePage).flatMap {
                case AlwaysUk =>
                  answers.getIor(ApplicantPreviousUkAddressPage).map(Some(_))

                case UkAndAbroad =>
                  answers.getIor(ApplicantPreviousAddressInUkPage).flatMap {
                    case true => answers.getIor(ApplicantPreviousUkAddressPage).map(Some(_))
                    case false => answers.getIor(ApplicantPreviousInternationalAddressPage).map(Some(_))
                  }

                case AlwaysAbroad =>
                  answers.getIor(ApplicantPreviousInternationalAddressPage).map(Some(_))
              }
          }
      }

    def getCurrentAddress: IorNec[Query, Address] = {
      answers.getIor(ApplicantResidencePage).flatMap {
        case AlwaysUk =>
          answers.getIor(ApplicantCurrentUkAddressPage)

        case UkAndAbroad =>
          answers.getIor(ApplicantCurrentAddressInUkPage).flatMap {
            case true => answers.getIor(ApplicantCurrentUkAddressPage)
            case false => answers.getIor(ApplicantCurrentInternationalAddressPage)
          }

        case AlwaysAbroad =>
          answers.getIor(ApplicantCurrentInternationalAddressPage)
      }
    }

    def getPreviousFamilyNames: IorNec[Query, List[ApplicantPreviousName]] =
      answers.getIor(ApplicantHasPreviousFamilyNamePage).flatMap {
        case true => answers.getIor(AllPreviousFamilyNames)
        case false => Ior.Right(Nil)
      }

    def getHmForces: IorNec[Query, Option[Boolean]] =
      answers.get(ApplicantIsHmfOrCivilServantPage)
        .map(x => Ior.Right(Some(x)))
        .getOrElse(Ior.Right(None))

    def getResidency: IorNec[Query, Residency] = {

      def getCountriesWorked: IorNec[Query, List[Country]] =
        answers.getIor(ApplicantWorkedAbroadPage).flatMap {
          case true => answers.getIor(AllCountriesApplicantWorked)
          case false => Ior.Right(Nil)
        }

      def getCountriesReceivedBenefits: IorNec[Query, List[Country]] =
        answers.getIor(ApplicantReceivedBenefitsAbroadPage).flatMap {
          case true => answers.getIor(AllCountriesApplicantReceivedBenefits)
          case false => Ior.Right(Nil)
        }

      answers.getIor(ApplicantResidencePage).flatMap {
        case AlwaysUk =>
          Ior.Right(Residency.AlwaysLivedInUk)

        case UkAndAbroad =>
          def getCountry = answers.getIor(ApplicantUsuallyLivesInUkPage).flatMap {
            case true => Ior.Right(None)
            case false => answers.getIor(ApplicantUsualCountryOfResidencePage).map(Some(_))
          }

          def getArrivalDate = answers.getIor(ApplicantCurrentAddressInUkPage).flatMap {
            case true => answers.getIor(ApplicantArrivedInUkPage).map(Some(_))
            case false => Ior.Right(None)
          }

          (
            getCountry,
            getArrivalDate,
            answers.getIor(ApplicantEmploymentStatusPage),
            getCountriesWorked,
            getCountriesReceivedBenefits
          ).parMapN(Residency.LivedInUkAndAbroad)

        case AlwaysAbroad =>
          (
            answers.getIor(ApplicantUsualCountryOfResidencePage),
            answers.getIor(ApplicantEmploymentStatusPage),
            getCountriesWorked,
            getCountriesReceivedBenefits
          ).parMapN(Residency.AlwaysLivedAbroad)

      }
    }

    def getNationalities: IorNec[Query, NonEmptyList[Nationality]] = {
      val nationalities = answers.get(AllApplicantNationalities).getOrElse(Nil)
      NonEmptyList.fromList(nationalities).toRightIor(NonEmptyChain.one(AllApplicantNationalities))
    }

    (
      answers.getIor(ApplicantNamePage),
      getPreviousFamilyNames,
      answers.getIor(ApplicantDateOfBirthPage),
      getNino,
      getCurrentAddress,
      getPreviousAddress,
      answers.getIor(ApplicantPhoneNumberPage),
      getNationalities,
      getResidency,
      getHmForces,
      answers.getIor(CurrentlyReceivingChildBenefitPage)
      ).parMapN(Applicant.apply)
  }

  private def getPartner(answers: UserAnswers): IorNec[Query, Partner] = {

    import models.PartnerClaimingChildBenefit._

    def getPartnerNino: IorNec[Query, Option[String]] =
      answers.getIor(PartnerNinoKnownPage).flatMap {
        case true => answers.getIor(PartnerNinoPage).map(nino => Some(nino.value))
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

    def getNationalities: IorNec[Query, NonEmptyList[Nationality]] = {
      val nationalities = answers.get(AllPartnerNationalities).getOrElse(Nil)
      NonEmptyList.fromList(nationalities).toRightIor(NonEmptyChain.one(AllPartnerNationalities))
    }

    (
      answers.getIor(PartnerNamePage),
      answers.getIor(PartnerDateOfBirthPage),
      getNationalities,
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

    def getBank: IorNec[Query, BankAccountWithHolder] =
      (
        answers.getIor(BankAccountHolderPage),
        answers.getIor(BankAccountDetailsPage)
      ).parMapN(BankAccountWithHolder.apply)

    def getBuildingSociety: IorNec[Query, BuildingSocietyWithHolder] =
      (
        answers.getIor(BankAccountHolderPage),
        answers.getIor(BuildingSocietyDetailsPage)
      ).parMapN(BuildingSocietyWithHolder.apply)

    def getAccountDetails: IorNec[Query, AccountDetailsWithHolder] =
      answers.getIor(AccountTypePage).flatMap {
        case AccountType.SortCodeAccountNumber     => getBank
        case AccountType.BuildingSocietyRollNumber => getBuildingSociety
      }

    def getBankAccount: IorNec[Query, Option[AccountDetailsWithHolder]] =
      answers.getIor(ApplicantHasSuitableAccountPage).flatMap {
        case true =>
          getAccountDetails.map(Some(_))
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
              case _ => getBankAccount.map(bank => EveryFourWeeks(bank, None))
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
