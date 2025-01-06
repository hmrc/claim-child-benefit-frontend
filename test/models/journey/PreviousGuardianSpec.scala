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

class PreviousGuardianSpec extends AnyFreeSpec with Matchers with TryValues with OptionValues with ModelGenerators {

  private val index = Index(0)
  private val adultName = arbitrary[AdultName].sample.value
  private val ukAddress = arbitrary[UkAddress].sample.value
  private val internationalAddress = arbitrary[InternationalAddress].sample.value
  private val phoneNumber = "07777 777777"

  ".build" - {

    "when the child lives with the applicant" - {

      "and has lived with someone else" - {

        "must return a Previous Guardian when no details are known" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), true)
              .success
              .value
              .set(ChildLivedWithAnyoneElsePage(index), true)
              .success
              .value
              .set(PreviousGuardianNameKnownPage(index), false)
              .success
              .value

          val (errors, data) = PreviousGuardian.build(answers, index).pad

          data.value.value `mustEqual` PreviousGuardian(None, None, None)
          errors `must` `not` `be` defined
        }

        "must return a Previous Guardian when name is known but other details are not" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), true)
              .success
              .value
              .set(ChildLivedWithAnyoneElsePage(index), true)
              .success
              .value
              .set(PreviousGuardianNameKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianNamePage(index), adultName)
              .success
              .value
              .set(PreviousGuardianAddressKnownPage(index), false)
              .success
              .value
              .set(PreviousGuardianPhoneNumberKnownPage(index), false)
              .success
              .value

          val (errors, data) = PreviousGuardian.build(answers, index).pad

          data.value.value `mustEqual` PreviousGuardian(Some(adultName), None, None)
          errors `must` `not` `be` defined
        }

        "must return a Previous Guardian when all details are known" - {

          "with a UK address" in {

            val answers =
              UserAnswers("id")
                .set(ChildLivesWithApplicantPage(index), true)
                .success
                .value
                .set(ChildLivedWithAnyoneElsePage(index), true)
                .success
                .value
                .set(PreviousGuardianNameKnownPage(index), true)
                .success
                .value
                .set(PreviousGuardianNamePage(index), adultName)
                .success
                .value
                .set(PreviousGuardianAddressKnownPage(index), true)
                .success
                .value
                .set(PreviousGuardianAddressInUkPage(index), true)
                .success
                .value
                .set(PreviousGuardianUkAddressPage(index), ukAddress)
                .success
                .value
                .set(PreviousGuardianPhoneNumberKnownPage(index), true)
                .success
                .value
                .set(PreviousGuardianPhoneNumberPage(index), phoneNumber)
                .success
                .value

            val (errors, data) = PreviousGuardian.build(answers, index).pad

            data.value.value `mustEqual` PreviousGuardian(Some(adultName), Some(ukAddress), Some(phoneNumber))
            errors `must` `not` `be` defined
          }

          "with an international address" in {

            val answers =
              UserAnswers("id")
                .set(ChildLivesWithApplicantPage(index), true)
                .success
                .value
                .set(ChildLivedWithAnyoneElsePage(index), true)
                .success
                .value
                .set(PreviousGuardianNameKnownPage(index), true)
                .success
                .value
                .set(PreviousGuardianNamePage(index), adultName)
                .success
                .value
                .set(PreviousGuardianAddressKnownPage(index), true)
                .success
                .value
                .set(PreviousGuardianAddressInUkPage(index), false)
                .success
                .value
                .set(PreviousGuardianInternationalAddressPage(index), internationalAddress)
                .success
                .value
                .set(PreviousGuardianPhoneNumberKnownPage(index), true)
                .success
                .value
                .set(PreviousGuardianPhoneNumberPage(index), phoneNumber)
                .success
                .value

            val (errors, data) = PreviousGuardian.build(answers, index).pad

            data.value.value `mustEqual` PreviousGuardian(Some(adultName), Some(internationalAddress), Some(phoneNumber))
            errors `must` `not` `be` defined
          }
        }

        "must return errors when the user says they know details, but none are provided" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), true)
              .success
              .value
              .set(ChildLivedWithAnyoneElsePage(index), true)
              .success
              .value
              .set(PreviousGuardianNameKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianAddressKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianPhoneNumberKnownPage(index), true)
              .success
              .value

          val (errors, data) = PreviousGuardian.build(answers, index).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList `must` contain theSameElementsAs Seq(
            PreviousGuardianNamePage(index),
            PreviousGuardianAddressInUkPage(index),
            PreviousGuardianPhoneNumberPage(index)
          )
        }

        "must return errors when the user says the address is in the UK, but no address is provided" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), true)
              .success
              .value
              .set(ChildLivedWithAnyoneElsePage(index), true)
              .success
              .value
              .set(PreviousGuardianNameKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianNamePage(index), adultName)
              .success
              .value
              .set(PreviousGuardianAddressKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianAddressInUkPage(index), true)
              .success
              .value
              .set(PreviousGuardianPhoneNumberKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianPhoneNumberPage(index), phoneNumber)
              .success
              .value

          val (errors, data) = PreviousGuardian.build(answers, index).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList `must` contain `only` PreviousGuardianUkAddressPage(index)
        }

        "must return errors when the user says the address is not in the UK, but no address is provided" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), true)
              .success
              .value
              .set(ChildLivedWithAnyoneElsePage(index), true)
              .success
              .value
              .set(PreviousGuardianNameKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianNamePage(index), adultName)
              .success
              .value
              .set(PreviousGuardianAddressKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianAddressInUkPage(index), false)
              .success
              .value
              .set(PreviousGuardianPhoneNumberKnownPage(index), true)
              .success
              .value
              .set(PreviousGuardianPhoneNumberPage(index), phoneNumber)
              .success
              .value

          val (errors, data) = PreviousGuardian.build(answers, index).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList `must` contain `only` PreviousGuardianInternationalAddressPage(index)
        }
      }

      "and has not lived with anyone else" - {

        "must return None" in {

          val answers =
            UserAnswers("id")
              .set(ChildLivesWithApplicantPage(index), true)
              .success
              .value
              .set(ChildLivedWithAnyoneElsePage(index), false)
              .success
              .value

          val (errors, data) = PreviousGuardian.build(answers, index).pad

          data.value `must` `not` `be` defined
          errors `must` `not` `be` defined
        }
      }
    }

    "when the child does not live with the applicant" - {

      "must return None" in {

        val answers = UserAnswers("id").set(ChildLivesWithApplicantPage(index), false).success.value

        val (errors, data) = PreviousGuardian.build(answers, index).pad

        data.value `must` `not` `be` defined
        errors `must` `not` `be` defined
      }
    }
  }
}
