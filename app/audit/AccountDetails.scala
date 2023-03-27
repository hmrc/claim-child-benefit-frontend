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

package audit

import models.BankAccountInsightsResponseModel
import models.journey
import play.api.libs.json.{Json, Writes}


sealed trait AccountDetails

object AccountDetails {
  implicit lazy val writes: Writes[AccountDetails] = Writes {
    case x: BankAccount => Json.toJson(x)(BankAccount.writes)
    case x: BuildingSociety => Json.toJson(x)(BuildingSociety.writes)
  }

  def build(details: journey.AccountDetailsWithHolder): AccountDetails =
    details match {
      case bankAccount: journey.BankAccountWithHolder =>
        BankAccount(
          holder = bankAccount.holder.toString,
          firstName = bankAccount.details.firstName,
          lastName = bankAccount.details.lastName,
          sortCode = bankAccount.details.sortCode,
          accountNumber = bankAccount.details.accountNumber,
          bankAccountInsightsResult = bankAccount.risk
        )

      case buildingSociety: journey.BuildingSocietyWithHolder =>
        BuildingSociety(
          holder = buildingSociety.holder.toString,
          firstName = buildingSociety.details.firstName,
          lastName = buildingSociety.details.lastName,
          buildingSociety = buildingSociety.details.buildingSociety.name,
          rollNumber = buildingSociety.details.rollNumber
        )
    }
}

final case class BuildingSociety(holder: String, firstName: String, lastName: String, buildingSociety: String, rollNumber: String) extends AccountDetails

object BuildingSociety {

  implicit lazy val writes: Writes[BuildingSociety] = Json.writes
}

final case class BankAccount(
                              holder: String,
                              firstName: String,
                              lastName: String,
                              sortCode: String,
                              accountNumber: String,
                              bankAccountInsightsResult: Option[BankAccountInsightsResponseModel]
                            ) extends AccountDetails

object BankAccount {

  implicit lazy val writes: Writes[BankAccount] = Json.writes
}
