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

package models.domain

import cats.data.NonEmptyList
import generators.ModelGenerators
import models.PartnerClaimingChildBenefit._
import models.journey
import models.{AdultName, EmploymentStatus}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class PartnerSpec extends AnyFreeSpec with Matchers with ModelGenerators with OptionValues {

  ".build" - {

    "must return a Partner when the given model has a NINO and is claiming Child Benefit or waiting to hear" in {

      val surname = "surname"
      val nino = arbitrary[Nino].sample.value
      val claiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value

      val partner = journey.Partner(
        name = AdultName(None, "first", None, surname),
        nationalInsuranceNumber = Some(nino),
        dateOfBirth = LocalDate.now,
        nationalities = NonEmptyList.fromListUnsafe(Gen.nonEmptyListOf(arbitrary[models.Nationality]).sample.value),
        memberOfHMForcesOrCivilServantAbroad = true,
        currentlyClaimingChildBenefit = claiming,
        eldestChild = None,
        countriesWorked = Nil,
        countriesReceivedBenefits = Nil,
        employmentStatus = Set[EmploymentStatus](EmploymentStatus.NoneOfThese)
      )

      val result = Partner.build(partner)
      result.value `mustEqual` Partner(nino.withoutSuffix, surname)
    }

    "must return None when the given model has a NINO and is not claiming Child Benefit or waiting to hear about their eligibility" in {

      val surname = "surname"
      val nino = arbitrary[Nino].sample.value

      val partner = journey.Partner(
        name = AdultName(None, "first", None, surname),
        nationalInsuranceNumber = Some(nino),
        dateOfBirth = LocalDate.now,
        nationalities = NonEmptyList.fromListUnsafe(Gen.nonEmptyListOf(arbitrary[models.Nationality]).sample.value),
        memberOfHMForcesOrCivilServantAbroad = true,
        currentlyClaimingChildBenefit = NotClaiming,
        eldestChild = None,
        countriesWorked = Nil,
        countriesReceivedBenefits = Nil,
        employmentStatus = Set[EmploymentStatus](EmploymentStatus.NoneOfThese)
      )

      val result = Partner.build(partner)
      result `must` `not` `be` defined
    }

    "must return None when the given model does not have a NINO" in {

      val surname = "surname"

      val partner = journey.Partner(
        name = AdultName(None, "first", None, surname),
        nationalInsuranceNumber = None,
        dateOfBirth = LocalDate.now,
        nationalities = NonEmptyList.fromListUnsafe(Gen.nonEmptyListOf(arbitrary[models.Nationality]).sample.value),
        memberOfHMForcesOrCivilServantAbroad = true,
        currentlyClaimingChildBenefit = GettingPayments,
        eldestChild = None,
        countriesWorked = Nil,
        countriesReceivedBenefits = Nil,
        employmentStatus = Set[EmploymentStatus](EmploymentStatus.NoneOfThese)
      )

      val result = Partner.build(partner)
      result `must` `not` `be` defined
    }

    "must normalise accented characters and replace ’ with ' in the partner's surname" in {

      val surname = "À’ēîůŷ"
      val nino = arbitrary[Nino].sample.value
      val claiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value

      val partner = journey.Partner(
        name = AdultName(None, "first", None, surname),
        nationalInsuranceNumber = Some(nino),
        dateOfBirth = LocalDate.now,
        nationalities = NonEmptyList.fromListUnsafe(Gen.nonEmptyListOf(arbitrary[models.Nationality]).sample.value),
        memberOfHMForcesOrCivilServantAbroad = true,
        currentlyClaimingChildBenefit = claiming,
        eldestChild = None,
        countriesWorked = Nil,
        countriesReceivedBenefits = Nil,
        employmentStatus = Set[EmploymentStatus](EmploymentStatus.NoneOfThese)
      )

      val result = Partner.build(partner)
      result.value `mustEqual` Partner(nino.withoutSuffix, "A'eiuy")
    }
  }
}
