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

import models.Country
import play.api.libs.json.{Json, Writes}

sealed trait Address

object Address {

  implicit val writes: Writes[Address] = Writes {
    case u: UkAddress => Json.toJson(u)(UkAddress.writes)
    case i: InternationalAddress => Json.toJson(i)(InternationalAddress.writes)
  }

  def build(address: models.Address): Address = {
    address match {
      case u: models.UkAddress => UkAddress.build(u)
      case i: models.InternationalAddress => InternationalAddress.build(i)
    }
  }
}

final case class UkAddress(line1: String, line2: Option[String], townOrCity: String, county: Option[String], postcode: String) extends Address

object UkAddress {

  implicit lazy val writes: Writes[UkAddress] = Json.writes

  def build(address: models.UkAddress): UkAddress =
    UkAddress(
      line1 = address.line1,
      line2 = address.line2,
      townOrCity = address.townOrCity,
      county = address.county,
      postcode = address.postcode
    )
}

final case class InternationalAddress(line1: String, line2: Option[String], townOrCity: String, stateOrRegion: Option[String], postcode: Option[String], country: Country) extends Address

object InternationalAddress {

  implicit lazy val writes: Writes[InternationalAddress] = Json.writes

  def build(address: models.InternationalAddress): InternationalAddress =
    InternationalAddress(
      line1 = address.line1,
      line2 = address.line2,
      townOrCity = address.townOrCity,
      stateOrRegion = address.stateOrRegion,
      postcode = address.postcode,
      country = address.country
    )
}
