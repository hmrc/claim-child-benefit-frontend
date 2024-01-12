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

package models

import play.api.libs.json._

sealed trait AccountDetails

object AccountDetails {

  private val reads: Reads[AccountDetails] =
    BankAccountDetails.format.widen[AccountDetails] orElse BuildingSocietyDetails.format.widen[AccountDetails]

  private val writes: Writes[AccountDetails] = Writes {
    case x: BankAccountDetails => Json.toJson(x)(BankAccountDetails.format)
    case x: BuildingSocietyDetails => Json.toJson(x)(BuildingSocietyDetails.format)
  }

  implicit lazy val format: Format[AccountDetails] = Format(reads, writes)
}

final case class BankAccountDetails (
                                      firstName: String,
                                      lastName: String,
                                      sortCode: String,
                                      accountNumber: String
                                    ) extends AccountDetails {

  lazy val sortCodeTrimmed: String =
    sortCode
      .replace(" ", "")
      .replace("-", "")

  private val accountNumberLength = 8

  val accountNumberPadded: String =
    accountNumber
      .replace(" ", "")
      .replace("-", "")
      .reverse.padTo(accountNumberLength, '0').reverse
}

object BankAccountDetails {
  implicit val format: OFormat[BankAccountDetails] = Json.format[BankAccountDetails]
}

final case class BuildingSocietyDetails(
                                         firstName: String,
                                         lastName: String,
                                         buildingSociety: BuildingSociety,
                                         rollNumber: String
                                       ) extends AccountDetails

object BuildingSocietyDetails {

  implicit lazy val format: OFormat[BuildingSocietyDetails] = Json.format
}
