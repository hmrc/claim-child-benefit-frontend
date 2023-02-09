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
import models.RelationshipStatus._
import models.{Index, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.partner._
import pages.payments._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingPartnerSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def nino = arbitrary[Nino].sample.value
  private def eldestChildName = arbitrary[ChildName].sample.value
  private def childName = arbitrary[ChildName].sample.value
  private def adultName = arbitrary[AdultName].sample.value
  private def nationality = Gen.oneOf(Nationality.allNationalities).sample.value
  private def bankDetails = arbitrary[BankAccountDetails].sample.value
  private def country = Gen.oneOf(Country.internationalCountries).sample.value

  private val setFullPaymentDetailsSingle: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(ApplicantIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(WantToBePaidPage, true),
    setUserAnswerTo(ApplicantBenefitsPage, Benefits.qualifyingBenefits),
    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
    setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
    setUserAnswerTo(ApplicantHasSuitableAccountPage, true),
    setUserAnswerTo(BankAccountHolderPage, BankAccountHolder.Applicant),
    setUserAnswerTo(BankAccountDetailsPage, bankDetails)
  )

  private val setFullPaymentDetailsPartner: JourneyStep[Unit] = journeyOf(
    setUserAnswerTo(ApplicantOrPartnerIncomePage, Income.BetweenThresholds),
    setUserAnswerTo(WantToBePaidPage, true),
    setUserAnswerTo(ApplicantOrPartnerBenefitsPage, Benefits.qualifyingBenefits),
    setUserAnswerTo(PaymentFrequencyPage, PaymentFrequency.Weekly),
    setUserAnswerTo(WantToBePaidToExistingAccountPage, false),
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
    answersMustNotContain(WantToBePaidToExistingAccountPage),
    answersMustNotContain(ApplicantHasSuitableAccountPage),
    answersMustNotContain(BankAccountHolderPage),
    answersMustNotContain(BankAccountDetailsPage)
  )

  private val paymentDetailsMustRemainSingle: JourneyStep[Unit] = journeyOf(
    answersMustContain(ApplicantIncomePage),
    answersMustContain(WantToBePaidPage),
    answersMustContain(ApplicantBenefitsPage),
    answersMustContain(PaymentFrequencyPage),
    answersMustContain(WantToBePaidToExistingAccountPage),
    answersMustContain(ApplicantHasSuitableAccountPage),
    answersMustContain(BankAccountHolderPage),
    answersMustContain(BankAccountDetailsPage)
  )

  private val paymentDetailsMustRemainPartner: JourneyStep[Unit] = journeyOf(
    answersMustContain(ApplicantOrPartnerIncomePage),
    answersMustContain(WantToBePaidPage),
    answersMustContain(ApplicantOrPartnerBenefitsPage),
    answersMustContain(PaymentFrequencyPage),
    answersMustContain(WantToBePaidToExistingAccountPage),
    answersMustContain(ApplicantHasSuitableAccountPage),
    answersMustContain(BankAccountHolderPage),
    answersMustContain(BankAccountDetailsPage)
  )

  private val partnerDetailsMustHaveBeenRemoved: JourneyStep[Unit] = journeyOf(
    answersMustNotContain(PartnerNamePage),
    answersMustNotContain(PartnerNinoKnownPage),
    answersMustNotContain(PartnerNinoPage),
    answersMustNotContain(PartnerDateOfBirthPage),
    answersMustNotContain(PartnerNationalityPage(Index(0))),
    answersMustNotContain(PartnerWorkedAbroadPage),
    answersMustNotContain(CountryPartnerWorkedPage(Index(0))),
    answersMustNotContain(PartnerReceivedBenefitsAbroadPage),
    answersMustNotContain(CountryPartnerReceivedBenefitsPage(Index(0))),
    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
    answersMustNotContain(PartnerClaimingChildBenefitPage),
    answersMustNotContain(PartnerEldestChildNamePage),
    answersMustNotContain(PartnerEldestChildDateOfBirthPage),
  )

  "when a user initially said they were married" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Married),
      submitAnswer(PartnerNamePage, adultName),
      submitAnswer(PartnerNinoKnownPage, true),
      submitAnswer(PartnerNinoPage, nino),
      submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
      submitAnswer(PartnerNationalityPage(Index(0)), nationality),
      submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
      submitAnswer(PartnerWorkedAbroadPage, true),
      submitAnswer(CountryPartnerWorkedPage(Index(0)), country),
      submitAnswer(AddCountryPartnerWorkedPage(Some(Index(0))), false),
      submitAnswer(PartnerReceivedBenefitsAbroadPage, true),
      submitAnswer(CountryPartnerReceivedBenefitsPage(Index(0)), country),
      submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(0))), false),
      submitAnswer(PartnerIsHmfOrCivilServantPage, false),
      submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
      submitAnswer(PartnerEldestChildNamePage, childName),
      submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
      pageMustBe(CheckPartnerDetailsPage)
    )

    "changing to say they are cohabiting must collect cohabitation date then go to Check Partner" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          setFullPaymentDetailsPartner,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(CohabitationDatePage, LocalDate.now),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustContain(PartnerNamePage),
          answersMustContain(PartnerNinoKnownPage),
          answersMustContain(PartnerNinoPage),
          answersMustContain(PartnerDateOfBirthPage),
          answersMustContain(PartnerNationalityPage(Index(0))),
          answersMustContain(PartnerIsHmfOrCivilServantPage),
          answersMustContain(PartnerWorkedAbroadPage),
          answersMustContain(CountryPartnerWorkedPage(Index(0))),
          answersMustContain(PartnerReceivedBenefitsAbroadPage),
          answersMustContain(CountryPartnerReceivedBenefitsPage(Index(0))),
          answersMustContain(PartnerClaimingChildBenefitPage),
          answersMustContain(PartnerEldestChildNamePage),
          answersMustContain(PartnerEldestChildDateOfBirthPage),
          paymentDetailsMustRemainPartner
        )
    }

    "changing to say they are separated" - {

      "when they have answered some payment details" - {

        "must remove partner and payment details, tell the user payment details were removed, collect separation date, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsPartner,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Separated),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(SeparationDatePage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              partnerDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must remove partner details, collect separation date, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Separated),
              submitAnswer(SeparationDatePage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              partnerDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }

    "changing to say they are single, divorced or widowed" - {

      def relationship: RelationshipStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

      "when they have given answered some payment details" - {

        "must remove partner and payment details, tell the user payment details were removed, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsPartner,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, relationship),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              partnerDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must remove partner details then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, relationship),
              pageMustBe(CheckPartnerDetailsPage),
              partnerDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }
  }

  "when a user initially said they were cohabiting" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Cohabiting),
      submitAnswer(CohabitationDatePage, LocalDate.now),
      submitAnswer(PartnerNamePage, adultName),
      submitAnswer(PartnerNinoKnownPage, true),
      submitAnswer(PartnerNinoPage, nino),
      submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
      submitAnswer(PartnerNationalityPage(Index(0)), nationality),
      submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
      submitAnswer(PartnerWorkedAbroadPage, true),
      submitAnswer(CountryPartnerWorkedPage(Index(0)), country),
      submitAnswer(AddCountryPartnerWorkedPage(Some(Index(0))), false),
      submitAnswer(PartnerReceivedBenefitsAbroadPage, true),
      submitAnswer(CountryPartnerReceivedBenefitsPage(Index(0)), country),
      submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(0))), false),
      submitAnswer(PartnerIsHmfOrCivilServantPage, false),
      submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
      submitAnswer(PartnerEldestChildNamePage, childName),
      submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
      pageMustBe(CheckPartnerDetailsPage)
    )

    "changing to say they are married must remove cohabitation date then go to Check Partner" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          setFullPaymentDetailsPartner,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Married),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustNotContain(CohabitationDatePage),
          answersMustContain(PartnerNamePage),
          answersMustContain(PartnerNinoKnownPage),
          answersMustContain(PartnerNinoPage),
          answersMustContain(PartnerDateOfBirthPage),
          answersMustContain(PartnerNationalityPage(Index(0))),
          answersMustContain(PartnerWorkedAbroadPage),
          answersMustContain(CountryPartnerWorkedPage(Index(0))),
          answersMustContain(PartnerReceivedBenefitsAbroadPage),
          answersMustContain(CountryPartnerReceivedBenefitsPage(Index(0))),
          answersMustContain(PartnerIsHmfOrCivilServantPage),
          answersMustContain(PartnerClaimingChildBenefitPage),
          answersMustContain(PartnerEldestChildNamePage),
          answersMustContain(PartnerEldestChildDateOfBirthPage),
          paymentDetailsMustRemainPartner
        )
    }

    "changing to say they are separated" - {

      "when they have answered some payment details" - {

        "must remove cohabitation date, partner and payment details, tell the user payment details were removed, collect separation date, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsPartner,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Separated),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(SeparationDatePage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              partnerDetailsMustHaveBeenRemoved,
              answersMustNotContain(CohabitationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must remove cohabitation date and partner details, collect separation date, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Separated),
              submitAnswer(SeparationDatePage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              partnerDetailsMustHaveBeenRemoved,
              answersMustNotContain(CohabitationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }

    "changing to say they are single, divorced or widowed" - {

      def relationship: RelationshipStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

      "when they have given answered some payment details" - {

        "must remove cohabitation date, partner and payment details, tell the user payment details were removed, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsPartner,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, relationship),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              partnerDetailsMustHaveBeenRemoved,
              answersMustNotContain(CohabitationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must remove cohabitation date and partner details then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, relationship),
              pageMustBe(CheckPartnerDetailsPage),
              partnerDetailsMustHaveBeenRemoved,
              answersMustNotContain(CohabitationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }
  }

  "when a user initially said they were separated" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Separated),
      submitAnswer(SeparationDatePage, LocalDate.now),
      pageMustBe(CheckPartnerDetailsPage)
    )

    "changing to say they are married" - {

      "when they have answered some payment details" - {

        "must remove separation date and payment details, tell the user they were removed, collect partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsSingle,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answersMustNotContain(SeparationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must remove separation date, collect partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              answersMustNotContain(SeparationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }

    "changing to say they are cohabiting" - {

      "when they have answered some payment details" - {

        "must remove separation date and payment details, tell the user they were removed, collect cohabitation date and partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsSingle,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(CohabitationDatePage, LocalDate.now),
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answersMustNotContain(SeparationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must remove separation date, collect cohabitation date and partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              answersMustNotContain(SeparationDatePage),
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }

    "changing to say they are single, divorced or widowed" - {

      def relationship: RelationshipStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

      "must remove separation date then go to Check Partner" in {

        startingFrom(RelationshipStatusPage)
          .run(
            initialise,
            setFullPaymentDetailsSingle,
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, relationship),
            pageMustBe(CheckPartnerDetailsPage),
            answersMustNotContain(SeparationDatePage),
            paymentDetailsMustRemainSingle,
            answerMustEqual(RelationshipStatusChangesTaskListPage,false)
          )
      }
    }
  }

  "when a user initially said they were single, widowed or divorced" - {

    val originalRelationship = Gen.oneOf(Single, Widowed, Divorced).sample.value

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, originalRelationship),
      pageMustBe(CheckPartnerDetailsPage)
    )

    "changing to say they are married" - {

      "when they have answered some payment details" - {

        "must remove payment details, tell the user they were removed, collect partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsSingle,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must collect partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Married),
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }

    "changing to say they are cohabiting" - {

      "when they have answered some payment details" - {

        "must remove payment details, tell the user they were removed, collect cohabitation date and partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              setFullPaymentDetailsSingle,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              pageMustBe(RelationshipStatusChangesTaskListPage),
              next,
              submitAnswer(CohabitationDatePage, LocalDate.now),
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              paymentDetailsMustHaveBeenRemoved,
              answerMustEqual(RelationshipStatusChangesTaskListPage, true)
            )
        }
      }

      "when they had not already answered any payment details" - {

        "must collect cohabitation date and partner details, then go to Check Partner" in {

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              submitAnswer(PartnerNamePage, adultName),
              submitAnswer(PartnerNinoKnownPage, true),
              submitAnswer(PartnerNinoPage, nino),
              submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
              submitAnswer(PartnerNationalityPage(Index(0)), nationality),
              submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
              submitAnswer(PartnerWorkedAbroadPage, false),
              submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
              submitAnswer(PartnerIsHmfOrCivilServantPage, false),
              submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.GettingPayments),
              submitAnswer(PartnerEldestChildNamePage, childName),
              submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
              pageMustBe(CheckPartnerDetailsPage),
              answerMustEqual(RelationshipStatusChangesTaskListPage,false)
            )
        }
      }
    }

    "changing to say they are separated" - {

      "must collect separation date then go to Check Partner" in {

        startingFrom(RelationshipStatusPage)
          .run(
            initialise,
            setFullPaymentDetailsSingle,
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            pageMustBe(CheckPartnerDetailsPage),
            paymentDetailsMustRemainSingle,
            answerMustEqual(RelationshipStatusChangesTaskListPage,false)
          )
      }
    }

    "changing to say they are single, divorced or widowed" - {

      val relationship = originalRelationship
      val newRelationship = Gen.oneOf(Set(Single, Divorced, Widowed) - relationship).sample.value

      "must go to Check Partner" in {

        startingFrom(RelationshipStatusPage)
          .run(
            initialise,
            setFullPaymentDetailsSingle,
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, newRelationship),
            pageMustBe(CheckPartnerDetailsPage),
            paymentDetailsMustRemainSingle,
            answerMustEqual(RelationshipStatusChangesTaskListPage,false)
          )
      }
    }
  }

  "when a user initially said they knew their partner's NINO" - {

    "changing that answer to say they don't know it must remove the NINO, then return to Check Partner Details" in {

      val initialise = journeyOf(
        submitAnswer(PartnerNinoKnownPage, true),
        submitAnswer(PartnerNinoPage, nino),
        submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
        goTo(CheckPartnerDetailsPage)
      )

      startingFrom(PartnerNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerNinoKnownPage),
          submitAnswer(PartnerNinoKnownPage, false),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustNotContain(PartnerNinoPage)
        )
    }
  }

  "when a user initially said they did not know their partner's NINO" - {

    "changing that answer to say they do know it must collect the NINO, then return to Check Partner Details" in {

      val initialise = journeyOf(
        submitAnswer(PartnerNinoKnownPage, false),
        submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
        goTo(CheckPartnerDetailsPage)
      )

      startingFrom(PartnerNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerNinoKnownPage),
          submitAnswer(PartnerNinoKnownPage, true),
          submitAnswer(PartnerNinoPage, nino),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }
  
  "the user must be able to add a nationality for their partner" in {

    val initialise = journeyOf(
      submitAnswer(PartnerNationalityPage(Index(0)), nationality),
      submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
      submitAnswer(PartnerWorkedAbroadPage, false),
      goTo(CheckPartnerDetailsPage)
    )

    startingFrom(PartnerNationalityPage(Index(0)))
      .run(
        initialise,
        goToChangeAnswer(AddPartnerNationalityPage()),
        submitAnswer(AddPartnerNationalityPage(), true),
        submitAnswer(PartnerNationalityPage(Index(1)), nationality),
        submitAnswer(AddPartnerNationalityPage(Some(Index(1))), false),
        pageMustBe(CheckPartnerDetailsPage)
      )
  }

  "the user must be able to remove a nationality from their partner, leaving at least one" in {

    val initialise = journeyOf(
      submitAnswer(PartnerNationalityPage(Index(0)), nationality),
      submitAnswer(AddPartnerNationalityPage(Some(Index(0))), true),
      submitAnswer(PartnerNationalityPage(Index(1)), nationality),
      submitAnswer(AddPartnerNationalityPage(Some(Index(1))), false),
      submitAnswer(PartnerWorkedAbroadPage, false),
      goTo(CheckPartnerDetailsPage)
    )

    startingFrom(PartnerNationalityPage(Index(0)))
      .run(
        initialise,
        goToChangeAnswer(AddPartnerNationalityPage()),
        goTo(RemovePartnerNationalityPage(Index(1))),
        removeAddToListItem(PartnerNationalityPage(Index(1))),
        submitAnswer(AddPartnerNationalityPage(), false),
        pageMustBe(CheckPartnerDetailsPage)
      )
  }

  "removing their partner's last nationality must ask the user their nationality" in {

    val initialise = journeyOf(
      submitAnswer(PartnerNationalityPage(Index(0)), nationality),
      submitAnswer(AddPartnerNationalityPage(Some(Index(0))), false),
      submitAnswer(PartnerWorkedAbroadPage, true),
      goTo(CheckPartnerDetailsPage)
    )

    startingFrom(PartnerNationalityPage(Index(0)))
      .run(
        initialise,
        goToChangeAnswer(AddPartnerNationalityPage()),
        goTo(RemovePartnerNationalityPage(Index(0))),
        removeAddToListItem(PartnerNationalityPage(Index(0))),
        pageMustBe(PartnerNationalityPage(Index(0)))
      )
  }

  "when the user initially said their partner had not worked abroad" - {

    "changing to say they had worked abroad must collect the countries" in {

      val initialise = journeyOf(
        submitAnswer(PartnerWorkedAbroadPage, false),
        submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
        goTo(CheckPartnerDetailsPage)
      )

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerWorkedAbroadPage),
          submitAnswer(PartnerWorkedAbroadPage, true),
          submitAnswer(CountryPartnerWorkedPage(Index(0)), country),
          submitAnswer(AddCountryPartnerWorkedPage(Some(Index(0))), false),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }

  "when the user initially said their partner had worked abroad" - {

    val initialise = journeyOf(
      submitAnswer(PartnerWorkedAbroadPage, true),
      submitAnswer(CountryPartnerWorkedPage(Index(0)), country),
      submitAnswer(AddCountryPartnerWorkedPage(Some(Index(0))), true),
      submitAnswer(CountryPartnerWorkedPage(Index(1)), country),
      submitAnswer(AddCountryPartnerWorkedPage(Some(Index(1))), false),
      submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
      goTo(CheckPartnerDetailsPage)
    )

    "changing to say they haven't worked abroad must remove the countries" in {

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerWorkedAbroadPage),
          submitAnswer(PartnerWorkedAbroadPage, false),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustNotContain(CountryPartnerWorkedPage(Index(1))),
          answersMustNotContain(CountryPartnerWorkedPage(Index(0)))
        )
    }

    "they must be able to add another country" in {

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryPartnerWorkedPage()),
          submitAnswer(AddCountryPartnerWorkedPage(), true),
          submitAnswer(CountryPartnerWorkedPage(Index(2)), country),
          submitAnswer(AddCountryPartnerWorkedPage(Some(Index(2))), false),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }

    "they must be able to remove a country, leaving at least one" in {

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryPartnerWorkedPage()),
          goTo(RemoveCountryPartnerWorkedPage(Index(1))),
          removeAddToListItem(CountryPartnerWorkedPage(Index(1))),
          submitAnswer(AddCountryPartnerWorkedPage(), false),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustNotContain(CountryPartnerWorkedPage(Index(1))),
          answersMustContain(CountryPartnerWorkedPage(Index(0)))
        )
    }

    "removing the last country must ask if the user has worked abroad" in {

      startingFrom(PartnerWorkedAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryPartnerWorkedPage()),
          goTo(RemoveCountryPartnerWorkedPage(Index(1))),
          removeAddToListItem(CountryPartnerWorkedPage(Index(1))),
          goTo(RemoveCountryPartnerWorkedPage(Index(0))),
          removeAddToListItem(CountryPartnerWorkedPage(Index(0))),
          pageMustBe(PartnerWorkedAbroadPage)
        )
    }
  }

  "when the user initially said their partner had not received benefits abroad" - {

    "changing to say they had received benefits abroad must collect the countries" in {

      val initialise = journeyOf(
        submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
        submitAnswer(PartnerIsHmfOrCivilServantPage, false),
        goTo(CheckPartnerDetailsPage)
      )

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerReceivedBenefitsAbroadPage),
          submitAnswer(PartnerReceivedBenefitsAbroadPage, true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(0)), country),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(0))), false),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }

  "when the user initially said their partner had received benefits abroad" - {

    val initialise = journeyOf(
      submitAnswer(PartnerReceivedBenefitsAbroadPage, true),
      submitAnswer(CountryPartnerReceivedBenefitsPage(Index(0)), country),
      submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(0))), true),
      submitAnswer(CountryPartnerReceivedBenefitsPage(Index(1)), country),
      submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(1))), false),
      submitAnswer(PartnerIsHmfOrCivilServantPage, false),
      goTo(CheckPartnerDetailsPage)
    )

    "changing to say they haven't received benefits abroad must remove the countries" in {

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerReceivedBenefitsAbroadPage),
          submitAnswer(PartnerReceivedBenefitsAbroadPage, false),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustNotContain(CountryPartnerReceivedBenefitsPage(Index(0)))
        )
    }

    "they must be able to add another country" in {

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryPartnerReceivedBenefitsPage()),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(), true),
          submitAnswer(CountryPartnerReceivedBenefitsPage(Index(2)), country),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(Some(Index(2))), false),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }

    "they must be able to remove a country, leaving at least one" in {

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryPartnerReceivedBenefitsPage()),
          goTo(RemoveCountryPartnerReceivedBenefitsPage(Index(1))),
          removeAddToListItem(CountryPartnerReceivedBenefitsPage(Index(1))),
          submitAnswer(AddCountryPartnerReceivedBenefitsPage(), false),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustContain(CountryPartnerReceivedBenefitsPage(Index(0)))
        )
    }

    "removing the last country must ask if the user has received benefits abroad" in {

      startingFrom(PartnerReceivedBenefitsAbroadPage)
        .run(
          initialise,
          goToChangeAnswer(AddCountryPartnerReceivedBenefitsPage()),
          goTo(RemoveCountryPartnerReceivedBenefitsPage(Index(1))),
          removeAddToListItem(CountryPartnerReceivedBenefitsPage(Index(1))),
          goTo(RemoveCountryPartnerReceivedBenefitsPage(Index(0))),
          removeAddToListItem(CountryPartnerReceivedBenefitsPage(Index(0))),
          pageMustBe(PartnerReceivedBenefitsAbroadPage)
        )
    }
  }

  "when a user initially said their partner was entitled to claim Child Benefit or waiting to hear" - {

    "changing that answer should remove their eldest child's details" in {

      import models.PartnerClaimingChildBenefit._

      val partnerClaiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value

      val initialise = journeyOf(
        submitAnswer(PartnerClaimingChildBenefitPage, partnerClaiming),
        submitAnswer(PartnerEldestChildNamePage, eldestChildName),
        submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
        goTo(CheckPartnerDetailsPage)
      )

      startingFrom(PartnerClaimingChildBenefitPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerClaimingChildBenefitPage),
          submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming),
          pageMustBe(CheckPartnerDetailsPage),
          answersMustNotContain(PartnerEldestChildNamePage),
          answersMustNotContain(PartnerEldestChildDateOfBirthPage)
        )
    }
  }

  "when a user initially said their partner was not entitled to claim Child Benefit or waiting to hear" - {

    "changing that answer should collect their oldest child's details" in {

      import models.PartnerClaimingChildBenefit._

      val partnerClaiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value

      val initialise = journeyOf(
        submitAnswer(PartnerClaimingChildBenefitPage, NotClaiming),
        goTo(CheckPartnerDetailsPage)
      )

      startingFrom(PartnerClaimingChildBenefitPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerClaimingChildBenefitPage),
          submitAnswer(PartnerClaimingChildBenefitPage, partnerClaiming),
          submitAnswer(PartnerEldestChildNamePage, childName),
          submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
          pageMustBe(CheckPartnerDetailsPage)
        )
    }
  }
}
