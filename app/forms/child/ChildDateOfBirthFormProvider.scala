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

package forms.child

import forms.mappings.Mappings
import models.ChildName
import play.api.data.Form

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate}
import javax.inject.Inject

class ChildDateOfBirthFormProvider @Inject()(clock: Clock) extends Mappings {

  def minDate: LocalDate    = LocalDate.now(clock).minusYears(20)
  def maxDate: LocalDate    = LocalDate.now(clock)
  private def dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def apply(childName: ChildName): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = "childDateOfBirth.error.invalid",
        allRequiredKey = "childDateOfBirth.error.required.all",
        twoRequiredKey = "childDateOfBirth.error.required.two",
        requiredKey    = "childDateOfBirth.error.required",
        args           = Seq(childName.firstName)
      ).verifying(
        maxDate(maxDate, "childDateOfBirth.error.afterMaximum", maxDate.format(dateFormatter), childName.firstName),
        minDate(minDate, "childDateOfBirth.error.beforeMinimum", minDate.format(dateFormatter), childName.firstName)
      )
    )
}
