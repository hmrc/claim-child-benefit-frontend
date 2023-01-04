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
import models.{Benefits, Income}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.income._
import pages.payments.CurrentlyReceivingChildBenefitPage

class IncomeJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users with a partner" - {

    val benefits = Gen.nonEmptyListOf(Gen.oneOf(Benefits.values)).map(_.toSet).sample.value
    val income   = arbitrary[Income].sample.value

    "must proceed to the payments section" in {

      startingFrom(ApplicantOrPartnerIncomePage)
        .run(
          submitAnswer(ApplicantOrPartnerIncomePage, income),
          submitAnswer(ApplicantOrPartnerBenefitsPage, benefits),
          pageMustBe(CurrentlyReceivingChildBenefitPage)
        )
    }
  }

  "users without a partner" - {

    val benefits = Gen.nonEmptyListOf(Gen.oneOf(Benefits.values)).map(_.toSet).sample.value
    val income   = arbitrary[Income].sample.value

    "must proceed to the payments section" in {

      startingFrom(ApplicantIncomePage)
        .run(
          submitAnswer(ApplicantIncomePage, income),
          submitAnswer(ApplicantBenefitsPage, benefits),
          pageMustBe(CurrentlyReceivingChildBenefitPage)
        )
    }
  }
}
