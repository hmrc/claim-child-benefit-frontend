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

import cats.data._
import cats.implicits._
import models.{AccountType, BankAccountDetails, BankAccountHolder, BankAccountInsightsResponseModel, BuildingSocietyDetails, UserAnswers}
import pages.payments._
import queries.{BankAccountInsightsResultQuery, Query}

sealed trait AccountDetailsWithHolder

final case class BankAccountWithHolder(holder: BankAccountHolder, details: BankAccountDetails, risk: Option[BankAccountInsightsResponseModel]) extends AccountDetailsWithHolder

final case class BuildingSocietyWithHolder(holder: BankAccountHolder, details: BuildingSocietyDetails) extends AccountDetailsWithHolder

object AccountDetailsWithHolder {

  def build(answers: UserAnswers): IorNec[Query, Option[AccountDetailsWithHolder]] =
    answers.getIor(ApplicantHasSuitableAccountPage).flatMap {
      case true =>
        getAccountDetails(answers).map(Some(_))
      case false =>
        Ior.Right(None)
    }

  private def getBank(answers: UserAnswers): IorNec[Query, BankAccountWithHolder] =
    (
      answers.getIor(BankAccountHolderPage),
      answers.getIor(BankAccountDetailsPage),
      answers
        .get(BankAccountInsightsResultQuery)
        .map(x => Ior.Right(Some(x)))
        .getOrElse(Ior.Right(None))
    ).parMapN(BankAccountWithHolder.apply)

  private def getBuildingSociety(answers: UserAnswers): IorNec[Query, BuildingSocietyWithHolder] =
    (
      answers.getIor(BankAccountHolderPage),
      answers.getIor(BuildingSocietyDetailsPage)
    ).parMapN(BuildingSocietyWithHolder.apply)

  private def getAccountDetails(answers: UserAnswers): IorNec[Query, AccountDetailsWithHolder] =
    answers.getIor(AccountTypePage).flatMap {
      case AccountType.SortCodeAccountNumber     => getBank(answers)
      case AccountType.BuildingSocietyRollNumber => getBuildingSociety(answers)
    }
}