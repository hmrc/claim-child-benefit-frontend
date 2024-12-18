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
import models.{AccountType, BankAccountDetails, BankAccountHolder, BankAccountInsightsResponseModel, BuildingSocietyDetails, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.payments._
import queries.BankAccountInsightsResultQuery

class AccountDetailsWithHolderSpec
    extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues {

  private val bankDetails = arbitrary[BankAccountDetails].sample.value
  private val buildingSociety = arbitrary[BuildingSocietyDetails].sample.value
  private val bankAccountRisk = BankAccountInsightsResponseModel("foo", 0, "bar")

  ".build" - {

    "when the applicant has a suitable account" - {

      "with a sort code and account number" - {

        "must return a Bank Account With Holder" - {

          "when bank account insights are available" in {

            val answers =
              UserAnswers("id")
                .set(ApplicantHasSuitableAccountPage, true)
                .success
                .value
                .set(AccountTypePage, AccountType.SortCodeAccountNumber)
                .success
                .value
                .set(BankAccountHolderPage, BankAccountHolder.Applicant)
                .success
                .value
                .set(BankAccountDetailsPage, bankDetails)
                .success
                .value
                .set(BankAccountInsightsResultQuery, bankAccountRisk)
                .success
                .value

            val (errors, data) = AccountDetailsWithHolder.build(answers).pad

            data.value.value `mustEqual` BankAccountWithHolder(
              BankAccountHolder.Applicant,
              bankDetails,
              Some(bankAccountRisk)
            )
            errors `must` `not` `be` defined
          }

          "when bank account insights are not available" in {

            val answers =
              UserAnswers("id")
                .set(ApplicantHasSuitableAccountPage, true)
                .success
                .value
                .set(AccountTypePage, AccountType.SortCodeAccountNumber)
                .success
                .value
                .set(BankAccountHolderPage, BankAccountHolder.Applicant)
                .success
                .value
                .set(BankAccountDetailsPage, bankDetails)
                .success
                .value

            val (errors, data) = AccountDetailsWithHolder.build(answers).pad

            data.value.value `mustEqual` BankAccountWithHolder(BankAccountHolder.Applicant, bankDetails, None)
            errors `must` `not` `be` defined
          }
        }

        "must return errors when any details are missing" in {

          val answers =
            UserAnswers("id")
              .set(ApplicantHasSuitableAccountPage, true)
              .success
              .value
              .set(AccountTypePage, AccountType.SortCodeAccountNumber)
              .success
              .value

          val (errors, data) = AccountDetailsWithHolder.build(answers).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList `must` contain theSameElementsAs Seq(
            BankAccountHolderPage,
            BankAccountDetailsPage
          )
        }
      }

      "with a building society roll number" - {

        "must return a Building Society With Holder" in {

          val answers =
            UserAnswers("id")
              .set(ApplicantHasSuitableAccountPage, true)
              .success
              .value
              .set(AccountTypePage, AccountType.BuildingSocietyRollNumber)
              .success
              .value
              .set(BankAccountHolderPage, BankAccountHolder.Applicant)
              .success
              .value
              .set(BuildingSocietyDetailsPage, buildingSociety)
              .success
              .value

          val (errors, data) = AccountDetailsWithHolder.build(answers).pad

          data.value.value `mustEqual` BuildingSocietyWithHolder(BankAccountHolder.Applicant, buildingSociety)
          errors `must` `not` `be` defined
        }

        "must return errors when any details are missing" in {

          val answers =
            UserAnswers("id")
              .set(ApplicantHasSuitableAccountPage, true)
              .success
              .value
              .set(AccountTypePage, AccountType.BuildingSocietyRollNumber)
              .success
              .value

          val (errors, data) = AccountDetailsWithHolder.build(answers).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList `must` contain theSameElementsAs Seq(
            BankAccountHolderPage,
            BuildingSocietyDetailsPage
          )
        }
      }

      "must return errors when the account type has not been answered" in {

        val answers =
          UserAnswers("id")
            .set(ApplicantHasSuitableAccountPage, true)
            .success
            .value

        val (errors, data) = AccountDetailsWithHolder.build(answers).pad

        data `must` `not` `be` defined
        errors.value.toChain.toList `must` contain `only` AccountTypePage
      }
    }

    "when the applicant does not have a suitable account" - {

      "must return None" in {

        val answers =
          UserAnswers("id")
            .set(ApplicantHasSuitableAccountPage, false)
            .success
            .value

        val (errors, data) = AccountDetailsWithHolder.build(answers).pad

        data.value `must` `not` `be` defined
        errors `must` `not` `be` defined
      }
    }

    "must return errors when whether the applicant has a suitable account is not answered" in {

      val answers = UserAnswers("id")

      val (errors, data) = AccountDetailsWithHolder.build(answers).pad

      data `must` `not` `be` defined
      errors.value.toChain.toList `must` contain `only` ApplicantHasSuitableAccountPage
    }
  }
}
