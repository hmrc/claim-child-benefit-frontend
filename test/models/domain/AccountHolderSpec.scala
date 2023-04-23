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

import models.journey
import models.{BankAccountDetails, BankAccountHolder, BankAccountInsightsResponseModel}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class AccountHolderSpec extends AnyFreeSpec with Matchers {

  ".build" - {

    "must return an account holder when given bank details" - {

      "where the account holder is the claimant" in {

        val bankDetails = journey.BankAccountWithHolder(
          holder = BankAccountHolder.Applicant,
          details = BankAccountDetails("first", "last", "123456", "12345678"),
          risk = Some(BankAccountInsightsResponseModel("correlation", 0, "foo"))
        )

        val result = AccountHolder.build(bankDetails)

        result mustEqual ClaimantAccountHolder
      }

      "where the account holder is joint, normalising inputs" in {

        val bankDetails = journey.BankAccountWithHolder(
          holder = BankAccountHolder.JointNames,
          details = BankAccountDetails("fîrśt", "la’st", "123456", "12345678"),
          risk = None
        )

        val result = AccountHolder.build(bankDetails)

        result mustEqual OtherAccountHolder(
          accountHolderType = AccountHolderType.Joint,
          forenames = "first", surname = "la'st"
        )
      }

      "where the account holder is someone else, normalising inputs" in {

        val bankDetails = journey.BankAccountWithHolder(
          holder = BankAccountHolder.SomeoneElse,
          details = BankAccountDetails("fîrśt", "la’st", "123456", "12345678"),
          risk = Some(BankAccountInsightsResponseModel("correlation", 0, "foo"))
        )

        val result = AccountHolder.build(bankDetails)

        result mustEqual OtherAccountHolder(
          accountHolderType = AccountHolderType.SomeoneElse,
          forenames = "first", surname = "la'st"
        )
      }
    }

    "must return an account holder when given building society details" - {

      "where the account holder is the claimant" in {

        val buildingSocietyDetails = journey.BuildingSocietyWithHolder(
          holder = BankAccountHolder.Applicant,
          details = models.BuildingSocietyDetails("first", "last", models.BuildingSociety.allBuildingSocieties.head, "roll number")
        )

        val result = AccountHolder.build(buildingSocietyDetails)

        result mustEqual ClaimantAccountHolder
      }

      "where the account holder is joint, normalising inputs" in {

        val buildingSocietyDetails = journey.BuildingSocietyWithHolder(
          holder = BankAccountHolder.JointNames,
          details = models.BuildingSocietyDetails("fîrśt", "la’st", models.BuildingSociety.allBuildingSocieties.head, "roll number")
        )

        val result = AccountHolder.build(buildingSocietyDetails)

        result mustEqual OtherAccountHolder(
          accountHolderType = AccountHolderType.Joint,
          forenames = "first", surname = "la'st"
        )
      }

      "where the account holder is someone else, normalising inputs" in {

        val buildingSocietyDetails = journey.BuildingSocietyWithHolder(
          holder = BankAccountHolder.SomeoneElse,
          details = models.BuildingSocietyDetails("fîrśt", "la’st", models.BuildingSociety.allBuildingSocieties.head, "roll number")
        )

        val result = AccountHolder.build(buildingSocietyDetails)

        result mustEqual OtherAccountHolder(
          accountHolderType = AccountHolderType.SomeoneElse,
          forenames = "first", surname = "la'st"
        )
      }
    }
  }

  ".writes" - {

    "must write Claimant Account Holder" in {

      Json.toJson[AccountHolder](ClaimantAccountHolder) mustEqual Json.obj("accountHolderType" -> "CLAIMANT")
    }

    "must write Other Account Holder" in {

      val accountHolder = OtherAccountHolder(
        accountHolderType = AccountHolderType.Joint,
        forenames = "foo",
        surname = "bar"
      )

      val expectedJson = Json.obj(
        "accountHolderType" -> "JOINT",
        "forenames" -> "foo",
        "surname" -> "bar"
      )
      
      Json.toJson[AccountHolder](accountHolder) mustEqual expectedJson
    }
  }
}
