/*
 * Copyright 2024 HM Revenue & Customs
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

package models.journey

import generators.ModelGenerators
import models.{AdultName, Index, InternationalAddress, UkAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.child._

class PreviousClaimantSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues with ModelGenerators {

  private val index = Index(0)
  private val adultName = arbitrary[AdultName].sample.value
  private val ukAddress = arbitrary[UkAddress].sample.value
  private val internationalAddress = arbitrary[InternationalAddress].sample.value

  ".build" - {

    "when someone has claimed for the child before" - {

      "must return a Previous Claimant when no details are known" in {

        val answers =
          UserAnswers("id")
            .set(AnyoneClaimedForChildBeforePage(index), true)
            .success
            .value
            .set(PreviousClaimantNameKnownPage(index), false)
            .success
            .value

        val (errors, data) = PreviousClaimant.build(answers, index).pad

        data.value.value `mustEqual` PreviousClaimant(None, None)
        errors `must` `not` `be` defined
      }

      "must return a Previous Claimant when name is known but address is not" in {

        val answers =
          UserAnswers("id")
            .set(AnyoneClaimedForChildBeforePage(index), true)
            .success
            .value
            .set(PreviousClaimantNameKnownPage(index), true)
            .success
            .value
            .set(PreviousClaimantNamePage(index), adultName)
            .success
            .value
            .set(PreviousClaimantAddressKnownPage(index), false)
            .success
            .value

        val (errors, data) = PreviousClaimant.build(answers, index).pad

        data.value.value `mustEqual` PreviousClaimant(Some(adultName), None)
        errors `must` not be defined
      }

      "must return a Previous Claimant when all details are known" - {

        "and the address is in the UK" in {

          val answers =
            UserAnswers("id")
              .set(AnyoneClaimedForChildBeforePage(index), true)
              .success
              .value
              .set(PreviousClaimantNameKnownPage(index), true)
              .success
              .value
              .set(PreviousClaimantNamePage(index), adultName)
              .success
              .value
              .set(PreviousClaimantAddressKnownPage(index), true)
              .success
              .value
              .set(PreviousClaimantAddressInUkPage(index), true)
              .success
              .value
              .set(PreviousClaimantUkAddressPage(index), ukAddress)
              .success
              .value

          val (errors, data) = PreviousClaimant.build(answers, index).pad

          data.value.value `mustEqual` PreviousClaimant(Some(adultName), Some(ukAddress))
          errors `must` not be defined
        }

        "and the address is not in the UK" in {

          val answers =
            UserAnswers("id")
              .set(AnyoneClaimedForChildBeforePage(index), true)
              .success
              .value
              .set(PreviousClaimantNameKnownPage(index), true)
              .success
              .value
              .set(PreviousClaimantNamePage(index), adultName)
              .success
              .value
              .set(PreviousClaimantAddressKnownPage(index), true)
              .success
              .value
              .set(PreviousClaimantAddressInUkPage(index), false)
              .success
              .value
              .set(PreviousClaimantInternationalAddressPage(index), internationalAddress)
              .success
              .value

          val (errors, data) = PreviousClaimant.build(answers, index).pad

          data.value.value `mustEqual` PreviousClaimant(Some(adultName), Some(internationalAddress))
          errors `must` not `be` defined
        }
      }

      "must return errors when the user says they know the details, but none are provided" in {

        val answers =
          UserAnswers("id")
            .set(AnyoneClaimedForChildBeforePage(index), true)
            .success
            .value
            .set(PreviousClaimantNameKnownPage(index), true)
            .success
            .value

        val (errors, data) = PreviousClaimant.build(answers, index).pad

        data `must` not `be` defined
        errors.value.toChain.toList must contain theSameElementsAs Seq(
          PreviousClaimantNamePage(index),
          PreviousClaimantAddressKnownPage(index)
        )
      }

      "must return errors when the user says the address is in the UK, but no address is provided" in {

        val answers =
          UserAnswers("id")
            .set(AnyoneClaimedForChildBeforePage(index), true)
            .success
            .value
            .set(PreviousClaimantNameKnownPage(index), true)
            .success
            .value
            .set(PreviousClaimantNamePage(index), adultName)
            .success
            .value
            .set(PreviousClaimantAddressKnownPage(index), true)
            .success
            .value
            .set(PreviousClaimantAddressInUkPage(index), true)
            .success
            .value

        val (errors, data) = PreviousClaimant.build(answers, index).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` PreviousClaimantUkAddressPage(index)
      }

      "must return errors when the user says the address is not in the UK, but no address is provided" in {

        val answers =
          UserAnswers("id")
            .set(AnyoneClaimedForChildBeforePage(index), true)
            .success
            .value
            .set(PreviousClaimantNameKnownPage(index), true)
            .success
            .value
            .set(PreviousClaimantNamePage(index), adultName)
            .success
            .value
            .set(PreviousClaimantAddressKnownPage(index), true)
            .success
            .value
            .set(PreviousClaimantAddressInUkPage(index), false)
            .success
            .value

        val (errors, data) = PreviousClaimant.build(answers, index).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` PreviousClaimantInternationalAddressPage(index)
      }
    }

    "when no one has claimed for the child before" - {

      "must return None" in {

        val answers = UserAnswers("id").set(AnyoneClaimedForChildBeforePage(index), false).success.value

        val (errors, data) = PreviousClaimant.build(answers, index).pad

        data.value `must` not `be` defined
        errors `must` not `be` defined
      }
    }
  }
}
