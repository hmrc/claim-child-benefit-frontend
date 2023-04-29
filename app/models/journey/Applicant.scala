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
import models.ApplicantResidence.{AlwaysAbroad, AlwaysUk, UkAndAbroad}
import models.{Address, AdultName, ApplicantPreviousName, CurrentlyReceivingChildBenefit, Nationality, NationalityGroup, NationalityGroupOrdering, UserAnswers}
import pages.applicant._
import queries.{AllApplicantNationalities, AllPreviousFamilyNames, Query}

import java.time.LocalDate


final case class Applicant(
                            name: AdultName,
                            previousFamilyNames: List[ApplicantPreviousName],
                            dateOfBirth: LocalDate,
                            nationalInsuranceNumber: Option[String],
                            currentAddress: Address,
                            previousAddress: Option[Address],
                            telephoneNumber: String,
                            nationalities: NonEmptyList[Nationality],
                            residency: Residency,
                            memberOfHMForcesOrCivilServantAbroad: Boolean,
                            currentlyReceivingChildBenefit: CurrentlyReceivingChildBenefit,
                            changedDesignatoryDetails: Option[Boolean],
                            correspondenceAddress: Option[Address]
                          ) {

  lazy val nationalityGroupToUse: NationalityGroup =
    nationalities
      .map(_.group)
      .toList
      .sorted(NationalityGroupOrdering)
      .head
}

object Applicant {

  def build(answers: UserAnswers): IorNec[Query, Applicant] =
    (
      getName(answers),
      getPreviousFamilyNames(answers),
      getDateOfBirth(answers),
      getNino(answers),
      getCurrentAddress(answers),
      getPreviousAddress(answers),
      answers.getIor(ApplicantPhoneNumberPage),
      getNationalities(answers),
      Residency.build(answers),
      answers.getIor(ApplicantIsHmfOrCivilServantPage),
      getCurrentlyReceivingChildBenefit(answers),
      designatoryDetailsChanged(answers),
      getCorrespondenceAddress(answers)
    ).parMapN(Applicant.apply)

  private def getCurrentlyReceivingChildBenefit(answers: UserAnswers): IorNec[Query, CurrentlyReceivingChildBenefit] =
    answers.getIor(CurrentlyReceivingChildBenefitPage)

  private def designatoryDetailsChanged(answers: UserAnswers): IorNec[Query, Option[Boolean]] =
    if (answers.isAuthenticated) {
      if (answers.isDefined(DesignatoryNamePage) ||
        answers.isDefined(DesignatoryAddressInUkPage) ||
        answers.isDefined(CorrespondenceAddressInUkPage)) {

        Ior.Right(Some(true))
      } else {
        Ior.Right(Some(false))
      }
    } else {
      Ior.Right(None)
    }

  private def getNino(answers: UserAnswers): IorNec[Query, Option[String]] =
    answers.nino
      .map(nino => Ior.Right(Some(nino)))
      .getOrElse {
        answers.getIor(ApplicantNinoKnownPage).flatMap {
          case true => answers.getIor(ApplicantNinoPage).map(nino => Some(nino.value))
          case false => Ior.Right(None)
        }
      }

  private def getName(answers: UserAnswers): IorNec[Query, AdultName] =
    if (answers.isAuthenticated) {
      answers.designatoryDetails.map {
        details =>
          details.preferredName.map {
            originalName =>
              val name = answers.get(DesignatoryNamePage).getOrElse(originalName)
              Ior.Right(name)
          }.getOrElse(Ior.Left(NonEmptyChain(DesignatoryNamePage)))
      }.getOrElse(Ior.Left(NonEmptyChain(DesignatoryNamePage)))
    } else {
      answers.getIor(ApplicantNamePage)
    }

  private def getDateOfBirth(answers: UserAnswers): IorNec[Query, LocalDate] =
    if (answers.isAuthenticated) {
      answers.designatoryDetails
        .map(details => Ior.Right(details.dateOfBirth))
        .getOrElse(Ior.Left(NonEmptyChain(ApplicantDateOfBirthPage)))
    } else {
      answers.getIor(ApplicantDateOfBirthPage)
    }

  private def getPreviousAddress(answers: UserAnswers): IorNec[Query, Option[Address]] =
    if (answers.isAuthenticated) {
      Ior.Right(None)
    } else {
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

  private def getCurrentAddress(answers: UserAnswers): IorNec[Query, Address] =
    if (answers.isAuthenticated) {
      answers.get(DesignatoryAddressInUkPage).map {
        case true => answers.getIor(DesignatoryUkAddressPage)
        case false => answers.getIor(DesignatoryInternationalAddressPage)
      }.getOrElse {
        answers.designatoryDetails.flatMap { details =>
          details.residentialAddress.map(Ior.Right(_))
        }.getOrElse(Ior.Left(NonEmptyChain(DesignatoryAddressInUkPage)))
      }
    } else {
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

  private def getCorrespondenceAddress(answers: UserAnswers): IorNec[Query, Option[Address]] =
    if (answers.isAuthenticated) {
      answers.get(CorrespondenceAddressInUkPage).map {
        case true => answers.getIor(CorrespondenceUkAddressPage).map(Some(_))
        case false => answers.getIor(CorrespondenceInternationalAddressPage).map(Some(_))
      }.getOrElse {
        answers.designatoryDetails.map { details =>
          Ior.Right(details.correspondenceAddress)
        }.getOrElse(Ior.Left(NonEmptyChain(CorrespondenceAddressInUkPage)))
      }
    } else {
      Ior.Right(None)
    }

  private def getPreviousFamilyNames(answers: UserAnswers): IorNec[Query, List[ApplicantPreviousName]] =
    if (answers.isAuthenticated) {
      Ior.Right(Nil)
    } else {
      answers.getIor(ApplicantHasPreviousFamilyNamePage).flatMap {
        case true => answers.getIor(AllPreviousFamilyNames)
        case false => Ior.Right(Nil)
      }
    }

  def getNationalities(answers: UserAnswers): IorNec[Query, NonEmptyList[Nationality]] = {
    val nationalities = answers.get(AllApplicantNationalities).getOrElse(Nil)
    NonEmptyList.fromList(nationalities).toRightIor(NonEmptyChain.one(AllApplicantNationalities))
  }
}
