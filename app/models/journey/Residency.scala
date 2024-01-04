/*
 * Copyright 2024 HM Revenue & Customs
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
import models.ApplicantResidence._
import models.{Country, EmploymentStatus, UserAnswers}
import pages.applicant._
import queries.{AllCountriesApplicantReceivedBenefits, AllCountriesApplicantWorked, Query}

import java.time.LocalDate

sealed trait Residency

object Residency {

  case object AlwaysLivedInUk extends Residency

  final case class LivedInUkAndAbroad(
                                       usualCountryOfResidence: Option[Country],
                                       arrivalDate: Option[LocalDate],
                                       employmentStatus: Set[EmploymentStatus],
                                       countriesWorked: List[Country],
                                       countriesReceivedBenefits: List[Country]
                                     ) extends Residency

  final case class AlwaysLivedAbroad(
                                      usualCountryOfResidence: Country,
                                      employmentStatus: Set[EmploymentStatus],
                                      countriesWorked: List[Country],
                                      countriesReceivedBenefits: List[Country]
                                    ) extends Residency

  def build(answers: UserAnswers): IorNec[Query, Residency] =
    answers.getIor(ApplicantResidencePage).flatMap {
      case AlwaysUk =>
        Ior.Right(Residency.AlwaysLivedInUk)

      case UkAndAbroad =>
        def getCountry = answers.getIor(ApplicantUsuallyLivesInUkPage).flatMap {
          case true => Ior.Right(None)
          case false => answers.getIor(ApplicantUsualCountryOfResidencePage).map(Some(_))
        }

        def getArrivalDate: IorNec[Query, Option[LocalDate]] = {
          if (answers.isAuthenticated) {
            answers.get(DesignatoryAddressInUkPage).map {
              case true => answers.getIor(ApplicantArrivedInUkPage).map(Some(_))
              case false => Ior.Right(None)
            }.getOrElse {
              if (answers.designatoryDetails.exists(x => x.residentialAddress.exists(_.isUkAddress))) {
                answers.getIor(ApplicantArrivedInUkPage).map(Some(_))
              } else {
                Ior.Right(None)
              }
            }
          } else {
            answers.getIor(ApplicantCurrentAddressInUkPage).flatMap {
              case true => answers.getIor(ApplicantArrivedInUkPage).map(Some(_))
              case false => Ior.Right(None)
            }
          }
        }

        (
          getCountry,
          getArrivalDate,
          answers.getIor(ApplicantEmploymentStatusPage),
          getCountriesWorked(answers),
          getCountriesReceivedBenefits(answers)
        ).parMapN(Residency.LivedInUkAndAbroad)

      case AlwaysAbroad =>
        (
          answers.getIor(ApplicantUsualCountryOfResidencePage),
          answers.getIor(ApplicantEmploymentStatusPage),
          getCountriesWorked(answers),
          getCountriesReceivedBenefits(answers)
        ).parMapN(Residency.AlwaysLivedAbroad)

    }

  private def getCountriesWorked(answers: UserAnswers): IorNec[Query, List[Country]] =
    answers.getIor(ApplicantWorkedAbroadPage).flatMap {
      case true => answers.getIor(AllCountriesApplicantWorked)
      case false => Ior.Right(Nil)
    }

  private def getCountriesReceivedBenefits(answers: UserAnswers): IorNec[Query, List[Country]] =
    answers.getIor(ApplicantReceivedBenefitsAbroadPage).flatMap {
      case true => answers.getIor(AllCountriesApplicantReceivedBenefits)
      case false => Ior.Right(Nil)
    }
}
