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

import play.api.libs.json.{Json, OWrites}

sealed trait PaymentDetails

object PaymentDetails {

  implicit lazy val writes: OWrites[PaymentDetails] = OWrites {
    case x: BankDetails            => Json.toJsObject(x)(BankDetails.writes)
    case x: BuildingSocietyDetails => Json.toJsObject(x)(BuildingSocietyDetails.writes)
  }
}

final case class BankDetails(
                              accountHolder: AccountHolder,
                              bankAccount: BankAccount
                            ) extends PaymentDetails

object BankDetails {

  lazy val writes: OWrites[BankDetails] = Json.writes
}

final case class BuildingSocietyDetails(
                                         accountHolder: AccountHolder,
                                         buildingSocietyDetails: BuildingSocietyAccount
                                       ) extends PaymentDetails

object BuildingSocietyDetails {

  lazy val writes: OWrites[BuildingSocietyDetails] = Json.writes
}
