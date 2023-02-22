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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class NPSAddress(
                             line1: String,
                             line2: Option[String],
                             line3: Option[String],
                             line4: Option[String],
                             line5: Option[String],
                             postcode: Option[String],
                             country: Option[Country]
                           ) extends Address {

  lazy val isUkAddress: Boolean = country.exists(_.code == "GB")

  val lines: Seq[String] =
    Seq(
      Some(line1),
      line2,
      line3,
      line4,
      line5,
      postcode,
      country.map(_.name)
    ).flatten
}

object NPSAddress {

  implicit lazy val format: OFormat[NPSAddress] = Json.format

}

final case class DesignatoryDetails(
                                     realName: Option[AdultName],
                                     knownAsName: Option[AdultName],
                                     residentialAddress: Option[NPSAddress],
                                     correspondenceAddress: Option[NPSAddress],
                                     dateOfBirth: LocalDate
                                   ) {

  lazy val preferredName: Option[AdultName] = knownAsName orElse realName
}

object DesignatoryDetails {
  implicit lazy val format: OFormat[DesignatoryDetails] = Json.format
}
