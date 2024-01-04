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

package forms.applicant

import forms.behaviours.DateBehaviours
import play.api.data.FormError

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, ZoneId}

class ApplicantArrivedInUkFormProviderSpec extends DateBehaviours {

  private val fixedInstant = LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant
  private val clock = Clock.fixed(fixedInstant, ZoneId.systemDefault)

  private val form = new ApplicantArrivedInUkFormProvider(clock)()

  private val maxDate = LocalDate.now(clock)

  private def dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  ".value" - {

    val validData = datesBetween(maxDate.minusYears(20), maxDate)

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMax(
      form      = form,
      key       = "value",
      max       = maxDate,
      formError = FormError("value", "applicantArrivedInUk.error.afterMaximum", Seq(maxDate.format(dateFormatter)))
    )

    behave like mandatoryDateField(form, "value", "applicantArrivedInUk.error.required.all")
  }
}
