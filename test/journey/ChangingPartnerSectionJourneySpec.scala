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
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages._
import pages.income._
import pages.partner._
import pages.payments._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingPartnerSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def nino = arbitrary[Nino].sample.value
  private def eldestChildName = arbitrary[ChildName].sample.value
  private def childName = arbitrary[ChildName].sample.value
  private def adultName = arbitrary[AdultName].sample.value
  private val nationality = "nationality"
  private def bankDetails = arbitrary[BankAccountDetails].sample.value

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
    answersMustNotContain(PartnerNationalityPage),
    answersMustNotContain(PartnerIsHmfOrCivilServantPage),
    answersMustNotContain(PartnerClaimingChildBenefitPage),
    answersMustNotContain(PartnerEldestChildNamePage),
    answersMustNotContain(PartnerEldestChildDateOfBirthPage)
  )

  "when a user initially said they were married" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Married),
      submitAnswer(PartnerNamePage, adultName),
      submitAnswer(PartnerNinoKnownPage, true),
      submitAnswer(PartnerNinoPage, nino),
      submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
      submitAnswer(PartnerNationalityPage, nationality),
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
          answersMustContain(PartnerNationalityPage),
          answersMustContain(PartnerIsHmfOrCivilServantPage),
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
      submitAnswer(PartnerNationalityPage, nationality),
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
          answersMustContain(PartnerNationalityPage),
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
              submitAnswer(PartnerNationalityPage, nationality),
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
              submitAnswer(PartnerNationalityPage, nationality),
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
              submitAnswer(PartnerNationalityPage, nationality),
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
              submitAnswer(PartnerNationalityPage, nationality),
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

    val originalRelationship = Gen.oneOf(Single, Separated, Divorced).sample.value

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
              submitAnswer(PartnerNationalityPage, nationality),
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
              submitAnswer(PartnerNationalityPage, nationality),
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
              submitAnswer(PartnerNationalityPage, nationality),
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
              submitAnswer(PartnerNationalityPage, nationality),
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
