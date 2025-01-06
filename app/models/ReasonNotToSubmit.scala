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

sealed trait ReasonNotToSubmit

object ReasonNotToSubmit extends Enumerable.Implicits {

  case object UserUnauthenticated extends WithName("userUnauthenticated") with ReasonNotToSubmit
  case object ChildOverSixMonths extends WithName("childOverSixMonthsOld") with ReasonNotToSubmit
  case object DocumentsRequired extends WithName("documentsRequired") with ReasonNotToSubmit
  case object DesignatoryDetailsChanged extends WithName("designatoryDetailsChanged") with ReasonNotToSubmit
  case object PartnerNinoMissing extends WithName("partnerNinoMissing") with ReasonNotToSubmit

  val values: Seq[ReasonNotToSubmit] = Seq(
    UserUnauthenticated,
    ChildOverSixMonths,
    DocumentsRequired,
    DesignatoryDetailsChanged,
    PartnerNinoMissing
  )

  implicit val enumerable: Enumerable[ReasonNotToSubmit] =
    Enumerable(values.map(v => v.toString -> v)*)
}
