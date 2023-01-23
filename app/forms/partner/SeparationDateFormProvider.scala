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

package forms.partner

import forms.mappings.Mappings
import play.api.data.Form

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate}
import javax.inject.Inject

class SeparationDateFormProvider @Inject()(clock: Clock) extends Mappings {

  private def maxDate       = LocalDate.now(clock)
  private def minDate       = LocalDate.now(clock).minusYears(100)
  private def dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = s"separationDate.error.invalid",
        allRequiredKey = s"separationDate.error.required.all",
        twoRequiredKey = s"separationDate.error.required.two",
        requiredKey    = s"separationDate.error.required"
      ).verifying(
        maxDate(maxDate, "separationDate.error.afterMaximum", maxDate.format(dateFormatter)),
        minDate(minDate, "separationDate.error.beforeMinimum", minDate.format(dateFormatter))
      )
    )
}
