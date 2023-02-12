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

import models.NationalityGroup
import play.api.libs.json.{JsString, Writes}

sealed trait Nationality

case object Nationality {

  case object UkCta extends Nationality
  case object Eea extends Nationality
  case object NonEea extends Nationality

  implicit lazy val writes: Writes[Nationality] = Writes {
    case UkCta  => JsString("UK_OR_CTA")
    case Eea    => JsString("EEA")
    case NonEea => JsString("NON_EEA")
  }

  def fromNationalityGroup(group: NationalityGroup): Nationality = group match {
    case NationalityGroup.UkCta  => UkCta
    case NationalityGroup.Eea    => Eea
    case NationalityGroup.NonEea => NonEea
  }
}
