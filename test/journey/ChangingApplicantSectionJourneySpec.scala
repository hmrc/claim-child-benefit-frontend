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

package journey

import generators.ModelGenerators
import models.{Index, InternationalAddress, RelationshipStatus, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.{AlwaysLivedInUkPage, CheckYourAnswersPage, RelationshipStatusPage}
import pages.applicant._
import pages.partner.PartnerIsHmfOrCivilServantPage
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingApplicantSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def previousName         = arbitrary[String].sample.value
  private def nino                 = arbitrary[Nino].sample.value
  private def ukAddress            = arbitrary[UkAddress].sample.value
  private def internationalAddress = arbitrary[InternationalAddress].sample.value
  private def phoneNumber          = arbitrary[String].sample.value

  "when the user is HM Forces or a civil servant abroad" - {

    "and originally gave a current UK address" - {

      "changing to say their address is international must collect the address, remove the UK address and return to Check Answers" in{

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, true),
          setUserAnswerTo(RelationshipStatusPage, RelationshipStatus.Single),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantCurrentAddressInUkPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantCurrentAddressInUkPage),
            submitAnswer(ApplicantCurrentAddressInUkPage, false),
            submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(ApplicantCurrentUkAddressPage)
          )
      }
    }

    "and originally gave a current international address" - {

      "changing to say their address is in the UK must collect the address, remove the international address and return to Check Answers" in{

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, true),
          setUserAnswerTo(RelationshipStatusPage, RelationshipStatus.Single),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantCurrentAddressInUkPage, false),
          submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantCurrentAddressInUkPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantCurrentAddressInUkPage),
            submitAnswer(ApplicantCurrentAddressInUkPage, true),
            submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(ApplicantCurrentInternationalAddressPage)
          )
      }
    }
  }

  "when the user's partner is HM Forces or a civil servant living abroad" - {

    "and originally gave a current UK address" - {

      "changing to say their address is international must collect the address, remove the UK address and return to Check Answers" in{

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, true),
          setUserAnswerTo(RelationshipStatusPage, RelationshipStatus.Married),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, false),
          setUserAnswerTo(PartnerIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantCurrentAddressInUkPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantCurrentAddressInUkPage),
            submitAnswer(ApplicantCurrentAddressInUkPage, false),
            submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(ApplicantCurrentUkAddressPage)
          )
      }
    }

    "and originally gave a current international address" - {

      "changing to say their address is in the UK must collect the address, remove the international address and return to Check Answers" in{

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, true),
          setUserAnswerTo(RelationshipStatusPage, RelationshipStatus.Married),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, false),
          setUserAnswerTo(PartnerIsHmfOrCivilServantPage, true),
          submitAnswer(ApplicantCurrentAddressInUkPage, false),
          submitAnswer(ApplicantCurrentInternationalAddressPage, internationalAddress),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantCurrentAddressInUkPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantCurrentAddressInUkPage),
            submitAnswer(ApplicantCurrentAddressInUkPage, true),
            submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(ApplicantCurrentInternationalAddressPage)
          )
      }
    }
  }

  "when the user initially said they had previous names" - {

      val initialise = journeyOf(
        submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
        submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), previousName),
        submitAnswer(AddApplicantPreviousFamilyNamePage, true),
        submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), previousName),
        submitAnswer(AddApplicantPreviousFamilyNamePage, false),
        submitAnswer(ApplicantNinoKnownPage, false),
        goTo(CheckYourAnswersPage)
      )

    "changing that answer to `no` must remove them and return to Check Answers" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantHasPreviousFamilyNamePage),
          submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantPreviousFamilyNamePage(Index(1))),
          answersMustNotContain(ApplicantPreviousFamilyNamePage(Index(0)))
        )
    }

    "they must be able to add another name" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage),
          submitAnswer(AddApplicantPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(2)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage, false),
          pageMustBe(CheckYourAnswersPage)
        )
    }

    "they must be able to change a name" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage),
          goToChangeAnswer(ApplicantPreviousFamilyNamePage(Index(1))),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage, false),
          pageMustBe(CheckYourAnswersPage)
        )
    }

    "they must be able to remove a name, leaving at least one" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(1))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(1))),
          submitAnswer(AddApplicantPreviousFamilyNamePage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantPreviousFamilyNamePage(Index(1))),
          answersMustContain(ApplicantPreviousFamilyNamePage(Index(0)))
        )
    }

    "removing the last name must go to ask if the user has a previous name" in {

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(AddApplicantPreviousFamilyNamePage),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(1))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(1))),
          pageMustBe(AddApplicantPreviousFamilyNamePage),
          goTo(RemoveApplicantPreviousFamilyNamePage(Index(0))),
          removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(0))),
          pageMustBe(ApplicantHasPreviousFamilyNamePage)
        )
    }
  }

  "when the user initially said they had no previous names" - {

    "changing that answer to `yes` must collect the names then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
        submitAnswer(ApplicantNinoKnownPage, false),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantHasPreviousFamilyNamePage),
          submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
          submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), previousName),
          submitAnswer(AddApplicantPreviousFamilyNamePage, false),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }

  "when the user initially gave their NINO" - {

    "and has always lived in the UK" - {

      "changing to say they don't know their NINO must remove the NINO, ask if they have lived at their current address a year, and return to Check Answers" in {

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, true),
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantNinoKnownPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantNinoKnownPage),
            submitAnswer(ApplicantNinoKnownPage, false),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
            submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(ApplicantNinoPage)
          )
      }
    }

    "and has not always lived in the UK" - {

      "changing to say they don't know their NINO must remove the NINO, ask if they have lived at their current address a year, and return to Check Answers" in {

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, false),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          setUserAnswerTo(RelationshipStatusPage, RelationshipStatus.Single),
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
          submitAnswer(ApplicantCurrentAddressInUkPage, true),
          submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
          setUserAnswerTo(ApplicantPhoneNumberPage, phoneNumber),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantNinoKnownPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantNinoKnownPage),
            submitAnswer(ApplicantNinoKnownPage, false),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
            submitAnswer(ApplicantPreviousAddressInUkPage, false),
            submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
            pageMustBe(CheckYourAnswersPage),
            answersMustNotContain(ApplicantNinoPage)
          )
      }
    }
  }

  "when the user initially said they did not know their NINO" - {

    "changing to say they do know it must collect the NINO, remove their previous address then return to Check Answers" in {

      val initialise = journeyOf(
        setUserAnswerTo(AlwaysLivedInUkPage, false),
        setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
        submitAnswer(ApplicantNinoKnownPage, false),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        submitAnswer(ApplicantCurrentAddressInUkPage, true),
        submitAnswer(ApplicantCurrentUkAddressPage, ukAddress),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousAddressInUkPage, true),
        submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
        setUserAnswerTo(ApplicantPreviousInternationalAddressPage, internationalAddress),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantNinoKnownPage),
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantLivedAtCurrentAddressOneYearPage),
          answersMustNotContain(ApplicantPreviousAddressInUkPage),
          answersMustNotContain(ApplicantPreviousUkAddressPage),
          answersMustNotContain(ApplicantPreviousInternationalAddressPage)
        )
    }
  }

  "when the user initially said they had lived at their current address for a year" - {

    "changing that answer to `no` must collect their previous address then return to Check Answers" - {

      "when the address is in the UK" in {

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, false),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          setUserAnswerTo(ApplicantNinoKnownPage, false),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
            submitAnswer(ApplicantPreviousAddressInUkPage, true),
            submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
            pageMustBe(CheckYourAnswersPage)
          )
      }

      "when the address is not in the UK" in {

        val initialise = journeyOf(
          setUserAnswerTo(AlwaysLivedInUkPage, false),
          setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
          setUserAnswerTo(ApplicantNinoKnownPage, false),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
          goTo(CheckYourAnswersPage)
        )

        startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
          .run(
            initialise,
            goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
            submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
            submitAnswer(ApplicantPreviousAddressInUkPage, false),
            submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
            pageMustBe(CheckYourAnswersPage)
          )
      }
    }
  }

  "when the user initially said their previous address was in the UK" - {

    "changing that answer to `international` must collect their international address and remove their UK address" in {

      val initialise = journeyOf(
        setUserAnswerTo(AlwaysLivedInUkPage, false),
        setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousAddressInUkPage, true),
        submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantPreviousAddressInUkPage),
          submitAnswer(ApplicantPreviousAddressInUkPage, false),
          submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantPreviousUkAddressPage)
        )
    }
  }

  "when the user initially said their previous address was not in the UK" - {

    "changing that answer to `UK` must collect their UK address and remove their international address" in {

      val initialise = journeyOf(
        setUserAnswerTo(AlwaysLivedInUkPage, false),
        setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousAddressInUkPage, false),
        submitAnswer(ApplicantPreviousInternationalAddressPage, internationalAddress),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantPreviousAddressInUkPage),
          submitAnswer(ApplicantPreviousAddressInUkPage, true),
          submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantPreviousInternationalAddressPage)
        )
    }
  }

  "when the user initially said they had not lived at their current address for a year" - {

    "changing that answer to `yes` must remove their previous address and return to Check Answers" in {

      val initialise = journeyOf(
        setUserAnswerTo(AlwaysLivedInUkPage, false),
        setUserAnswerTo(ApplicantIsHmfOrCivilServantPage, true),
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousAddressInUkPage, true),
        submitAnswer(ApplicantPreviousUkAddressPage, ukAddress),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        goTo(CheckYourAnswersPage),
        setUserAnswerTo(ApplicantPreviousInternationalAddressPage, internationalAddress)
      )

      startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantPreviousAddressInUkPage),
          answersMustNotContain(ApplicantPreviousUkAddressPage),
          answersMustNotContain(ApplicantPreviousInternationalAddressPage)
        )
    }
  }
}
