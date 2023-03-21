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
import models.CurrentlyReceivingChildBenefit._
import models.RelationshipStatus._
import models.{Index, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.applicant._
import pages.partner.RelationshipStatusPage
import pages.payments._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingApplicantSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val ukAddress = UkAddress("line 1", None, "town", None, "postcode")
  private val adultName = AdultName(None, "first", None, "last")
  private val childName = ChildName("first", None, "last")
  private val nino = arbitrary[Nino].sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value
  private val internationalAddress = InternationalAddress("line1", None, "town", None, None, country)
  private val bankDetails = arbitrary[BankAccountDetails].sample.value
  private val previousName = ApplicantPreviousName("name")
  private val phoneNumber = "07777 777777"
  private def nationality = Gen.oneOf(Nationality.allNationalities).sample.value

  private val setFullPaymentDetailsSingle: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(ApplicantIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(WantToBePaidPage, true),
    setUserAnswerTo(ApplicantBenefitsPage, Benefits.qualifyingBenefits),
    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
    setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
    setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
    setUserAnswerTo(BankAccountDetailsPage, bankDetails)
  )

  private val setFullPaymentDetailsPartner: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(ApplicantOrPartnerIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(WantToBePaidPage, true),
    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Benefits.qualifyingBenefits),
    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
    setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
    setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
    setUserAnswerTo(BankAccountDetailsPage, bankDetails)
  )

  private val paymentDetailsMustHaveBeenRemoved: JourneyStep[Unit] = journeyOf(
    answersMustNotContain(ApplicantIncomePage),
    answersMustNotContain(ApplicantOrPartnerIncomePage),
    answersMustNotContain(WantToBePaidPage),
    answersMustNotContain(ApplicantBenefitsPage),
    answersMustNotContain(ApplicantOrPartnerBenefitsPage),
    answersMustNotContain(PaymentFrequencyPage),
    answersMustNotContain(ApplicantHasSuitableAccountPage),
    answersMustNotContain(BankAccountHolderPage),
    answersMustNotContain(BankAccountDetailsPage)
  )

  "when the user originally said they knew their NINO" - {

    "and has always lived in the UK" - {

      "changing to say they don't know their NINO must remove it then return to Check Applicant" in {

        val initialise = journeyOf(
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          submitAnswer(ApplicantNamePage, adultName),
          setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(ApplicantNinoKnownPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantNinoKnownPage),
            submitAnswer(ApplicantNinoKnownPage, false),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantNinoPage)
          )
      }
    }

    "and has lived in the UK and abroad" - {

      "changing to say they don't know their NINO must remove it then return to Check Applicant" in {

        val initialise = journeyOf(
          setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
          setUserAnswerTo(ApplicantUsuallyLivesInUkPage, true),
          setUserAnswerTo(ApplicantWorkedAbroadPage, false),
          setUserAnswerTo(ApplicantReceivedBenefitsAbroadPage, false),
          setUserAnswerTo(ApplicantArrivedInUkPage, LocalDate.now),
          setUserAnswerTo(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          submitAnswer(ApplicantNamePage, adultName),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(ApplicantNinoKnownPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantNinoKnownPage),
            submitAnswer(ApplicantNinoKnownPage, false),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantNinoPage)
          )
      }
    }

    "and has always lived abroad" - {

      "changing to say they don't know their NINO must remove it then return to Check Applicant" in {

        val initialise = journeyOf(
          setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
          setUserAnswerTo(ApplicantUsualCountryOfResidencePage, country),
          setUserAnswerTo(ApplicantWorkedAbroadPage, false),
          setUserAnswerTo(ApplicantReceivedBenefitsAbroadPage, false),
          setUserAnswerTo(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          submitAnswer(ApplicantNamePage, adultName),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(ApplicantNinoKnownPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantNinoKnownPage),
            submitAnswer(ApplicantNinoKnownPage, false),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantNinoPage)
          )
      }
    }
  }

  "when the user originally said they did not know their NINO" - {

    "changing to say they know it must collect the NINO then return to Check Applicant" in {

      val initialise = journeyOf(
        setUserAnswerTo(AlwaysLivedInUkPage, true),
        submitAnswer(ApplicantNinoKnownPage, false),
        submitAnswer(ApplicantNamePage, adultName),
        setUserAnswerTo(CurrentlyReceivingChildBenefitPage, NotClaiming),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantNinoKnownPage),
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }
  }

  "when the user originally said they had always lived in the UK" - {

      val initialise = journeyOf(
        submitAnswer(ApplicantNinoKnownPage, false),
        submitAnswer(ApplicantNamePage, adultName),
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        submitAnswer(ApplicantNationalityPage(Index(0)), nationality),
        submitAnswer(AddApplicantNationalityPage(Some(Index(0))), false),
        submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
        submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
        submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
        submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
        pageMustBe(CheckApplicantDetailsPage)
      )

    "changing to say they have lived in the UK and abroad must remove addresses, ask residency and address questions, then go to Check Applicant" in {

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantResidencePage),
          submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
          submitAnswer(ApplicantUsuallyLivesInUkPage, true),
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantWorkedAbroadPage, false),
          submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
          pageMustBe(CheckApplicantDetailsPage),
        )
    }

    "changing to say they have always lived abroad must remove addresses, ask residency and address questions, then go to Check Applicant" in {

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantResidencePage),
          submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
          submitAnswer(ApplicantUsualCountryOfResidencePage, country),
          submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantWorkedAbroadPage, false),
          submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(ApplicantPreviousUkAddressPage)
        )
    }
  }

  "when the user originally said they had lived in the UK and abroad" - {

    val initialise = journeyOf(
      submitAnswer(ApplicantNinoKnownPage, false),
      submitAnswer(ApplicantNamePage, adultName),
      submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
      submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
      submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
      submitAnswer(ApplicantNationalityPage(Index(0)), nationality),
      submitAnswer(AddApplicantNationalityPage(Some(Index(0))), false),
      submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
      submitAnswer(ApplicantUsuallyLivesInUkPage, false),
      submitAnswer(ApplicantUsualCountryOfResidencePage, country),
      submitAnswer(ApplicantCurrentAddressInUkPage, true),
      submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
      submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
      submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
      submitAnswer(ApplicantPreviousAddressInUkPage, true),
      submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
      submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
      submitAnswer(ApplicantWorkedAbroadPage, true),
      submitAnswer(CountryApplicantWorkedPage(Index(0)), country),
      submitAnswer(AddCountryApplicantWorkedPage(Some(Index(0))), false),
      submitAnswer(ApplicantReceivedBenefitsAbroadPage, true),
      submitAnswer(CountryApplicantReceivedBenefitsPage(Index(0)), country),
      submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(0))), false),
      submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
      submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
      pageMustBe(CheckApplicantDetailsPage),
      setUserAnswerTo(ApplicantCurrentInternationalAddressPage, internationalAddress),
      setUserAnswerTo(ApplicantPreviousInternationalAddressPage, internationalAddress)
    )

    "changing to say they have always lived in the UK" - {

      "must remove addresses and residency questions, collect address details, then go to Check Applicant" in {

        startingFrom(ApplicantNinoKnownPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantResidencePage),
            submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
            submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantUsuallyLivesInUkPage),
            answersMustNotContain(ApplicantUsualCountryOfResidencePage),
            answersMustNotContain(ApplicantCurrentAddressInUkPage),
            answersMustNotContain(ApplicantArrivedInUkPage),
            answersMustNotContain(ApplicantCurrentInternationalAddressPage),
            answersMustNotContain(ApplicantPreviousAddressInUkPage),
            answersMustNotContain(ApplicantPreviousUkAddressPage),
            answersMustNotContain(ApplicantPreviousInternationalAddressPage),
            answersMustNotContain(ApplicantEmploymentStatusPage),
            answersMustNotContain(ApplicantWorkedAbroadPage),
            answersMustNotContain(CountryApplicantWorkedPage(Index(0))),
            answersMustNotContain(ApplicantReceivedBenefitsAbroadPage),
            answersMustNotContain(CountryApplicantReceivedBenefitsPage(Index(0)))
          )
      }
    }

    "changing to say they have always lived abroad" - {

      "must remove addresses and residency questions, collect them, then to go Check Applicant" in {

        startingFrom(ApplicantNinoKnownPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantResidencePage),
            submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
            submitAnswer(ApplicantUsualCountryOfResidencePage, country),
            submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
            submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
            submitAnswer(ApplicantWorkedAbroadPage, false),
            submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantCurrentAddressInUkPage),
            answersMustNotContain(ApplicantArrivedInUkPage),
            answersMustNotContain(ApplicantPreviousAddressInUkPage),
            answersMustNotContain(ApplicantPreviousUkAddressPage),
            answersMustNotContain(ApplicantPreviousInternationalAddressPage)
          )
      }
    }
  }

  "when the user originally said they had always lived abroad" - {

    val initialise = journeyOf(
      submitAnswer(ApplicantNinoKnownPage, false),
      submitAnswer(ApplicantNamePage, adultName),
      submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
      submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
      submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
      submitAnswer(ApplicantNationalityPage(Index(0)), nationality),
      submitAnswer(AddApplicantNationalityPage(Some(Index(0))), false),
      submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
      submitAnswer(ApplicantUsualCountryOfResidencePage, country),
      submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
      submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
      submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
      submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
      submitAnswer(ApplicantWorkedAbroadPage, true),
      submitAnswer(CountryApplicantWorkedPage(Index(0)), country),
      submitAnswer(AddCountryApplicantWorkedPage(Some(Index(0))), false),
      submitAnswer(ApplicantReceivedBenefitsAbroadPage, true),
      submitAnswer(CountryApplicantReceivedBenefitsPage(Index(0)), country),
      submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(0))), false),
      submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
      submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
      pageMustBe(CheckApplicantDetailsPage)
    )

    "changing to say they have always lived in the UK must remove address and residency questions, collect address details, then go to Check Applicant" in {

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantResidencePage),
          submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(ApplicantUsualCountryOfResidencePage),
          answersMustNotContain(ApplicantCurrentInternationalAddressPage),
          answersMustNotContain(ApplicantPreviousInternationalAddressPage),
          answersMustNotContain(ApplicantWorkedAbroadPage),
          answersMustNotContain(ApplicantEmploymentStatusPage),
          answersMustNotContain(CountryApplicantWorkedPage(Index(0))),
          answersMustNotContain(ApplicantReceivedBenefitsAbroadPage),
          answersMustNotContain(CountryApplicantReceivedBenefitsPage(Index(0)))
        )
    }

    "changing to say they have lived in the UK and abroad must remove address and residency questions, collect them, then go to Check Applicant" in {

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantResidencePage),
          submitAnswer(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
          submitAnswer(ApplicantUsuallyLivesInUkPage, false),
          submitAnswer(ApplicantUsualCountryOfResidencePage, country),
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantWorkedAbroadPage, true),
          submitAnswer(CountryApplicantWorkedPage(Index(0)), country),
          submitAnswer(AddCountryApplicantWorkedPage(Some(Index(0))), false),
          submitAnswer(ApplicantReceivedBenefitsAbroadPage, true),
          submitAnswer(CountryApplicantReceivedBenefitsPage(Index(0)), country),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(0))), false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(ApplicantCurrentInternationalAddressPage),
          answersMustNotContain(ApplicantPreviousInternationalAddressPage),
        )
    }
  }

  "when the user originally said they had previous names" - {

    val initialise = journeyOf(
      submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
      submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), previousName),
      submitAnswer(AddApplicantPreviousFamilyNamePage(Some(Index(0))), true),
      submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), previousName),
      submitAnswer(AddApplicantPreviousFamilyNamePage(Some(Index(1))), false),
      submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
      goTo(CheckApplicantDetailsPage)
    )

    "changing to say they don't have previous names must remove them and return to Check Applicant" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantHasPreviousFamilyNamePage),
          submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(ApplicantPreviousFamilyNamePage(Index(1))),
          answersMustNotContain(ApplicantPreviousFamilyNamePage(Index(0)))
        )
    }

    "they must be able to add another previous name" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage()),
          submitAnswer(AddApplicantPreviousFamilyNamePage(), true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(2)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage(Some(Index(2))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }

    "they must be able to change a name" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage()),
          goToChangeAnswer(ApplicantPreviousFamilyNamePage(Index(1))),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage(), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }

    "they must be able to remove a name, leaving at least one" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage()),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(1))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(1))),
          submitAnswer(AddApplicantPreviousFamilyNamePage(), false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(ApplicantPreviousFamilyNamePage(Index(1))),
          answersMustContain(ApplicantPreviousFamilyNamePage(Index(0)))
        )
    }

    "removing the last name must go to ask if the user has a previous name" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage()),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(1))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(1))),
          pageMustBe(AddApplicantPreviousFamilyNamePage()),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(0))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(0))),
          pageMustBe(ApplicantHasPreviousFamilyNamePage)
        )
    }
  }

  "when the user initially said they had no previous names" - {

    "changing to say they have previous names must collect the names then return to Check Applicant" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantHasPreviousFamilyNamePage),
          submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage(Some(Index(0))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }
  }

  "the user must be able to add a nationality" in {

    val initialise = journeyOf(
      submitAnswer(ApplicantNationalityPage(Index(0)), nationality),
      submitAnswer(AddApplicantNationalityPage(Some(Index(0))), false),
      submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
      goTo(CheckApplicantDetailsPage)
    )

    startingFrom(ApplicantNationalityPage(Index(0)))
      .run(
        initialise,
        goToChangeAnswer(AddApplicantNationalityPage()),
        submitAnswer(AddApplicantNationalityPage(), true),
        submitAnswer(ApplicantNationalityPage(Index(1)), nationality),
        submitAnswer(AddApplicantNationalityPage(Some(Index(1))), false),
        pageMustBe(CheckApplicantDetailsPage)
      )
  }

  "the user must be able to remove a nationality, leaving at least one" in {

    val initialise = journeyOf(
      submitAnswer(ApplicantNationalityPage(Index(0)), nationality),
      submitAnswer(AddApplicantNationalityPage(Some(Index(0))), true),
      submitAnswer(ApplicantNationalityPage(Index(1)), nationality),
      submitAnswer(AddApplicantNationalityPage(Some(Index(1))), false),
      submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
      goTo(CheckApplicantDetailsPage)
    )

    startingFrom(ApplicantNationalityPage(Index(0)))
      .run(
        initialise,
        goToChangeAnswer(AddApplicantNationalityPage()),
        goTo(RemoveApplicantNationalityPage(Index(1))),
        removeAddToListItem(ApplicantNationalityPage(Index(1))),
        submitAnswer(AddApplicantNationalityPage(), false),
        pageMustBe(CheckApplicantDetailsPage)
      )
  }

  "removing the last nationality must ask the user their nationality" in {

    val initialise = journeyOf(
      submitAnswer(ApplicantNationalityPage(Index(0)), nationality),
      submitAnswer(AddApplicantNationalityPage(Some(Index(0))), false),
      submitAnswer(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
      goTo(CheckApplicantDetailsPage)
    )

    startingFrom(ApplicantNationalityPage(Index(0)))
      .run(
        initialise,
        goToChangeAnswer(AddApplicantNationalityPage()),
        goTo(RemoveApplicantNationalityPage(Index(0))),
        removeAddToListItem(ApplicantNationalityPage(Index(0))),
        pageMustBe(ApplicantNationalityPage(Index(0)))
      )
  }

  "when the user had the option to give either a UK or an international current address" - {

    "and originally gave a UK address" - {

      "changing to say their address is international must collect the address, remove the UK address, and return to Check Applicant" in {

        val initialise = journeyOf(
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(ApplicantCurrentAddressInUkPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantCurrentAddressInUkPage),
            submitAnswer(ApplicantCurrentAddressInUkPage, false),
            submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantCurrentUkAddressPage),
            answersMustNotContain(ApplicantArrivedInUkPage)
          )
      }
    }

    "and originally gave an international address" - {

      "changing to say their address is in the UK must collect the date they arrived in the UK and the address, remove the international address, and return to Check Applicant" in {

        val initialise = journeyOf(
          submitAnswer(ApplicantCurrentAddressInUkPage, false),
          submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(ApplicantCurrentAddressInUkPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantCurrentAddressInUkPage),
            submitAnswer(ApplicantCurrentAddressInUkPage, true),
            submitAnswer(ApplicantArrivedInUkPage, LocalDate.now),
            submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantCurrentInternationalAddressPage)
          )
      }
    }
  }

  "when the user originally said they had lived at their current address a year" - {

    "changing to say they haven't lived there a year" - {

      "when they have always lived in the UK" - {

        "must collect their previous UK address and return to Check Applicant" in {

          val initialise = journeyOf(
            setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysUk),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
            submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
              submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
              submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
              pageMustBe(CheckApplicantDetailsPage)
            )
        }
      }

      "when they have lived in the UK and abroad" - {

        val initialise = journeyOf(
          setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantWorkedAbroadPage, false),
          goTo(CheckApplicantDetailsPage)
        )

        "must collect their previous address when it is in the UK, and return to Check Applicant" in {

          startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
              submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
              submitAnswer(ApplicantPreviousAddressInUkPage, true),
              submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
              pageMustBe(CheckApplicantDetailsPage)
            )
        }

        "must collect their previous address when it is international, and return to Check Applicant" in {

          startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
              submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
              submitAnswer(ApplicantPreviousAddressInUkPage, false),
              submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
              pageMustBe(CheckApplicantDetailsPage)
            )
        }
      }

      "when they have always lived abroad" - {

        "must collect their previous UK address and return to Check Applicant" in {

          val initialise = journeyOf(
            setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
            submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
            submitAnswer(ApplicantWorkedAbroadPage, false),
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
            .run(
              initialise,
              goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
              submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
              submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
              pageMustBe(CheckApplicantDetailsPage)
            )
        }
      }
    }
  }

  "when the user originally said they had not lived at their current address a year" - {

    "changing to say they have lived there a year must remove their previous address and return to Check Applicant" in {

      val initialise = journeyOf(
        setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousAddressInUkPage, true),
        submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
        submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
        submitAnswer(ApplicantWorkedAbroadPage, false),
        setUserAnswerTo(ApplicantPreviousInternationalAddressPage, internationalAddress),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(ApplicantPreviousAddressInUkPage),
          answersMustNotContain(ApplicantPreviousUkAddressPage),
          answersMustNotContain(ApplicantPreviousInternationalAddressPage)
        )
    }

    "and originally said their previous address was in the UK" - {

      "changing to say it was international must collect the address, remove the UK address, and return to Check Applicant" in {

        val initialise = journeyOf(
          setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
          submitAnswer(ApplicantPreviousAddressInUkPage, true),
          submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
          submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantWorkedAbroadPage, false),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantPreviousAddressInUkPage),
            submitAnswer(ApplicantPreviousAddressInUkPage, false),
            submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantPreviousUkAddressPage)
          )
      }
    }

    "and originally said their previous address was international" - {

      "changing to say it was in the UK must collect the address, remove the international address, and return to Check Applicant" in {

        val initialise = journeyOf(
          setUserAnswerTo(ApplicantResidencePage, ApplicantResidence.UkAndAbroad),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
          submitAnswer(ApplicantPreviousAddressInUkPage, false),
          submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
          submitAnswer(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses),
          submitAnswer(ApplicantWorkedAbroadPage, false),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantPreviousAddressInUkPage),
            submitAnswer(ApplicantPreviousAddressInUkPage, true),
            submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(ApplicantPreviousInternationalAddressPage)
          )
      }
    }
  }

  "when the user originally said they had worked abroad" - {

    val initialise = journeyOf(
      submitAnswer(ApplicantWorkedAbroadPage, true),
      submitAnswer(CountryApplicantWorkedPage(Index(0)), country),
      submitAnswer(AddCountryApplicantWorkedPage(Some(Index(0))), true),
      submitAnswer(CountryApplicantWorkedPage(Index(1)), country),
      submitAnswer(AddCountryApplicantWorkedPage(Some(Index(1))), false),
      submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
      goTo(CheckApplicantDetailsPage)
    )

    "changing to say they haven't must remove the countries and return to Check Applicant" in {

      startingFrom(ApplicantWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantWorkedAbroadPage),
          submitAnswer(ApplicantWorkedAbroadPage, false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(CountryApplicantWorkedPage(Index(1))),
          answersMustNotContain(CountryApplicantWorkedPage(Index(0))),
          answersMustNotContain(AddCountryApplicantWorkedPage())
        )
    }

    "they must be able to add another country" in {

      startingFrom(ApplicantWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantWorkedPage()),
          submitAnswer(AddCountryApplicantWorkedPage(), true),
          submitAnswer(CountryApplicantWorkedPage(Index(2)), country),
          submitAnswer(AddCountryApplicantWorkedPage(Some(Index(2))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }

    "they must be able to change a country" in {

      startingFrom(ApplicantWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantWorkedPage()),
          goToChangeAnswer(CountryApplicantWorkedPage(Index(1))),
          submitAnswer(CountryApplicantWorkedPage(Index(1)), country),
          submitAnswer(AddCountryApplicantWorkedPage(Some(Index(1))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }

    "they must be able to remove a country, leaving at least one" in {

      startingFrom(ApplicantWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantWorkedPage()),
          goTo(RemoveCountryApplicantWorkedPage(Index(1))),
          removeAddToListItem(CountryApplicantWorkedPage(Index(1))),
          submitAnswer(AddCountryApplicantWorkedPage(), false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(CountryApplicantWorkedPage(Index(1))),
          answersMustContain(CountryApplicantWorkedPage(Index(0)))
        )
    }

    "removing the last country must ask if the user has worked abroad" in {

      startingFrom(ApplicantWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantWorkedPage()),
          goTo(RemoveCountryApplicantWorkedPage(Index(1))),
          removeAddToListItem(CountryApplicantWorkedPage(Index(1))),
          goTo(RemoveCountryApplicantWorkedPage(Index(0))),
          removeAddToListItem(CountryApplicantWorkedPage(Index(0))),
          pageMustBe(ApplicantWorkedAbroadPage)
        )
    }
  }

  "when the user originally said they had not worked abroad" - {

    "changing to say they have worked abroad must collect the countries where they worked then return to Check Applicant" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantWorkedAbroadPage, false),
        submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(ApplicantWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantWorkedAbroadPage),
          submitAnswer(ApplicantWorkedAbroadPage, true),
          submitAnswer(CountryApplicantWorkedPage(Index(0)), country),
          submitAnswer(AddCountryApplicantWorkedPage(Some(Index(0))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }
  }

  "when the user originally said they had received benefits abroad" - {

    val initialise = journeyOf(
      submitAnswer(ApplicantReceivedBenefitsAbroadPage, true),
      submitAnswer(CountryApplicantReceivedBenefitsPage(Index(0)), country),
      submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(0))), true),
      submitAnswer(CountryApplicantReceivedBenefitsPage(Index(1)), country),
      submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(1))), false),
      submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
      goTo(CheckApplicantDetailsPage)
    )

    "changing to say they haven't must remove the countries and return to Check Applicant" in {

      startingFrom(ApplicantReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantReceivedBenefitsAbroadPage),
          submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(CountryApplicantReceivedBenefitsPage(Index(1))),
          answersMustNotContain(CountryApplicantReceivedBenefitsPage(Index(0))),
          answersMustNotContain(AddCountryApplicantReceivedBenefitsPage())
        )
    }

    "they must be able to add another country" in {

      startingFrom(ApplicantReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantReceivedBenefitsPage()),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(), true),
          submitAnswer(CountryApplicantReceivedBenefitsPage(Index(2)), country),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(2))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }

    "they must be able to change a country" in {

      startingFrom(ApplicantReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantReceivedBenefitsPage()),
          goToChangeAnswer(CountryApplicantReceivedBenefitsPage(Index(1))),
          submitAnswer(CountryApplicantReceivedBenefitsPage(Index(1)), country),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(1))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }

    "they must be able to remove a country, leaving at least one" in {

      startingFrom(ApplicantReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantReceivedBenefitsPage()),
          goTo(RemoveCountryApplicantReceivedBenefitsPage(Index(1))),
          removeAddToListItem(CountryApplicantReceivedBenefitsPage(Index(1))),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(), false),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(CountryApplicantReceivedBenefitsPage(Index(1))),
          answersMustContain(CountryApplicantReceivedBenefitsPage(Index(0)))
        )
    }

    "removing the last country must ask if the user has received benefits" in {

      startingFrom(ApplicantReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryApplicantReceivedBenefitsPage()),
          goTo(RemoveCountryApplicantReceivedBenefitsPage(Index(1))),
          removeAddToListItem(CountryApplicantReceivedBenefitsPage(Index(1))),
          goTo(RemoveCountryApplicantReceivedBenefitsPage(Index(0))),
          removeAddToListItem(CountryApplicantReceivedBenefitsPage(Index(0))),
          pageMustBe(ApplicantReceivedBenefitsAbroadPage)
        )
    }
  }

  "when the user originally said they had not received benefits abroad" - {

    "changing to say they have received benefits abroad must collect the countries where they worked then return to Check Applicant" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantReceivedBenefitsAbroadPage, false),
        submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(ApplicantReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantReceivedBenefitsAbroadPage),
          submitAnswer(ApplicantReceivedBenefitsAbroadPage, true),
          submitAnswer(CountryApplicantReceivedBenefitsPage(Index(0)), country),
          submitAnswer(AddCountryApplicantReceivedBenefitsPage(Some(Index(0))), false),
          pageMustBe(CheckApplicantDetailsPage)
        )
    }
  }
  
  "when the user originally said they were getting Child Benefit payments" - {

    "and they had not already given payment details" - {

      "changing to say they are not claiming must remove details of their oldest child and return to Check Applicant" in {

        val initialise = journeyOf(
          submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage)
          )
      }

      "changing to say they are claiming but not getting payments must return to Check Applicant" in {

        val initialise = journeyOf(
          submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustContain(EldestChildNamePage),
            answersMustContain(EldestChildDateOfBirthPage)
          )
      }
    }

    "and they had already given payment details" - {

      "and are married or cohabiting" - {

        "changing to say they are not claiming must remove details of their oldest child and their payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsPartner,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustNotContain(EldestChildNamePage),
              answersMustNotContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }

        "changing to say they are claiming but not getting payments must remove payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsPartner,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustContain(EldestChildNamePage),
              answersMustContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }
      }

      "and are single, separated, divorced or widowed" - {

        "changing to say they are not claiming must remove details of their oldest child and their payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Single, Separated, Widowed, Divorced).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsSingle,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustNotContain(EldestChildNamePage),
              answersMustNotContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }

        "changing to say they are claiming but not getting payments must remove payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Single, Separated, Widowed, Divorced).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsSingle,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustContain(EldestChildNamePage),
              answersMustContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }
      }
    }
  }

  "when the user originally said they were claiming Child Benefit but not getting payments" - {

    "and they had not already given payment details" - {

      "changing to say they are not claiming must remove details of their oldest child and return to Check Applicant" in {

        val initialise = journeyOf(
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustNotContain(EldestChildNamePage),
            answersMustNotContain(EldestChildDateOfBirthPage)
          )
      }

      "changing to say they are getting payments must return to Check Applicant" in {

        val initialise = journeyOf(
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
          submitAnswer(EldestChildNamePage, childName),
          submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
            pageMustBe(CheckApplicantDetailsPage),
            answersMustContain(EldestChildNamePage),
            answersMustContain(EldestChildDateOfBirthPage)
          )
      }
    }

    "and they had already given payment details" - {

      "and are married or cohabiting" - {

        "changing to say they are not claiming must remove details of their oldest child and their payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsPartner,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustNotContain(EldestChildNamePage),
              answersMustNotContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }

        "changing to say they are getting payments must remove payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsPartner,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustContain(EldestChildNamePage),
              answersMustContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }
      }

      "and are single, separated, divorced or widowed" - {

        "changing to say they are not claiming must remove details of their oldest child and their payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Single, Separated, Widowed, Divorced).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsSingle,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustNotContain(EldestChildNamePage),
              answersMustNotContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }

        "changing to say they are getting payments must remove payment details, tell them payment details have been removed, then return to Check Applicant" in {

          val relationship = Gen.oneOf(Single, Separated, Widowed, Divorced).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotGettingPayments),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            setFullPaymentDetailsSingle,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, GettingPayments),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              pageMustBe(CheckApplicantDetailsPage),
              answersMustContain(EldestChildNamePage),
              answersMustContain(EldestChildDateOfBirthPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }
      }
    }
  }

  "when the user originally said they were not claiming Child Benefit" - {

    "and had not already given payment details" - {

      "changing to say they are claiming must collect details of their oldest child and return to Check Applicant" in {

        val currentlyReceiving = Gen.oneOf(GettingPayments, NotGettingPayments).sample.value

        val initialise = journeyOf(
          submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
          goTo(CheckApplicantDetailsPage)
        )

        startingFrom(CurrentlyReceivingChildBenefitPage)
          .run(
            initialise,
            goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
            submitAnswer(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
            submitAnswer(EldestChildNamePage, childName),
            submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
            pageMustBe(CheckApplicantDetailsPage)
          )
      }
    }

    "and had already given payment details" - {

      "and the user is married or cohabiting" - {

        "changing to say they are claiming must collect details of their oldest child, remove payment details, tell the user they've been removed then return to Check Applicant" in {

          val currentlyReceiving = Gen.oneOf(GettingPayments, NotGettingPayments).sample.value
          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            setFullPaymentDetailsPartner,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckApplicantDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }
      }

      "and the user is single, separated, divorced or widowed" - {

        "changing to say they are claiming must collect details of their oldest child, remove payment details, tell the user they've been removed then return to Check Applicant" in {

          val currentlyReceiving = Gen.oneOf(GettingPayments, NotGettingPayments).sample.value
          val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val initialise = journeyOf(
            setUserAnswerTo(RelationshipStatusPage, relationship),
            submitAnswer(CurrentlyReceivingChildBenefitPage, NotClaiming),
            setFullPaymentDetailsSingle,
            goTo(CheckApplicantDetailsPage)
          )

          startingFrom(CurrentlyReceivingChildBenefitPage)
            .run(
              initialise,
              goToChangeAnswer(CurrentlyReceivingChildBenefitPage),
              submitAnswer(CurrentlyReceivingChildBenefitPage, currentlyReceiving),
              pageMustBe(CurrentlyReceivingChangesTaskListPage),
              next,
              submitAnswer(EldestChildNamePage, childName),
              submitAnswer(EldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckApplicantDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(CurrentlyReceivingChangesTaskListPage, true)
            )
        }
      }
    }
  }
}