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
import models.{AdultName, ChildName, EmploymentStatus, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.child.ChildNamePage
import pages.partner._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class PartnerJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users who don't know their partner's NINO, and the partner is not entitled to CB, must proceed to Child Name" in{

    val partnerName      = AdultName(None, "first", None, "last")
    val employmentStatus = Set(arbitrary[EmploymentStatus].sample.value)

    startingFrom(PartnerNamePage)
      .run(
        submitAnswer(PartnerNamePage, partnerName),
        submitAnswer(PartnerNinoKnownPage, false),
        submitAnswer(PartnerDateOfBirthPage, LocalDate.now),
        submitAnswer(PartnerNationalityPage, "nationality"),
        submitAnswer(PartnerEmploymentStatusPage, employmentStatus),
        submitAnswer(PartnerEntitledToChildBenefitPage, false),
        submitAnswer(PartnerWaitingForEntitlementDecisionPage, false),
        pageMustBe(ChildNamePage(Index(0)))
      )
  }

  "users who know their partner's NINO must be asked for it" in {

    val nino = arbitrary[Nino].sample.value

    startingFrom(PartnerNinoKnownPage)
      .run(
        submitAnswer(PartnerNinoKnownPage, true),
        submitAnswer(PartnerNinoPage, nino),
        pageMustBe(PartnerDateOfBirthPage)
      )
  }

  "users whose partner is entitled to Child Benefit must be asked for their partner's eldest child's details then go to Child Name" in {

    val childName = ChildName("first", None, "last")

    startingFrom(PartnerEntitledToChildBenefitPage)
      .run(
        submitAnswer(PartnerEntitledToChildBenefitPage, true),
        submitAnswer(PartnerEldestChildNamePage, childName),
        submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
        pageMustBe(ChildNamePage(Index(0)))
      )
  }

  "users whose partner is waiting to hear if they are entitled must be asked for their partner's eldest child's details then go to Child Name" in {

    val childName = ChildName("first", None, "last")

    startingFrom(PartnerEntitledToChildBenefitPage)
      .run(
        submitAnswer(PartnerEntitledToChildBenefitPage, false),
        submitAnswer(PartnerWaitingForEntitlementDecisionPage, true),
        submitAnswer(PartnerEldestChildNamePage, childName),
        submitAnswer(PartnerEldestChildDateOfBirthPage, LocalDate.now),
        pageMustBe(ChildNamePage(Index(0)))
      )
  }
}
