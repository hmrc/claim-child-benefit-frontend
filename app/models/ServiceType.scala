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

import models.Enumerable

sealed trait ServiceType {

}

object ServiceType extends Enumerable.Implicits {
  case object NewClaim extends WithName("newClaim") with ServiceType
  case object AddClaim extends WithName("addClaim") with ServiceType
  case object CheckClaim extends WithName("checkClaim") with ServiceType
  case object RestartChildBenefit extends WithName("restartChildBenefit") with ServiceType
  case object StopChildBenefit extends WithName("stopChildBenefit") with ServiceType

  val values: List[ServiceType] = List(NewClaim, AddClaim, CheckClaim, RestartChildBenefit, StopChildBenefit)
  implicit val enumerable: Enumerable[ServiceType] = Enumerable(values.map(v => v.toString -> v): _*)
}
