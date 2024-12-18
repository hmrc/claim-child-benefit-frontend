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

import cats.data.NonEmptyList
import generators.ModelGenerators
import models.RelationshipStatus._
import models.{AdultName, EmploymentStatus, Index, Nationality, PartnerClaimingChildBenefit, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.partner._

import java.time.LocalDate

class RelationshipSpec extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues {

  private val index = Index(0)
  private val adultName = arbitrary[AdultName].sample.value
  private val nationality = Gen.oneOf(Nationality.allNationalities).sample.value

  ".build" - {

    "must return a model when the relationship is Married and Partner data is present" in {

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, Married)
          .success
          .value
          .set(PartnerNamePage, adultName)
          .success
          .value
          .set(PartnerNinoKnownPage, false)
          .success
          .value
          .set(PartnerDateOfBirthPage, LocalDate.now)
          .success
          .value
          .set(PartnerNationalityPage(index), nationality)
          .success
          .value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses)
          .success
          .value
          .set(PartnerIsHmfOrCivilServantPage, false)
          .success
          .value
          .set(PartnerWorkedAbroadPage, false)
          .success
          .value
          .set(PartnerReceivedBenefitsAbroadPage, false)
          .success
          .value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming)
          .success
          .value

      val (errors, data) = Relationship.build(answers).pad

      data.value `mustEqual` Relationship(
        status = Married,
        since = None,
        partner = Some(
          Partner(
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
        )
      )

      errors `must` `not` `be` defined
    }

    "must return a model when the relationship is Cohabiting and cohabitation date and Partner data are present" in {

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, Cohabiting)
          .success
          .value
          .set(CohabitationDatePage, LocalDate.now)
          .success
          .value
          .set(PartnerNamePage, adultName)
          .success
          .value
          .set(PartnerNinoKnownPage, false)
          .success
          .value
          .set(PartnerDateOfBirthPage, LocalDate.now)
          .success
          .value
          .set(PartnerNationalityPage(index), nationality)
          .success
          .value
          .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses)
          .success
          .value
          .set(PartnerIsHmfOrCivilServantPage, false)
          .success
          .value
          .set(PartnerWorkedAbroadPage, false)
          .success
          .value
          .set(PartnerReceivedBenefitsAbroadPage, false)
          .success
          .value
          .set(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming)
          .success
          .value

      val (errors, data) = Relationship.build(answers).pad

      data.value `mustEqual` Relationship(
        status = Cohabiting,
        since = Some(LocalDate.now),
        partner = Some(
          Partner(
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
        )
      )

      errors `must` `not` `be` defined
    }

    "must return a model when the relationship is Separated and separation date is present" in {

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, Separated)
          .success
          .value
          .set(SeparationDatePage, LocalDate.now)
          .success
          .value

      val (errors, data) = Relationship.build(answers).pad

      data.value `mustEqual` Relationship(Separated, Some(LocalDate.now), None)
      errors `must` `not` `be` defined
    }

    "must return a model when the relationship is Single, Divorced or Widowed" in {

      val status = Gen.oneOf(Single, Divorced, Widowed).sample.value
      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, status)
          .success
          .value

      val (errors, data) = Relationship.build(answers).pad

      data.value `mustEqual` Relationship(status, None, None)
      errors `must` `not` `be` defined
    }

    "must return errors when relationship status is missing" in {

      val answers = UserAnswers("id")

      val (errors, data) = Relationship.build(answers).pad

      data `must` `not` `be` defined
      errors.value.toChain.toList `must` contain `only` RelationshipStatusPage
    }

    "must return errors when the relationship is Separated and separation date is missing" in {

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, Separated)
          .success
          .value

      val (errors, data) = Relationship.build(answers).pad

      data `must` `not` `be` defined
      errors.value.toChain.toList `must` contain `only` SeparationDatePage
    }

    "must return errors when the relationship is Cohabiting and cohabitation date or Partner data is missing" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Cohabiting).success.value

      val (errors, data) = Relationship.build(answers).pad

      data `must` `not` `be` defined
      errors.value.toChain.toList `must` contain(CohabitationDatePage)
    }

    "must return errors when the relationship is Married and Partner data is missing" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Married).success.value

      val (errors, data) = Relationship.build(answers).pad

      data `must` `not` `be` defined
      errors `mustBe` defined
    }
  }
}
