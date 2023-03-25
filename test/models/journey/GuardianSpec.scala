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

package models.journey

import generators.ModelGenerators
import models.{AdultName, Index, InternationalAddress, UkAddress, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.child._

class GuardianSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues with ModelGenerators {

  private val index = Index(0)
  private val adultName = arbitrary[AdultName].sample.value
  private val ukAddress = arbitrary[UkAddress].sample.value
  private val internationalAddress = arbitrary[InternationalAddress].sample.value

  ".build" - {

    "when the child does not live with the applicant" - {

      "must return a Guardian when no details are known" in {

        val answers =
          UserAnswers("id")
            .set(ChildLivesWithApplicantPage(index), false).success.value
            .set(GuardianNameKnownPage(index), false).success.value

        val (errors, data) = Guardian.build(answers, index).pad

        data.value.value mustEqual Guardian(None, None)
        errors must not be defined
      }

      "must return a Guardian when name is known but address is not" in {

        val answers =
          UserAnswers("id")
            .set(ChildLivesWithApplicantPage(index), false).success.value
            .set(GuardianNameKnownPage(index), true).success.value
            .set(GuardianNamePage(index), adultName).success.value
            .set(GuardianAddressKnownPage(index), false).success.value

        val (errors, data) = Guardian.build(answers, index).pad

        data.value.value mustEqual Guardian(Some(adultName), None)
        errors must not be defined
      }

      "must return a Guardian when all details are known" - {

        "and the address is in the UK" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), false).success.value
              .set(GuardianNameKnownPage(index), true).success.value
              .set(GuardianNamePage(index), adultName).success.value
              .set(GuardianAddressKnownPage(index), true).success.value
              .set(GuardianAddressInUkPage(index), true).success.value
              .set(GuardianUkAddressPage(index), ukAddress).success.value

          val (errors, data) = Guardian.build(answers, index).pad

          data.value.value mustEqual Guardian(Some(adultName), Some(ukAddress))
          errors must not be defined
        }

        "and the address is not in the UK" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), false).success.value
              .set(GuardianNameKnownPage(index), true).success.value
              .set(GuardianNamePage(index), adultName).success.value
              .set(GuardianAddressKnownPage(index), true).success.value
              .set(GuardianAddressInUkPage(index), false).success.value
              .set(GuardianInternationalAddressPage(index), internationalAddress).success.value

          val (errors, data) = Guardian.build(answers, index).pad

          data.value.value mustEqual Guardian(Some(adultName), Some(internationalAddress))
          errors must not be defined
        }
      }

      "must return errors when the user says they know the details, but none are provided" in {

        val answers =
          UserAnswers("id")
            .set(ChildLivesWithApplicantPage(index), false).success.value
            .set(GuardianNameKnownPage(index), true).success.value

        val (errors, data) = Guardian.build(answers, index).pad

        data must not be defined
        errors.value.toChain.toList must contain theSameElementsAs Seq(
          GuardianNamePage(index),
          GuardianAddressKnownPage(index)
        )
      }

      "must return errors when the user says the address is in the UK, but no address is provided" in {

        val answers =
          UserAnswers("id")
            .set(ChildLivesWithApplicantPage(index), false).success.value
            .set(GuardianNameKnownPage(index), true).success.value
            .set(GuardianNamePage(index), adultName).success.value
            .set(GuardianAddressKnownPage(index), true).success.value
            .set(GuardianAddressInUkPage(index), true).success.value

        val (errors, data) = Guardian.build(answers, index).pad

        data must not be defined
        errors.value.toChain.toList must contain only GuardianUkAddressPage(index)
      }

      "must return errors when the user says the address is not in the UK, but no address is provided" in {

        val answers =
          UserAnswers("id")
            .set(ChildLivesWithApplicantPage(index), false).success.value
            .set(GuardianNameKnownPage(index), true).success.value
            .set(GuardianNamePage(index), adultName).success.value
            .set(GuardianAddressKnownPage(index), true).success.value
            .set(GuardianAddressInUkPage(index), false).success.value

        val (errors, data) = Guardian.build(answers, index).pad

        data must not be defined
        errors.value.toChain.toList must contain only GuardianInternationalAddressPage(index)
      }
    }

    "when the child lives with the applicant" - {

      "must return None" in {

        val answers = UserAnswers("id").set(ChildLivesWithApplicantPage(index), true).success.value

        val (errors, data) = Guardian.build(answers, index).pad

        data.value must not be defined
        errors must not be defined
      }
    }
  }
}
