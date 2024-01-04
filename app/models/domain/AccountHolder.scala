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

import models.BankAccountHolder
import models.journey
import play.api.libs.json.{Json, OWrites}
import utils.StringNormaliser

sealed trait AccountHolder

object AccountHolder extends StringNormaliser {

  implicit lazy val writes: OWrites[AccountHolder] = OWrites {
    case x: OtherAccountHolder => Json.toJsObject(x)(OtherAccountHolder.writes)
    case ClaimantAccountHolder => Json.obj("accountHolderType" -> Json.toJson[AccountHolderType](AccountHolderType.Claimant))
  }

  def build(accountDetailsWithHolder: journey.AccountDetailsWithHolder): AccountHolder = accountDetailsWithHolder match {
    case journey.BankAccountWithHolder(holder, details, _) =>
      holder match {
        case BankAccountHolder.Applicant =>
          ClaimantAccountHolder

        case BankAccountHolder.JointNames =>
          OtherAccountHolder(
            accountHolderType = AccountHolderType.Joint,
            forenames = normalise(details.firstName),
            surname = normalise(details.lastName)
          )

        case BankAccountHolder.SomeoneElse =>
          OtherAccountHolder(
            accountHolderType = AccountHolderType.SomeoneElse,
            forenames = normalise(details.firstName),
            surname = normalise(details.lastName)
          )
      }

    case journey.BuildingSocietyWithHolder(holder, details) =>
      holder match {
        case BankAccountHolder.Applicant =>
          ClaimantAccountHolder

        case BankAccountHolder.JointNames =>
          OtherAccountHolder(
            accountHolderType = AccountHolderType.Joint,
            forenames = normalise(details.firstName),
            surname = normalise(details.lastName)
          )

        case BankAccountHolder.SomeoneElse =>
          OtherAccountHolder(
            accountHolderType = AccountHolderType.SomeoneElse,
            forenames = normalise(details.firstName),
            surname = normalise(details.lastName)
          )
      }
  }
}

case object ClaimantAccountHolder extends AccountHolder

final case class OtherAccountHolder(
                                     accountHolderType: AccountHolderType,
                                     forenames: String,
                                     surname: String
                                   ) extends AccountHolder

object OtherAccountHolder {

  lazy val writes: OWrites[OtherAccountHolder] = Json.writes
}

