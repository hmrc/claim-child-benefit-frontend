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

import play.api.libs.json.{JsString, Writes}

trait CountryOfRegistration

object CountryOfRegistration {

  case object EnglandWales extends CountryOfRegistration
  case object Scotland extends CountryOfRegistration
  case object NorthernIreland extends CountryOfRegistration
  case object Abroad extends CountryOfRegistration

  implicit lazy val writes: Writes[CountryOfRegistration] = Writes {
    case EnglandWales => JsString("ENGLAND_WALES")
    case Scotland => JsString("SCOTLAND")
    case NorthernIreland => JsString("NORTHERN_IRELAND")
    case Abroad => JsString("ABROAD")
  }

  def build(country: models.ChildBirthRegistrationCountry): CountryOfRegistration =
    country match {
      case models.ChildBirthRegistrationCountry.England => EnglandWales
      case models.ChildBirthRegistrationCountry.Wales => EnglandWales
      case models.ChildBirthRegistrationCountry.Scotland => Scotland
      case models.ChildBirthRegistrationCountry.NorthernIreland => NorthernIreland
      case _ => Abroad
    }
}
