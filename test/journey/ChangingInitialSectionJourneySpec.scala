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

import models.{Benefits, RelationshipStatus}
import models.RelationshipStatus._
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.income._
import pages.{CheckYourAnswersPage, CohabitationDatePage, RelationshipStatusPage, SeparationDatePage}

import java.time.LocalDate

class ChangingInitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ScalaCheckPropertyChecks {

  private def benefits = Set(Gen.oneOf(Benefits.values).sample.value)

  "when a user initially said they were Married" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Married),
      submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
      submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
      submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Cohabiting must collect the date then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(CohabitationDatePage, LocalDate.now),
          pageMustBe(CheckYourAnswersPage),
          answersMustContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustContain(ApplicantOrPartnerBenefitsPage)
        )
    }

    "changing the answer to Separated must collect the separation date and go to collect single income details, and remove joint income details" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Separated),
          submitAnswer(SeparationDatePage, LocalDate.now),
          pageMustBe(ApplicantIncomeOver50kPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustNotContain(ApplicantOrPartnerBenefitsPage)
        )
    }

    "changing the answer to Single, Divorced or Widowed must remove joint income details, then go to collect single income details" in {

      forAll(Gen.oneOf(Single, Divorced, Widowed)) {
        status =>

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, status),
              pageMustBe(ApplicantIncomeOver50kPage),
              answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
              answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
              answersMustNotContain(ApplicantOrPartnerBenefitsPage)
            )
      }
    }
  }

  "when a user initially said they were Cohabiting" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Cohabiting),
      submitAnswer(CohabitationDatePage, LocalDate.now),
      submitAnswer(ApplicantOrPartnerIncomeOver50kPage, true),
      submitAnswer(ApplicantOrPartnerIncomeOver60kPage, true),
      submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Married must remove the cohabitation date then return to Check Answers" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Married),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(CohabitationDatePage),
          answersMustContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustContain(ApplicantOrPartnerBenefitsPage)
        )
    }

    "changing the answer to Separated must remove cohab date and joint income details, collect separation date and go to collect single income details" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Separated),
          submitAnswer(SeparationDatePage, LocalDate.now),
          pageMustBe(ApplicantIncomeOver50kPage),
          answersMustNotContain(CohabitationDatePage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
          answersMustNotContain(ApplicantOrPartnerBenefitsPage)
        )
    }

    "changing the answer to Single, Divorced or Widowed must remove cohab date and joint income details, then go to collect single income details" in {

      forAll(Gen.oneOf(Single, Divorced, Widowed)) {
        status =>

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, status),
              pageMustBe(ApplicantIncomeOver50kPage),
              answersMustNotContain(CohabitationDatePage),
              answersMustNotContain(ApplicantOrPartnerIncomeOver50kPage),
              answersMustNotContain(ApplicantOrPartnerIncomeOver60kPage),
              answersMustNotContain(ApplicantOrPartnerBenefitsPage)
            )
      }
    }
  }

  "when the user initially said they were Separated" - {

    val initialise = journeyOf(
      submitAnswer(RelationshipStatusPage, Separated),
      submitAnswer(SeparationDatePage, LocalDate.now),
      submitAnswer(ApplicantIncomeOver50kPage, true),
      submitAnswer(ApplicantIncomeOver60kPage, true),
      submitAnswer(ApplicantBenefitsPage, benefits),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Married must remove the separation date and single income details, then got to collect joint income details" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Married),
          pageMustBe(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(SeparationDatePage),
          answersMustNotContain(ApplicantIncomeOver50kPage),
          answersMustNotContain(ApplicantIncomeOver60kPage),
          answersMustNotContain(ApplicantBenefitsPage)
        )
    }

    "changing the answer to Cohabiting must remove the separation date and single income details, then got to collect cohab date and joint income details" in {

      startingFrom(RelationshipStatusPage)
        .run(
          initialise,
          goToChangeAnswer(RelationshipStatusPage),
          submitAnswer(RelationshipStatusPage, Cohabiting),
          submitAnswer(CohabitationDatePage, LocalDate.now),
          pageMustBe(ApplicantOrPartnerIncomeOver50kPage),
          answersMustNotContain(SeparationDatePage),
          answersMustNotContain(ApplicantIncomeOver50kPage),
          answersMustNotContain(ApplicantIncomeOver60kPage),
          answersMustNotContain(ApplicantBenefitsPage)
        )
    }

    "changing the answer to Single, Divorced or Widowed must remove the separation date and go to Check Answers" in {

      forAll(Gen.oneOf(Single, Divorced, Widowed)) {
        status =>

          startingFrom(RelationshipStatusPage)
            .run(
              initialise,
              goToChangeAnswer(RelationshipStatusPage),
              submitAnswer(RelationshipStatusPage, status),
              pageMustBe(CheckYourAnswersPage),
              answersMustNotContain(SeparationDatePage),
              answersMustContain(ApplicantIncomeOver50kPage),
              answersMustContain(ApplicantIncomeOver60kPage),
              answersMustContain(ApplicantBenefitsPage)
            )
      }
    }
  }

  "when the user initially said they were Single, Divorced or Widowed" - {

    def initialise(status: RelationshipStatus) = journeyOf(
      submitAnswer(RelationshipStatusPage, status),
      submitAnswer(ApplicantIncomeOver50kPage, true),
      submitAnswer(ApplicantIncomeOver60kPage, true),
      submitAnswer(ApplicantBenefitsPage, benefits),
      goTo(CheckYourAnswersPage)
    )

    "changing the answer to Married must remove single income details, then go to collect joint income details" in {

      Seq(Single, Divorced, Widowed).foreach { status =>

        startingFrom(RelationshipStatusPage)
          .run(
            initialise(status),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Married),
            pageMustBe(ApplicantOrPartnerIncomeOver50kPage),
            answersMustNotContain(ApplicantIncomeOver50kPage),
            answersMustNotContain(ApplicantIncomeOver60kPage),
            answersMustNotContain(ApplicantBenefitsPage)
          )
      }
    }

    "changing the answer to Cohabiting must remove single income details, then collect the cohabitation date then joint income details" in {

      Seq(Single, Divorced, Widowed).foreach { status =>

        startingFrom(RelationshipStatusPage)
          .run(
            initialise(status),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Cohabiting),
            submitAnswer(CohabitationDatePage, LocalDate.now),
            pageMustBe(ApplicantOrPartnerIncomeOver50kPage),
            answersMustNotContain(ApplicantIncomeOver50kPage),
            answersMustNotContain(ApplicantIncomeOver60kPage),
            answersMustNotContain(ApplicantBenefitsPage)
          )
      }
    }

    "changing the answer to Separated must collect the separation date then go to Check Answers" in {

      Seq(Single, Divorced, Widowed).foreach { status =>

        startingFrom(RelationshipStatusPage)
          .run(
            initialise(status),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, Separated),
            submitAnswer(SeparationDatePage, LocalDate.now),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantIncomeOver50kPage),
            answersMustContain(ApplicantIncomeOver60kPage),
            answersMustContain(ApplicantBenefitsPage)
          )
      }
    }

    "changing the answer to Single, Divorced or Widowed must go to Check Answers" in {

      Seq(Single, Divorced, Widowed).foreach { status =>

        val newStatus = Gen.oneOf(Single, Divorced, Widowed).sample.value

        startingFrom(RelationshipStatusPage)
          .run(
            initialise(status),
            goToChangeAnswer(RelationshipStatusPage),
            submitAnswer(RelationshipStatusPage, newStatus),
            pageMustBe(CheckYourAnswersPage),
            answersMustContain(ApplicantIncomeOver50kPage),
            answersMustContain(ApplicantIncomeOver60kPage),
            answersMustContain(ApplicantBenefitsPage)
          )
      }
    }
  }
}