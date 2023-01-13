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

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

sealed trait AdditionalInformation {
  val nonEmpty: Boolean
}

object AdditionalInformation {

  case object NoInformation extends WithName("noInformation") with AdditionalInformation {
    override val nonEmpty: Boolean = false
  }

  final case class Information(value: String) extends AdditionalInformation {

    override val nonEmpty: Boolean = true
    override def toString: String = value
  }

  implicit lazy val writes: Writes[AdditionalInformation] = Writes {
    case i: Information => JsString(i.value)
    case NoInformation  => JsString(NoInformation.toString)
  }

  implicit lazy val reads: Reads[AdditionalInformation] = Reads {
    case JsString(NoInformation.toString) => JsSuccess(NoInformation)
    case JsString(str)                    => JsSuccess(Information(str))
    case _                                => JsError("invalid")
  }
}
