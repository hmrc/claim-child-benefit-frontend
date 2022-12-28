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

package journey.tasklist

import generators.ModelGenerators
import journey.JourneyHelpers
import models.CurrentlyReceivingChildBenefit.NotClaiming
import models.RelationshipStatus.{Cohabiting, Married}
import models.{AdultName, Country, Index, InternationalAddress, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.{AlwaysLivedInUkPage, ApplicantNamePage, RelationshipStatusPage, TaskListPage}
import pages.applicant._
import pages.partner.PartnerIsHmfOrCivilServantPage
import pages.payments.CurrentlyReceivingChildBenefitPage
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ApplicantSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val ukAddress = UkAddress("line 1", None, "town", None, "postcode")
  private val adultName = AdultName(None, "first", None, "last")
  private val phoneNumber = "07777 777777"
  private val nationality = "nationality"
  private val nino = arbitrary[Nino].sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value
  private val internationalAddress = InternationalAddress("line1", None, "town", None, None, country)
  private val partneredRelationship = Gen.oneOf(Married, Cohabiting).sample.value

  "users who don't know their NINO, with no previous names or addresses, who have always lived in the UK and are not claiming right now must proceed to the task list" in {

    startingFrom(ApplicantNinoKnownPage)
      .run(
        setUserAnswerTo(AlwaysLivedInUkPage, true),
        submitAnswer(ApplicantNinoKnownPage, false),
        submitAnswer(ApplicantNamePage, adultName),
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        submitAnswer(ApplicantNationalityPage, nationality),
        submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
        pageMustBe(CheckApplicantDetailsPage),
        next,
        pageMustBe(TaskListPage)
      )
  }

  "users who know their NINO" - {

    "must be asked for it" in {

      startingFrom(ApplicantNinoKnownPage)
        .run(
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          pageMustBe(ApplicantNamePage)
        )
    }

    "must not be asked if they have lived at their current address for a year" - {

      "when their address is in the UK" in {

        startingFrom(ApplicantNinoKnownPage)
          .run(
            setUserAnswerTo(AlwaysLivedInUkPage, true),
            submitAnswer(ApplicantNinoKnownPage, true),
            submitAnswer(ApplicantNinoPage, nino),
            submitAnswer(ApplicantNamePage, adultName),
            submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
            submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
            submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
            submitAnswer(ApplicantNationalityPage, nationality),
            submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
            pageMustBe(CurrentlyReceivingChildBenefitPage)
          )
      }

      "when their address is not in the UK" in {

        startingFrom(ApplicantNinoKnownPage)
          .run(
            setUserAnswerTo(AlwaysLivedInUkPage, false),
            setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
            submitAnswer(ApplicantNinoKnownPage, true),
            submitAnswer(ApplicantNinoPage, nino),
            submitAnswer(ApplicantNamePage, adultName),
            submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
            submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
            submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
            submitAnswer(ApplicantNationalityPage, nationality),
            submitAnswer(ApplicantCurrentAddressInUkPage, false),
            submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
            pageMustBe(CurrentlyReceivingChildBenefitPage)
          )
      }
    }
  }

  "users with previous family names" - {

    "must be asked for as many as necessary" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), "name"),
          submitAnswer(AddApplicantPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), "name"),
          submitAnswer(AddApplicantPreviousFamilyNamePage, false),
          pageMustBe(ApplicantDateOfBirthPage)
        )
    }

    "must be able to remove them" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), "name"),
          submitAnswer(AddApplicantPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), "name"),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(1))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(1))),
          pageMustBe(AddApplicantPreviousFamilyNamePage),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(0))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(0))),
          pageMustBe(ApplicantHasPreviousFamilyNamePage)
        )
    }
  }

  "users who are HM Forces or a civil servant abroad must be asked if their address is in the UK" - {

    "and proceed if they say yes" in {

      startingFrom(ApplicantNationalityPage)
        .run(
          setUserAnswerTo(AlwaysLivedInUkPage, false),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantNationalityPage, nationality),
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          pageMustBe(ApplicantLivedAtCurrentAddressOneYearPage)
        )
    }

    "and proceed if they say no" in {

      startingFrom(ApplicantNationalityPage)
        .run(
          setUserAnswerTo(AlwaysLivedInUkPage, false),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantNationalityPage, nationality),
          submitAnswer(ApplicantCurrentAddressInUkPage, false),
          submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
          pageMustBe(ApplicantLivedAtCurrentAddressOneYearPage)
        )
    }
  }

  "users whose partner is HM Forces or a civil servant abroad must be asked if their address is in the UK" - {

    "and proceed if they say yes" in {

      startingFrom(ApplicantNationalityPage)
        .run(
          setUserAnswerTo(RelationshipStatusPage, partneredRelationship),
          setUserAnswerTo(AlwaysLivedInUkPage, false),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, false),
          setUserAnswerTo(PartnerIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantNationalityPage, nationality),
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          pageMustBe(ApplicantLivedAtCurrentAddressOneYearPage)
        )
    }

    "and proceed if they say no" in {

      startingFrom(ApplicantNationalityPage)
        .run(
          setUserAnswerTo(RelationshipStatusPage, partneredRelationship),
          setUserAnswerTo(AlwaysLivedInUkPage, false),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, false),
          setUserAnswerTo(PartnerIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantNationalityPage, nationality),
          submitAnswer(ApplicantCurrentAddressInUkPage, false),
          submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
          pageMustBe(ApplicantLivedAtCurrentAddressOneYearPage)
        )
    }
  }

  "users who did not give a NINO" - {

    "who have not lived at their current address a year" - {

      "who have always lived in the UK" - {

        "must be asked for their previous UK address" in {

          startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
            .run(
              setUserAnswerTo(AlwaysLivedInUkPage, true),
              submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
              submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
              pageMustBe(CurrentlyReceivingChildBenefitPage)
            )
        }
      }

      "who have not always lived in the UK" - {

        "must be asked for their previous address" - {

          "and proceed when it was in the UK" in {

            startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
              .run(
                setUserAnswerTo(AlwaysLivedInUkPage, false),
                submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
                submitAnswer(ApplicantPreviousAddressInUkPage, true),
                submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
                pageMustBe(CurrentlyReceivingChildBenefitPage)
              )
          }

          "and proceed when it was not in the UK" in {

            startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
              .run(
                setUserAnswerTo(AlwaysLivedInUkPage, false),
                submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
                submitAnswer(ApplicantPreviousAddressInUkPage, false),
                submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
                pageMustBe(CurrentlyReceivingChildBenefitPage)
              )
          }
        }
      }
    }
  }
}
