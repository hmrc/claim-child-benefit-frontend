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
import models.CurrentlyReceivingChildBenefit.{GettingPayments, NotClaiming, NotGettingPayments}
import models.{AdultName, ApplicantPreviousName, ChildName, Country, Index, InternationalAddress, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.applicant._
import pages.TaskListPage
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ApplicantSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val ukAddress = UkAddress("line 1", None, "town", None, "postcode")
  private val adultName = AdultName(None, "first", None, "last")
  private val childName = ChildName("first", None, "list")
  private val phoneNumber = "07777 777777"
  private val nationality = "nationality"
  private val nino = arbitrary[Nino].sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value
  private val internationalAddress = InternationalAddress("line1", None, "town", None, None, country)
  private val previousName = ApplicantPreviousName("name")

  "users who don't know their NINO, with no previous names or addresses, who have always lived in the UK and are not claiming right now must proceed to the task list" in {

    startingFrom(ApplicantNinoKnownPage)
      .run(
        submitAnswer(ApplicantNinoKnownPage, false),
        submitAnswer(ApplicantNamePage, adultName),
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        submitAnswer(ApplicantNationalityPage, nationality),
        submitAnswer(AlwaysLivedInUkPage, true),
        submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
        submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
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
            submitAnswer(ApplicantNinoKnownPage, true),
            submitAnswer(ApplicantNinoPage, nino),
            submitAnswer(ApplicantNamePage, adultName),
            submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
            submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
            submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
            submitAnswer(ApplicantNationalityPage, nationality),
            submitAnswer(AlwaysLivedInUkPage, true),
            submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
            pageMustBe(ApplicantIsHmfOrCivilServantPage)
          )
      }

      "when their address is not in the UK" in {

        startingFrom(ApplicantNinoKnownPage)
          .run(
            submitAnswer(ApplicantNinoKnownPage, true),
            submitAnswer(ApplicantNinoPage, nino),
            submitAnswer(ApplicantNamePage, adultName),
            submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
            submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
            submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
            submitAnswer(ApplicantNationalityPage, nationality),
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantUsuallyLivesInUkPage, true),
            submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
            submitAnswer(ApplicantCurrentAddressInUkPage, false),
            submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
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
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage(Some(Index(0))), true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage(Some(Index(1))), false),
          pageMustBe(ApplicantDateOfBirthPage)
        )
    }

    "must be able to remove them" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage(Some(Index(0))), true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), previousName),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(1))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(1))),
          pageMustBe(AddApplicantPreviousFamilyNamePage()),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(0))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(0))),
          pageMustBe(ApplicantHasPreviousFamilyNamePage)
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
              pageMustBe(ApplicantIsHmfOrCivilServantPage)
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
                pageMustBe(ApplicantIsHmfOrCivilServantPage)
              )
          }

          "and proceed when it was not in the UK" in {

            startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
              .run(
                setUserAnswerTo(AlwaysLivedInUkPage, false),
                submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
                submitAnswer(ApplicantPreviousAddressInUkPage, false),
                submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
                pageMustBe(ApplicantIsHmfOrCivilServantPage)
              )
          }
        }
      }
    }
  }

  "users who have not always lived in the UK" - {

    "who usually live in the UK" - {

      "must be asked when they arrived in the UK" in {

        startingFrom(AlwaysLivedInUkPage)
          .run(
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantUsuallyLivesInUkPage, true),
            submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
            pageMustBe(ApplicantCurrentAddressInUkPage)
          )
      }
    }

    "who do not usually live in the UK" - {

      "must be asked which country they usually live in and when they arrived in the UK" in {

        startingFrom(AlwaysLivedInUkPage)
          .run(
            submitAnswer(AlwaysLivedInUkPage, false),
            submitAnswer(ApplicantUsuallyLivesInUkPage, false),
            submitAnswer(ApplicantUsualCountryOfResidencePage, country),
            submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
            pageMustBe(ApplicantCurrentAddressInUkPage)
          )
      }
    }
  }

  "users claiming Child Benefit must be asked for their eldest child's details" in {

    val currentlyReceiving = Gen.oneOf(GettingPayments, NotGettingPayments).sample.value

    startingFrom(CurrentlyReceivingChildBenefitPage)
      .run(
        submitAnswer(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
        submitAnswer(EldestChildNamePage, childName),
        submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
        pageMustBe(CheckApplicantDetailsPage)
      )
  }

  "users not claiming Child Benefit must not be asked for details of their eldest child" in {

    startingFrom(CurrentlyReceivingChildBenefitPage)
      .run(
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
        pageMustBe(CheckApplicantDetailsPage)
      )
  }
}
