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

package models.domain

import generators.ModelGenerators
import models.journey
import models.journey.EldestChild
import models.{BankAccountDetails, BankAccountHolder, BankAccountInsightsResponseModel, BuildingSociety}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class PaymentDetailsSpec extends AnyFreeSpec with Matchers with OptionValues with ModelGenerators {

  ".build" - {

    "must return None when the payment preference is `do not pay" in {

      val paymentPreference = journey.PaymentPreference.DoNotPay(None)

      val result = PaymentDetails.build(paymentPreference)

      result must not be defined
    }

    "must return None when the payment preference is `existing account`" in {

      val paymentPreference = journey.PaymentPreference.ExistingAccount(
        eldestChild = EldestChild(arbitrary[models.ChildName].sample.value, LocalDate.now)
      )

      val result = PaymentDetails.build(paymentPreference)

      result must not be defined
    }

    "must return details when the payment preference is `weekly`" - {

      "with bank account details, trimming the sort code and padding the account number" in {

        val paymentPreference = journey.PaymentPreference.Weekly(
          Some(journey.BankAccountWithHolder(
            holder = BankAccountHolder.Applicant,
            details = BankAccountDetails("first", "last", "12-34 56", "123456"),
            risk = Some(BankAccountInsightsResponseModel("correlation", 0, "foo"))
          )),
          None
        )

        val result = PaymentDetails.build(paymentPreference)

        result.value mustEqual BankDetails(
          accountHolder = ClaimantAccountHolder,
          bankAccount = BankAccount("123456", "00123456")
        )
      }

      "with building society details" in {

        val paymentPreference = journey.PaymentPreference.Weekly(
          Some(journey.BuildingSocietyWithHolder(
            holder = BankAccountHolder.Applicant,
            details = models.BuildingSocietyDetails("first", "last", BuildingSociety.allBuildingSocieties.head, "roll number")
          )),
          None
        )

        val result = PaymentDetails.build(paymentPreference)

        result.value mustEqual BuildingSocietyDetails(
          accountHolder = ClaimantAccountHolder,
          buildingSocietyDetails = BuildingSocietyAccount(BuildingSociety.allBuildingSocieties.head.id, "roll number")
        )
      }

      "with no account details" in {

        val paymentPreference = journey.PaymentPreference.Weekly(None, None)

        val result = PaymentDetails.build(paymentPreference)

        result must not be defined
      }
    }

    "must return details when the payment preference is `every four weeks`" - {

      "with bank account details, trimming the sort code and padding the account number" in {

        val paymentPreference = journey.PaymentPreference.EveryFourWeeks(
          Some(journey.BankAccountWithHolder(
            holder = BankAccountHolder.Applicant,
            details = BankAccountDetails("first", "last", "12-34 56", "123456"),
            risk = None
          )),
          None
        )

        val result = PaymentDetails.build(paymentPreference)

        result.value mustEqual BankDetails(
          accountHolder = ClaimantAccountHolder,
          bankAccount = BankAccount("123456", "00123456")
        )
      }

      "with building society details" in {

        val paymentPreference = journey.PaymentPreference.EveryFourWeeks(
          Some(journey.BuildingSocietyWithHolder(
            holder = BankAccountHolder.Applicant,
            details = models.BuildingSocietyDetails("first", "last", BuildingSociety.allBuildingSocieties.head, "roll number")
          )),
          None
        )

        val result = PaymentDetails.build(paymentPreference)

        result.value mustEqual BuildingSocietyDetails(
          accountHolder = ClaimantAccountHolder,
          buildingSocietyDetails = BuildingSocietyAccount(BuildingSociety.allBuildingSocieties.head.id, "roll number")
        )
      }

      "with no account details" in {

        val paymentPreference = journey.PaymentPreference.EveryFourWeeks(None, None)

        val result = PaymentDetails.build(paymentPreference)

        result must not be defined
      }
    }
  }

  ".writes" - {

    "must write Bank Details" in {

      val accountHolder = ClaimantAccountHolder
      val bankAccount = BankAccount("123456", "12345678")
      val bankDetails = BankDetails(accountHolder, bankAccount)

      val expectedJson = Json.obj(
        "accountHolder" -> Json.obj(
          "accountHolderType" -> "CLAIMANT"
        ),
        "bankAccount" -> Json.obj(
          "sortCode" -> "123456",
          "accountNumber" -> "12345678"
        )
      )

      Json.toJson[PaymentDetails](bankDetails) mustEqual expectedJson
    }

    "must write Building Society Details" in {

      val accountHolder = ClaimantAccountHolder
      val buildingSociety = BuildingSocietyAccount("1234", "roll number")
      val buildingSocietyDetails = BuildingSocietyDetails(accountHolder, buildingSociety)

      val expectedJson = Json.obj(
        "accountHolder" -> Json.obj(
          "accountHolderType" -> "CLAIMANT"
        ),
        "buildingSocietyDetails" -> Json.obj(
          "buildingSociety" -> "1234",
          "rollNumber" -> "roll number"
        )
      )

      Json.toJson[PaymentDetails](buildingSocietyDetails) mustEqual expectedJson
    }
  }
}
