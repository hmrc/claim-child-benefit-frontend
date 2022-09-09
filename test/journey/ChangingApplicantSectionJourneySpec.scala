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
import models.{Index, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.CheckYourAnswersPage
import pages.applicant._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingApplicantSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def previousName = arbitrary[String].sample.value
  private def nino         = arbitrary[Nino].sample.value
  private def address      = arbitrary[UkAddress].sample.value
  private def phoneNumber  = arbitrary[String].sample.value

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

    "changing to say they don't know it must remove the NINO and return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantNinoKnownPage, true),
        submitAnswer(ApplicantNinoPage, nino),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantNinoKnownPage),
          submitAnswer(ApplicantNinoKnownPage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantNinoPage)
        )
    }
  }

  "when the user initially said they did not know their NINO" - {

    "changing to say they do know it must collect the NINO then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantNinoKnownPage, false),
        submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantNinoKnownPage),
          submitAnswer(ApplicantNinoKnownPage, true),
          submitAnswer(ApplicantNinoPage, nino),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }

  "when the user initially said they had lived at their current address for a year" - {

    "changing that answer to `no` must collect their previous address then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
          submitAnswer(ApplicantPreviousUkAddressPage, address),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }

  "when the user initially said they had not lived at their current address for a year" - {

    "changing that answer to `yes` must remove their previous address and return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousUkAddressPage, address),
        submitAnswer(ApplicantPhoneNumberPage, phoneNumber),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
        .run(
          initialise,
          goToChangeAnswer(ApplicantLivedAtCurrentAddressOneYearPage),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(ApplicantPreviousUkAddressPage)
        )
    }
  }
}
