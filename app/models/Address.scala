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

import play.api.i18n.Messages
import play.api.libs.json._

trait Address extends Product with Serializable {
  val line1: String
  def lines(implicit messages: Messages): Seq[String]

  protected val localAuthorityKeywords: Seq[String] = Seq("COUNCIL", "CIVIC", "AUTHORITY", "DISTRICT", "METROPOLITAN", "BOROUGH")

  def possibleLocalAuthorityAddress(implicit messages: Messages): Boolean
}

object Address {
  def reads: Reads[Address] =
    UkAddress.format.widen[Address] orElse
      InternationalAddress.format.widen[Address] orElse
      NPSAddress.format.widen[Address]

  def writes: Writes[Address] = Writes {
    case u: UkAddress            => Json.toJson(u)(UkAddress.format)
    case i: InternationalAddress => Json.toJson(i)(InternationalAddress.format)
    case n: NPSAddress           => Json.toJson(n)(NPSAddress.format)
  }

  implicit def format: Format[Address] = Format(reads, writes)
}

final case class UkAddress(
                      line1: String,
                      line2: Option[String],
                      townOrCity: String,
                      county: Option[String],
                      postcode: String
                    ) extends Address {

  def lines(implicit messages: Messages): Seq[String] =
    Seq(
      Some(line1),
      line2,
      Some(townOrCity),
      county,
      Some(postcode)
    ).flatten

  override def possibleLocalAuthorityAddress(implicit messages: Messages): Boolean =
    lines
      .mkString(" ")
      .replaceAll("\\s", " ")
      .split(" ")
      .map(_.toUpperCase)
      .intersect(localAuthorityKeywords)
      .nonEmpty
}

object UkAddress {

  implicit val format: OFormat[UkAddress] = Json.format
}

final case class InternationalAddress (
                                  line1: String,
                                  line2: Option[String],
                                  townOrCity: String,
                                  stateOrRegion: Option[String],
                                  postcode: Option[String],
                                  country: Country
                                ) extends Address {

  def lines(implicit messages: Messages): Seq[String] =
    Seq(
      Some(line1),
      line2,
      Some(townOrCity),
      stateOrRegion,
      postcode,
      Option(country.message)
    ).flatten

  override def possibleLocalAuthorityAddress(implicit messages: Messages): Boolean = false
}

object InternationalAddress {

  implicit val format: OFormat[InternationalAddress] = Json.format
}

