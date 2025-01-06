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

package journey

import generators.ModelGenerators
import models.RelationshipStatus.{Cohabiting, Divorced, Married, Separated, Single, Widowed}
import models.{AdultName, ChildName, Country, EmploymentStatus, Index, Nationality, PartnerClaimingChildBenefit}
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
  private def country = Gen.oneOf(Country.internationalCountries).sample.value

  "users who say they are Married" - {

    "must be asked for details about their partner" in {

      startingFrom(RelationshipStatusPage)
        .run(
          submitAnswer(RelationshipStatusPage, Married),
          submitAnswer(PartnerNamePage, partnerName),
          submitAnswer(PartnerNinoKnownPage, false),
          submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
          submitAnswer(PartnerNationalityPage(Index(0)), nationality),
          submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
          submitAnswer(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(PartnerIsHmfOrCivilServantPage, false),
          submitAnswer(PartnerWorkedAbroadPage, false),
          submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
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
          submitAnswer(PartnerNationalityPage(Index(0)), nationality),
          submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
          submitAnswer(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(PartnerIsHmfOrCivilServantPage, false),
          submitAnswer(PartnerWorkedAbroadPage, false),
          submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
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
        submitAnswer(PartnerNationalityPage(Index(0)), nationality),
        submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
        submitAnswer(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses),
        submitAnswer(PartnerIsHmfOrCivilServantPage, false),
        submitAnswer(PartnerWorkedAbroadPage, false),
        submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
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

  "users whose partner has multiple nationalities" - {

    "must be asked for as many as necessary" in {

      startingFrom(PartnerNationalityPage(Index(0)))
        .run(
          submitAnswer(PartnerNationalityPage(Index(0)), Nationality.allNationalities.head),
          submitAnswer(AddPartnerNationalityPage(Some(Index(0))), true),
          submitAnswer(PartnerNationalityPage(Index(1)), Nationality.allNationalities.head),
          submitAnswer(AddPartnerNationalityPage(Some(Index(1))), false),
          pageMustBe(PartnerEmploymentStatusPage)
        )
    }

    "must be able to remove them" in {

      startingFrom(PartnerNationalityPage(Index(0)))
        .run(
          submitAnswer(PartnerNationalityPage(Index(0)), Nationality.allNationalities.head),
          submitAnswer(AddPartnerNationalityPage(Some(Index(0))), true),
          submitAnswer(PartnerNationalityPage(Index(1)), Nationality.allNationalities.head),
          goTo(RemovePartnerNationalityPage(Index(1))),
          removeAddToListItem(PartnerNationalityPage(Index(1))),
          pageMustBe(AddPartnerNationalityPage()),
          goTo(RemovePartnerNationalityPage(Index(0))),
          removeAddToListItem(PartnerNationalityPage(Index(0))),
          pageMustBe(PartnerNationalityPage(Index(0)))
        )
    }
  }

  "users who have worked abroad" - {

    "must be asked for as many countries as necessary" in {

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          submitAnswer(PartnerWorkedAbroadPage, true),
          submitAnswer(CountryPartnerWorkedPage(Index(0)), country),
          submitAnswer(AddCountryPartnerWorkedPage(Some(Index(0))), true),
          submitAnswer(CountryPartnerWorkedPage(Index(1)), country),
          submitAnswer(AddCountryPartnerWorkedPage(Some(Index(1))), false),
          pageMustBe(PartnerReceivedBenefitsAbroadPage)
        )
    }

    "must be able to remove countries, leaving at least one" in {

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          submitAnswer(PartnerWorkedAbroadPage, true),
          submitAnswer(CountryPartnerWorkedPage(Index(0)), country),
          submitAnswer(AddCountryPartnerWorkedPage(Some(Index(0))), true),
          submitAnswer(CountryPartnerWorkedPage(Index(1)), country),
          goTo(RemoveCountryPartnerWorkedPage(Index(1))),
          removeAddToListItem(CountryPartnerWorkedPage(Index(1))),
          submitAnswer(AddCountryPartnerWorkedPage(), false),
          pageMustBe(PartnerReceivedBenefitsAbroadPage)
        )
    }

    "must be able to remove them all and be asked again if they have worked abroad" in {

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          submitAnswer(PartnerWorkedAbroadPage, true),
          submitAnswer(CountryPartnerWorkedPage(Index(0)), country),
          submitAnswer(AddCountryPartnerWorkedPage(Some(Index(0))), true),
          submitAnswer(CountryPartnerWorkedPage(Index(1)), country),
          goTo(RemoveCountryPartnerWorkedPage(Index(1))),
          removeAddToListItem(CountryPartnerWorkedPage(Index(1))),
          goTo(RemoveCountryPartnerWorkedPage(Index(0))),
          removeAddToListItem(CountryPartnerWorkedPage(Index(0))),
          pageMustBe(PartnerWorkedAbroadPage)
        )
    }
  }

  "users who have received benefits abroad" - {

    "must be asked for as many countries as necessary" in {

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          submitAnswer(PartnerReceivedBenefitsAbroadPage, true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(0)), country),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(0))), true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(1)), country),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(1))), false),
          pageMustBe(PartnerClaimingChildBenefitPage)
        )
    }

    "must be able to remove countries, leaving at least one" in {

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          submitAnswer(PartnerReceivedBenefitsAbroadPage, true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(0)), country),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(0))), true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(1)), country),
          goTo(RemoveCountryPartnerReceivedBenefitsPage(Index(1))),
          removeAddToListItem(CountryPartnerReceivedBenefitsPage(Index(1))),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(), false),
          pageMustBe(PartnerClaimingChildBenefitPage)
        )
    }

    "must be able to remove them all and be asked again if they have received benefits abroad" in {

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          submitAnswer(PartnerReceivedBenefitsAbroadPage, true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(0)), country),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(0))), true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(1)), country),
          goTo(RemoveCountryPartnerReceivedBenefitsPage(Index(1))),
          removeAddToListItem(CountryPartnerReceivedBenefitsPage(Index(1))),
          goTo(RemoveCountryPartnerReceivedBenefitsPage(Index(0))),
          removeAddToListItem(CountryPartnerReceivedBenefitsPage(Index(0))),
          pageMustBe(PartnerReceivedBenefitsAbroadPage)
        )
    }
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
