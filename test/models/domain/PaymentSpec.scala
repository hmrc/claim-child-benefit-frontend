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

package models.domain

import generators.ModelGenerators
import models.journey
import models.journey.PaymentPreference.ExistingAccount
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class PaymentSpec extends AnyFreeSpec with Matchers with OptionValues with ModelGenerators {

  ".build" - {

    "when the user has claimed Child Benefit" - {

      "must return None when the user wants to be paid" in {

        val paymentPreference = ExistingAccount(None)

        val result = Payment.build(paymentPreference, hasClaimedChildBenefit = true)
        result `must` `not` `be` defined
      }

      "must return None when the user does not want to be paid" in {

        val paymentPreference = journey.PaymentPreference.DoNotPay(None)

        val result = Payment.build(paymentPreference, hasClaimedChildBenefit = true)
        result `must` `not` `be` defined
      }
    }

    "when the user has not claimed Child Benefit" - {

      "must return a Payment with frequency of Every Four Weeks and no details when the user does not want to be paid" in {

        val paymentPreference = journey.PaymentPreference.DoNotPay(None)
        val result = Payment.build(paymentPreference, hasClaimedChildBenefit = false)
        result.value `mustEqual` Payment(PaymentFrequency.EveryFourWeeks, None)
      }

      "must return a Payment with no payment details" - {

        "when the user wants to be paid weekly but has no suitable account" in {

          val paymentPreference = journey.PaymentPreference.Weekly(None, None)

          val result = Payment.build(paymentPreference, hasClaimedChildBenefit = false)

          result.value `mustEqual` Payment(PaymentFrequency.Weekly, None)
        }

        "when the user wants to be paid every four weeks but has no suitable account" in {

          val paymentPreference = journey.PaymentPreference.EveryFourWeeks(None, None)

          val result = Payment.build(paymentPreference, hasClaimedChildBenefit = false)

          result.value `mustEqual` Payment(PaymentFrequency.EveryFourWeeks, None)
        }
      }

      "must return a Payment with payment details" - {

        "when the user wants to be paid weekly" - {

          "and provides a bank account" in {

            val bankAccountDetails = arbitrary[models.BankAccountDetails].sample.value
            val bankAccountWithHolder =
              journey.BankAccountWithHolder(models.BankAccountHolder.Applicant, bankAccountDetails, None)
            val paymentPreference = journey.PaymentPreference.Weekly(Some(bankAccountWithHolder), None)

            val result = Payment.build(paymentPreference, hasClaimedChildBenefit = false)

            val expectedResult = Payment(
              PaymentFrequency.Weekly,
              Some(
                BankDetails(
                  ClaimantAccountHolder,
                  BankAccount(bankAccountDetails.sortCodeTrimmed, bankAccountDetails.accountNumberPadded)
                )
              )
            )

            result.value `mustEqual` expectedResult
          }

          "and provides a building society account" in {

            val buildingSocietyDetails = arbitrary[models.BuildingSocietyDetails].sample.value
            val buildingSocietyWithHolder =
              journey.BuildingSocietyWithHolder(models.BankAccountHolder.Applicant, buildingSocietyDetails)
            val paymentPreference = journey.PaymentPreference.Weekly(Some(buildingSocietyWithHolder), None)

            val result = Payment.build(paymentPreference, hasClaimedChildBenefit = false)

            val expectedResult = Payment(
              PaymentFrequency.Weekly,
              Some(
                BuildingSocietyDetails(
                  ClaimantAccountHolder,
                  BuildingSocietyAccount(buildingSocietyDetails.buildingSociety.id, buildingSocietyDetails.rollNumber)
                )
              )
            )

            result.value `mustEqual` expectedResult
          }
        }

        "when the user wants to be paid every four weeks" - {

          "and provides a bank account" in {

            val bankAccountDetails = arbitrary[models.BankAccountDetails].sample.value
            val bankAccountWithHolder =
              journey.BankAccountWithHolder(models.BankAccountHolder.Applicant, bankAccountDetails, None)
            val paymentPreference = journey.PaymentPreference.EveryFourWeeks(Some(bankAccountWithHolder), None)

            val result = Payment.build(paymentPreference, hasClaimedChildBenefit = false)

            val expectedResult = Payment(
              PaymentFrequency.EveryFourWeeks,
              Some(
                BankDetails(
                  ClaimantAccountHolder,
                  BankAccount(bankAccountDetails.sortCodeTrimmed, bankAccountDetails.accountNumberPadded)
                )
              )
            )

            result.value `mustEqual` expectedResult
          }

          "and provides a building society account" in {

            val buildingSocietyDetails = arbitrary[models.BuildingSocietyDetails].sample.value
            val buildingSocietyWithHolder =
              journey.BuildingSocietyWithHolder(models.BankAccountHolder.Applicant, buildingSocietyDetails)
            val paymentPreference = journey.PaymentPreference.EveryFourWeeks(Some(buildingSocietyWithHolder), None)

            val result = Payment.build(paymentPreference, hasClaimedChildBenefit = false)

            val expectedResult = Payment(
              PaymentFrequency.EveryFourWeeks,
              Some(
                BuildingSocietyDetails(
                  ClaimantAccountHolder,
                  BuildingSocietyAccount(buildingSocietyDetails.buildingSociety.id, buildingSocietyDetails.rollNumber)
                )
              )
            )

            result.value `mustEqual` expectedResult
          }
        }
      }
    }
  }
}
