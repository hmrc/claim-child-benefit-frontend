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

package journey

import generators.ModelGenerators
import models.RelationshipStatus.{Cohabiting, Divorced, Married, Separated, Single, Widowed}
import models.{AdultName, ChildName, Nationality, PartnerClaimingChildBenefit}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.TaskListPage
import pages.partner._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class PartnerSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val partnerName = AdultName(None, "first", None, "last")
  private val childName = ChildName("first", None, "last")
  private def nino = arbitrary[Nino].sample.value
  private def nationality = Gen.oneOf(Nationality.allNationalities).sample.value

  "users who say they are Married" - {

    "must be asked for details about their partner" in {

      startingFrom(RelationshipStatusPage)
        .run(
          submitAnswer(RelationshipStatusPage, Married),
          submitAnswer(PartnerNamePage, partnerName),
          submitAnswer(PartnerNinoKnownPage, false),
          submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
          submitAnswer(PartnerNationalityPage, nationality),
          submitAnswer(PartnerIsHmfOrCivilServantPage, false),
          submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }

  "users who say they are Cohabiting" - {

    "must be asked when they started living together, then for details about their partner" in {

      startingFrom(RelationshipStatusPage)
        .run(
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(CohabitationDatePage, LocalDate.now),
          submitAnswer(PartnerNamePage, partnerName),
          submitAnswer(PartnerNinoKnownPage, false),
          submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
          submitAnswer(PartnerNationalityPage, nationality),
          submitAnswer(PartnerIsHmfOrCivilServantPage, false),
          submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }

  "users who say they are Separated" - {

    "must be asked for the date they separated, then go to Check Partner Details" in {

      startingFrom(RelationshipStatusPage)
        .run(
          submitAnswer(RelationshipStatusPage, Separated),
          submitAnswer(SeparationDatePage, LocalDate.now),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }

  "users who say they are single, divorced or widowed" - {

    "must go to Check Partner Details" in {

      val relationship = Gen.oneOf(Single, Divorced, Widowed).sample.value

      startingFrom(RelationshipStatusPage)
        .run(
          submitAnswer(RelationshipStatusPage, relationship),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }

  "users who don't know their partner's NINO, and the partner is not entitled to CB, must proceed to the task list" in {

    startingFrom(PartnerNamePage)
      .run(
        submitAnswer(PartnerNamePage, partnerName),
        submitAnswer(PartnerNinoKnownPage, false),
        submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
        submitAnswer(PartnerNationalityPage, nationality),
        submitAnswer(PartnerIsHmfOrCivilServantPage, false),
        submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming),
        pageMustBe(CheckPartnerDetailsPage),
        next,
        pageMustBe(TaskListPage)
      )
  }

  "users who know their partner's NINO must be asked for it" in {

    startingFrom(PartnerNinoKnownPage)
      .run(
        submitAnswer(PartnerNinoKnownPage, true),
        submitAnswer(PartnerNinoPage, nino),
        pageMustBe(PartnerDateOfBirthPage)
      )
  }

  "users whose partner is entitled to Child Benefit or waiting to hear must be asked for their partner's eldest child's details then go to the task list" in {

    import PartnerClaimingChildBenefit._

    val partnerClaiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value

    startingFrom(PartnerClaimingChildBenefitPage)
      .run(
        submitAnswer(PartnerClaimingChildBenefitPage, partnerClaiming),
        submitAnswer(PartnerEldestChildNamePage, childName),
        submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
        pageMustBe(CheckPartnerDetailsPage),
        next,
        pageMustBe(TaskListPage)
      )
  }
}
