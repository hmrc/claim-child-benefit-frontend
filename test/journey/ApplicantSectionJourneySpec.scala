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
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.TaskListPage
import pages.applicant._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ApplicantSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val ukAddress = UkAddress("line 1", None, "town", None, "postcode")
  private val adultName = AdultName(None, "first", None, "last")
  private val childName = ChildName("first", None, "list")
  private val phoneNumber = "07777 777777"
  private def nationality = Gen.oneOf(Nationality.allNationalities).sample.value
  private val nino = arbitrary[Nino].sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value
  private val internationalAddress = InternationalAddress("line1", None, "town", None, None, country)
  private val previousName = ApplicantPreviousName("name")
  private val ukNpsAddress = NPSAddress("line 1", None, None, None, None, None, Country.allCountries.find(_.code == "GB"))
  private val nonUkNpsAddress = NPSAddress("line 1", None, None, None, None, None, None)

  "users who don't know their NINO, with no previous names or addresses, who have always lived in the UK and are not claiming right now must proceed to the task list" in {

    startingFrom(ApplicantNinoKnownPage)
      .run(
        submitAnswer(ApplicantNinoKnownPage, false),
        submitAnswer(ApplicantNamePage, adultName),
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        submitAnswer(ApplicantNationalityPage(Index(0)), nationality),
        submitAnswer(AddApplicantNationalityPage(Some(Index(0))), false),
        submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
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
  
  "users with more than one nationality" - {

    "must be asked for as many as necessary" in {

      startingFrom(ApplicantNationalityPage(Index(0)))
        .run(
          submitAnswer(ApplicantNationalityPage(Index(0)), Nationality.allNationalities.head),
          submitAnswer(AddApplicantNationalityPage(Some(Index(0))), true),
          submitAnswer(ApplicantNationalityPage(Index(1)), Nationality.allNationalities.head),
          submitAnswer(AddApplicantNationalityPage(Some(Index(1))), false),
          pageMustBe(ApplicantResidencePage)
        )
    }

    "must be able to remove them" in {

      startingFrom(ApplicantNationalityPage(Index(0)))
        .run(
          submitAnswer(ApplicantNationalityPage(Index(0)), Nationality.allNationalities.head),
          submitAnswer(AddApplicantNationalityPage(Some(Index(0))), true),
          submitAnswer(ApplicantNationalityPage(Index(1)), Nationality.allNationalities.head),
          submitAnswer(AddApplicantNationalityPage(Some(Index(1))), false),
          goTo(RemoveApplicantNationalityPage(Index(1))),
          removeAddToListItem(ApplicantNationalityPage(Index(1))),
          pageMustBe(AddApplicantNationalityPage()),
          goTo(RemoveApplicantNationalityPage(Index(0))),
          removeAddToListItem(ApplicantNationalityPage(Index(0))),
          pageMustBe(ApplicantNationalityPage(Index(0)))
        )
    }
  }

  "unauthenticated users who have lived at their current address a year" - {

    "who have always lived in the UK" - {

      "must proceed" in {

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
            pageMustBe(ApplicantIsHmfOrCivilServantPage)
          )
      }
    }

    "who have lived in the UK and abroad" - {

      "must proceed" in {

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
            submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
            pageMustBe(ApplicantWorkedAbroadPage)
          )
      }
    }

    "who have always lived abroad" - {

      "must proceed" in {

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
            submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
            pageMustBe(ApplicantWorkedAbroadPage)
          )
      }
    }
  }

  "unauthenticated users who have not lived at their current address a year" - {

    "who have always lived in the UK" - {

      "must be asked for their previous UK address" in {

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
            submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
            pageMustBe(ApplicantIsHmfOrCivilServantPage)
          )
      }
    }

    "who have lived in the UK and abroad" - {

      "must be asked for their previous address" - {

        "and proceed when it was in the UK" in {

          startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
            .run(
              setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
              submitAnswer(ApplicantPreviousAddressInUkPage, true),
              submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
              submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
              pageMustBe(ApplicantWorkedAbroadPage)
            )
        }

        "and proceed when it was not in the UK" in {

          startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
            .run(
              setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
              submitAnswer(ApplicantPreviousAddressInUkPage, false),
              submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
              submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
              pageMustBe(ApplicantWorkedAbroadPage)
            )
        }
      }
    }

    "who have always lived abroad" - {

      "must be asked for their previous address" in {

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
            submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
            submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
            pageMustBe(ApplicantWorkedAbroadPage)
          )
      }
    }
  }

  "authenticated users who have lived in the UK and abroad" - {

    "who usually live in the UK" - {

      "who have a designatory address in the UK and have not updated it" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(ukNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have a designatory address in the UK and have updated it with a UK address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(ukNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, true),
              setUserAnswerTo(DesignatoryUkAddressPage, ukAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have a designatory address in the UK and have updated it with an international address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(ukNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must not be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, false),
              setUserAnswerTo(DesignatoryInternationalAddressPage, internationalAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have an international designatory address and have not updated it" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(nonUkNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must not be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have an international designatory address and have updated it with a UK address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(nonUkNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, true),
              setUserAnswerTo(DesignatoryUkAddressPage, ukAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have an international designatory address and have updated it with an international address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(nonUkNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must not be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, false),
              setUserAnswerTo(DesignatoryInternationalAddressPage, internationalAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }
    }

    "who do not usually live in the UK" - {

      "who have a designatory address in the UK and have not updated it" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(ukNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have a designatory address in the UK and have updated it with a UK address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(ukNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, true),
              setUserAnswerTo(DesignatoryUkAddressPage, ukAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have a designatory address in the UK and have updated it with an international address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(ukNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must not be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, false),
              setUserAnswerTo(DesignatoryInternationalAddressPage, internationalAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have an international designatory address and have not updated it" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(nonUkNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must not be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have an international designatory address and have updated it with a UK address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(nonUkNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, true),
              setUserAnswerTo(DesignatoryUkAddressPage, ukAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }

      "who have an international designatory address and have updated it with an international address" - {

        val designatoryDetails = DesignatoryDetails(None, None, Some(nonUkNpsAddress), None)
        val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

        "must not be asked when the arrived in the UK" in {

          startingFrom(ApplicantResidencePage, answers = answers)
            .run(
              setUserAnswerTo(DesignatoryAddressInUkPage, false),
              setUserAnswerTo(DesignatoryInternationalAddressPage, internationalAddress),
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              pageMustBe(ApplicantEmploymentStatusPage)
            )
        }
      }
    }
  }

  "authenticated users who have always lived abroad" - {

    "must be asked where they usually live then for their employment status" in {

      val designatoryDetails = DesignatoryDetails(None, None, Some(nonUkNpsAddress), None)
      val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

      startingFrom(ApplicantResidencePage, answers = answers)
        .run(
          submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
          submitAnswer(ApplicantUsualCountryOfResidencePage, country),
          pageMustBe(ApplicantEmploymentStatusPage)
        )
    }
  }
  
  "authenticated users who have always lived in the UK" - {
    
    "must proceed from Applicant Residence to Applicant is HM Forces" in {

      val designatoryDetails = DesignatoryDetails(None, None, Some(ukNpsAddress), None)
      val answers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(designatoryDetails))

      startingFrom(ApplicantResidencePage, answers = answers)
        .run(
          submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
          pageMustBe(ApplicantIsHmfOrCivilServantPage)
        )
    }
  }

  "unauthenticated users who have lived in the UK and abroad" - {

    "who usually live in the UK" - {

      "and currently live in the UK" - {

        "must be asked when they arrived in the UK" in {

          startingFrom(ApplicantResidencePage)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              submitAnswer(ApplicantCurrentAddressInUkPage, true),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantCurrentUkAddressPage)
            )
        }
      }

      "and do not currently live in the UK" - {

        "must not be asked when they arrived in the UK" in {

          startingFrom(ApplicantResidencePage)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, true),
              submitAnswer(ApplicantCurrentAddressInUkPage, false),
              pageMustBe(ApplicantCurrentInternationalAddressPage)
            )
        }
      }
    }

    "who do not usually live in the UK" - {

      "and currently live in the UK" - {

        "must be asked which country they usually live in and when they arrived in the UK" in {

          startingFrom(ApplicantResidencePage)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              submitAnswer(ApplicantCurrentAddressInUkPage, true),
              submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
              pageMustBe(ApplicantCurrentUkAddressPage)
            )
        }
      }

      "and do not currently live in the UK" - {

        "must be asked which country they usually live in" in {

          startingFrom(ApplicantResidencePage)
            .run(
              submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
              submitAnswer(ApplicantUsuallyLivesInUkPage, false),
              submitAnswer(ApplicantUsualCountryOfResidencePage, country),
              submitAnswer(ApplicantCurrentAddressInUkPage, false),
              pageMustBe(ApplicantCurrentInternationalAddressPage)
            )
        }
      }
    }
  }

  "unauthenticated users who have always lived abroad" - {

    "must be asked which country they usually live in" in {

      startingFrom(ApplicantResidencePage)
        .run(
          submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
          submitAnswer(ApplicantUsualCountryOfResidencePage, country),
          pageMustBe(ApplicantCurrentInternationalAddressPage)
        )
    }
  }

  "users who have worked abroad" - {

    "must be able to add and remove multiple countries" in {

      startingFrom(ApplicantWorkedAbroadPage)
        .run(
          submitAnswer(ApplicantWorkedAbroadPage, true),
          submitAnswer(CountryApplicantWorkedPage(Index(0)), country),
          submitAnswer(AddCountryApplicantWorkedPage(Some(Index(0))), true),
          submitAnswer(CountryApplicantWorkedPage(Index(1)), country),
          goTo(RemoveCountryApplicantWorkedPage(Index(1))),
          removeAddToListItem(CountryApplicantWorkedPage(Index(1))),
          pageMustBe(AddCountryApplicantWorkedPage()),
          submitAnswer(AddCountryApplicantWorkedPage(), false),
          pageMustBe(ApplicantReceivedBenefitsAbroadPage)
        )
    }
  }
  
  "users who have not worked abroad must proceed" in {
    
    startingFrom(ApplicantWorkedAbroadPage)
      .run(
        submitAnswer(ApplicantWorkedAbroadPage, false),
        pageMustBe(ApplicantReceivedBenefitsAbroadPage)
      )
  }

  "users who have received benefits abroad" - {

    "must be able to add and remove multiple countries" in {

      startingFrom(ApplicantReceivedBenefitsAbroadPage)
        .run(
          submitAnswer(ApplicantReceivedBenefitsAbroadPage, true),
          submitAnswer(CountryApplicantReceivedBenefitsPage(Index(0)), country),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(0))), true),
          submitAnswer(CountryApplicantReceivedBenefitsPage(Index(1)), country),
          goTo(RemoveCountryApplicantReceivedBenefitsPage(Index(1))),
          removeAddToListItem(CountryApplicantReceivedBenefitsPage(Index(1))),
          pageMustBe(AddCountryApplicantReceivedBenefitsPage()),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(), false),
          pageMustBe(ApplicantIsHmfOrCivilServantPage)
        )
    }
  }

  "users who have not received benefits abroad must proceed" in {

    startingFrom(ApplicantReceivedBenefitsAbroadPage)
      .run(
        submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
        pageMustBe(ApplicantIsHmfOrCivilServantPage)
      )
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
