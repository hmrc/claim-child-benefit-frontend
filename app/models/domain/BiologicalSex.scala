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

import play.api.libs.json.{JsString, Writes}

sealed trait BiologicalSex

object BiologicalSex {

  case object Female extends BiologicalSex
  case object Male extends BiologicalSex
  case object Unspecified extends BiologicalSex

  implicit lazy val writes: Writes[BiologicalSex] = Writes {
    case Female => JsString("FEMALE")
    case Male => JsString("MALE")
    case Unspecified => JsString("UNSPECIFIED")
  }

  def build(biologicalSex: models.ChildBiologicalSex): BiologicalSex = {
    biologicalSex match {
      case models.ChildBiologicalSex.Female => Female
      case models.ChildBiologicalSex.Male => Male
      case models.ChildBiologicalSex.Unspecified => Unspecified
    }
  }
}
