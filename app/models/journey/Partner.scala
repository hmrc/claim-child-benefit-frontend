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
import models.{AdultName, Country, EmploymentStatus, Nationality, PartnerClaimingChildBenefit, UserAnswers}
import pages.partner._
import queries.{AllCountriesPartnerReceivedBenefits, AllCountriesPartnerWorked, AllPartnerNationalities, Query}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

final case class Partner(
                          name: AdultName,
                          dateOfBirth: LocalDate,
                          nationalities: NonEmptyList[Nationality],
                          nationalInsuranceNumber: Option[Nino],
                          memberOfHMForcesOrCivilServantAbroad: Boolean,
                          currentlyClaimingChildBenefit: PartnerClaimingChildBenefit,
                          eldestChild: Option[EldestChild],
                          countriesWorked: List[Country],
                          countriesReceivedBenefits: List[Country],
                          employmentStatus: Set[EmploymentStatus]
                        )

object Partner {

  def build(answers: UserAnswers): IorNec[Query, Partner] = {

    import models.PartnerClaimingChildBenefit._

    def getPartnerNino: IorNec[Query, Option[Nino]] =
      answers.getIor(PartnerNinoKnownPage).flatMap {
        case true => answers.getIor(PartnerNinoPage).map(nino => Some(nino))
        case false => Ior.Right(None)
      }

    def getPartnerEldestChild: IorNec[Query, Option[EldestChild]] =
      EldestChild.buildPartnerEldestChild(answers)

    def getNationalities: IorNec[Query, NonEmptyList[Nationality]] = {
      val nationalities = answers.get(AllPartnerNationalities).getOrElse(Nil)
      NonEmptyList.fromList(nationalities).toRightIor(NonEmptyChain.one(AllPartnerNationalities))
    }

    def getCountriesWorked: IorNec[Query, List[Country]] =
      answers.getIor(PartnerWorkedAbroadPage).flatMap {
        case true => answers.getIor(AllCountriesPartnerWorked)
        case false => Ior.Right(Nil)
      }

    def getCountriesReceivedBenefits: IorNec[Query, List[Country]] =
      answers.getIor(PartnerReceivedBenefitsAbroadPage).flatMap {
        case true => answers.getIor(AllCountriesPartnerReceivedBenefits)
        case false => Ior.Right(Nil)
      }

    (
      answers.getIor(PartnerNamePage),
      answers.getIor(PartnerDateOfBirthPage),
      getNationalities,
      getPartnerNino,
      answers.getIor(PartnerIsHmfOrCivilServantPage),
      answers.getIor(PartnerClaimingChildBenefitPage),
      getPartnerEldestChild,
      getCountriesWorked,
      getCountriesReceivedBenefits,
      answers.getIor(PartnerEmploymentStatusPage)
    ).parMapN(Partner.apply)
  }
}
