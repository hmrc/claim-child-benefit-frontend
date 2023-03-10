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

package models

import play.api.libs.json.{Json, OWrites}

final case class VerifyBankDetailsRequest(account: Account, subject: Subject)

object VerifyBankDetailsRequest {

  def from(bankAccountDetails: BankAccountDetails): VerifyBankDetailsRequest =
    VerifyBankDetailsRequest(
      Account(
        sortCode      = bankAccountDetails.sortCodeTrimmed,
        accountNumber = bankAccountDetails.accountNumberPadded
      ),
      Subject(
        firstName = bankAccountDetails.firstName,
        lastName = bankAccountDetails.lastName
      )
    )

  implicit val writes: OWrites[VerifyBankDetailsRequest] = Json.writes
}

final case class Account(sortCode: String, accountNumber: String)

object Account {
  implicit val writes: OWrites[Account] = Json.writes
}

final case class Subject(firstName: String, lastName: String)

object Subject {
  implicit val writes: OWrites[Subject] = Json.writes
}
