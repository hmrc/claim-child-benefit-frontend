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

import cats.data.NonEmptyList
import generators.ModelGenerators
import models.{AdultName, ChildName, Country, EmploymentStatus, Index, Nationality, PartnerClaimingChildBenefit, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.partner._
import queries.{AllCountriesPartnerReceivedBenefits, AllCountriesPartnerWorked, AllPartnerNationalities}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class PartnerSpec extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues {

  private val index = Index(0)
  private val adultName = arbitrary[AdultName].sample.value
  private val childName = arbitrary[ChildName].sample.value
  private val nino = arbitrary[Nino].sample.value
  private val nationality = arbitrary[Nationality].sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value

  ".build" - {

    "must return a Partner when all answers are present (fullest case)" in {

      val answers =
        UserAnswers("id")
          .set(PartnerNamePage, adultName).success.value
          .set(PartnerNinoKnownPage, true).success.value
          .set(PartnerNinoPage, nino).success.value
          .set(PartnerDateOfBirthPage, LocalDate.now).success.value
          .set(PartnerNationalityPage(index), nationality).success.value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerWorkedAbroadPage, true).success.value
          .set(CountryPartnerWorkedPage(index), country).success.value
          .set(PartnerReceivedBenefitsAbroadPage, true).success.value
          .set(CountryPartnerReceivedBenefitsPage(index), country).success.value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments).success.value
          .set(PartnerEldestChildNamePage, childName).success.value
          .set(PartnerEldestChildDateOfBirthPage, LocalDate.now).success.value

      val (errors, data) = Partner.build(answers).pad

      data.value mustEqual Partner(
        name = adultName,
        dateOfBirth = LocalDate.now,
        nationalities = NonEmptyList(nationality, Nil),
        nationalInsuranceNumber = Some(nino),
        memberOfHMForcesOrCivilServantAbroad = false,
        currentlyClaimingChildBenefit = PartnerClaimingChildBenefit.GettingPayments,
        eldestChild = Some(EldestChild(childName, LocalDate.now)),
        countriesWorked = List(country),
        countriesReceivedBenefits = List(country),
        employmentStatus = EmploymentStatus.activeStatuses
      )
      errors must not be defined
    }

    "must return a Partner when all answers are present (sparsest case)" in {

      val answers =
        UserAnswers("id")
          .set(PartnerNamePage, adultName).success.value
          .set(PartnerNinoKnownPage, false).success.value
          .set(PartnerDateOfBirthPage, LocalDate.now).success.value
          .set(PartnerNationalityPage(index), nationality).success.value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerWorkedAbroadPage, false).success.value
          .set(PartnerReceivedBenefitsAbroadPage, false).success.value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming).success.value

      val (errors, data) = Partner.build(answers).pad

      data.value mustEqual Partner(
        name = adultName,
        dateOfBirth = LocalDate.now,
        nationalities = NonEmptyList(nationality, Nil),
        nationalInsuranceNumber = None,
        memberOfHMForcesOrCivilServantAbroad = false,
        currentlyClaimingChildBenefit = PartnerClaimingChildBenefit.NotClaiming,
        eldestChild = None,
        countriesWorked = Nil,
        countriesReceivedBenefits = Nil,
        employmentStatus = EmploymentStatus.activeStatuses
      )
      errors must not be defined
    }

    "must return errors when details are missing" in {

      val answers = UserAnswers("id")

      val (errors, data) = Partner.build(answers).pad

      data must not be defined
      errors.value.toChain.toList.distinct must contain theSameElementsAs Seq(
        PartnerNamePage,
        PartnerNinoKnownPage,
        PartnerDateOfBirthPage,
        AllPartnerNationalities,
        PartnerIsHmfOrCivilServantPage,
        PartnerClaimingChildBenefitPage,
        PartnerWorkedAbroadPage,
        PartnerReceivedBenefitsAbroadPage,
        PartnerEmploymentStatusPage
      )
    }

    "must return errors when the applicant knows the partner's NINO but it is missing" in {

      val answers =
        UserAnswers("id")
          .set(PartnerNamePage, adultName).success.value
          .set(PartnerNinoKnownPage, true).success.value
          .set(PartnerDateOfBirthPage, LocalDate.now).success.value
          .set(PartnerNationalityPage(index), nationality).success.value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerWorkedAbroadPage, false).success.value
          .set(PartnerReceivedBenefitsAbroadPage, false).success.value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming).success.value

      val (errors, data) = Partner.build(answers).pad

      data must not be defined
      errors.value.toChain.toList must contain only PartnerNinoPage
    }

    "must return errors when the partner worked and received benefits abroad but the countries are missing" in {

      val answers =
        UserAnswers("id")
          .set(PartnerNamePage, adultName).success.value
          .set(PartnerNinoKnownPage, false).success.value
          .set(PartnerDateOfBirthPage, LocalDate.now).success.value
          .set(PartnerNationalityPage(index), nationality).success.value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
          .set(PartnerIsHmfOrCivilServantPage, false).success.value
          .set(PartnerWorkedAbroadPage, true).success.value
          .set(PartnerReceivedBenefitsAbroadPage, true).success.value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming).success.value

      val (errors, data) = Partner.build(answers).pad

      data must not be defined
      errors.value.toChain.toList must contain theSameElementsAs Seq(
        AllCountriesPartnerWorked,
        AllCountriesPartnerReceivedBenefits
      )
    }
  }
}
