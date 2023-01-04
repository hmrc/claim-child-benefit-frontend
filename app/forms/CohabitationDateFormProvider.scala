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

package forms

import forms.mappings.Mappings
import play.api.data.Form

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate}
import javax.inject.Inject

class CohabitationDateFormProvider @Inject()(clock: Clock) extends Mappings {

  private def maxDate       = LocalDate.now(clock)
  private def minDate       = LocalDate.now(clock).minusYears(100)
  private def dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def apply(): Form[LocalDate] =
    Form(
      "value" -> localDate(
        invalidKey     = s"cohabitationDate.error.invalid",
        allRequiredKey = s"cohabitationDate.error.required.all",
        twoRequiredKey = s"cohabitationDate.error.required.two",
        requiredKey    = s"cohabitationDate.error.required"
      ).verifying(
        maxDate(maxDate, "cohabitationDate.error.afterMaximum", maxDate.format(dateFormatter)),
        minDate(minDate, "cohabitationDate.error.beforeMinimum", minDate.format(dateFormatter))
      )
    )
}
