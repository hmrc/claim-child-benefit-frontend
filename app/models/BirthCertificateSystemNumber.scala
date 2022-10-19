/*
 * Copyright 2022 HM Revenue & Customs
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

final case class BirthCertificateSystemNumber(value: String) extends BirthCertificateNumber {

  override val brmsFormat: String = value
  override val display: String    = value
}

object BirthCertificateSystemNumber {

  implicit lazy val reads: Reads[BirthCertificateSystemNumber] = Reads {
    case JsString(value) =>
      JsSuccess(BirthCertificateSystemNumber(value))

    case _ =>
      JsError("Unable to read value as a birth certificate system number")
  }

  implicit lazy val writes: Writes[BirthCertificateSystemNumber] = Writes {
    value => JsString(value.display)
  }
}
