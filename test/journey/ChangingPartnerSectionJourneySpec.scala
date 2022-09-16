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
import models.{ChildName, Index, PartnerClaimingChildBenefit}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.CheckYourAnswersPage
import pages.child.ChildNamePage
import pages.partner._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ChangingPartnerSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def nino            = arbitrary[Nino].sample.value
  private def eldestChildName = arbitrary[ChildName].sample.value
  private def childName       = arbitrary[ChildName].sample.value

  "when a user initially said they knew their partner's NINO" - {

    "changing that answer to say they don't know it must remove the NINO, then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(PartnerNinoKnownPage, true),
        submitAnswer(PartnerNinoPage, nino),
        submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(PartnerNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerNinoKnownPage),
          submitAnswer(PartnerNinoKnownPage, false),
          pageMustBe(CheckYourAnswersPage),
          answersMustNotContain(PartnerNinoPage)
        )
    }
  }

  "when a user initially said they did not know their partner's NINO" - {

    "changing that answer to say they do know it must collect the NINO, then return to Check Answers" in {

      val initialise = journeyOf(
        submitAnswer(PartnerNinoKnownPage, false),
        submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
        goTo(CheckYourAnswersPage)
      )

      startingFrom(PartnerNinoKnownPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerNinoKnownPage),
          submitAnswer(PartnerNinoKnownPage, true),
          submitAnswer(PartnerNinoPage, nino),
          pageMustBe(CheckYourAnswersPage)
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
        goTo(CheckYourAnswersPage)
      )

      startingFrom(PartnerClaimingChildBenefitPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerClaimingChildBenefitPage),
          submitAnswer(PartnerClaimingChildBenefitPage, PartnerClaimingChildBenefit.NotClaiming),
          pageMustBe(ChildNamePage(Index(0))),
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
        goTo(CheckYourAnswersPage)
      )

      startingFrom(PartnerClaimingChildBenefitPage)
        .run(
          initialise,
          goToChangeAnswer(PartnerClaimingChildBenefitPage),
          submitAnswer(PartnerClaimingChildBenefitPage, partnerClaiming),
          submitAnswer(PartnerEldestChildNamePage, childName),
          submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
          pageMustBe(ChildNamePage(Index(0)))
        )
    }
  }
}
