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

package forms.partner

import forms.behaviours.DateBehaviours
import play.api.data.FormError

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, ZoneId, ZoneOffset}

class PartnerEldestChildDateOfBirthFormProviderSpec extends DateBehaviours {

  private val fixedInstant = LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant
  private val clock = Clock.fixed(fixedInstant, ZoneId.systemDefault)

  private val minDate = LocalDate.now(clock).minusYears(20)
  private val maxDate = LocalDate.now(clock)

  private def dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  val name = "name"
  val form = new PartnerEldestChildDateOfBirthFormProvider(clock)(name)

  ".value" - {

    val validData = datesBetween(minDate, maxDate)

    behave like dateField(form, "value", validData)

    behave like dateFieldWithMax(
      form      = form,
      key       = "value",
      max       = maxDate,
      formError = FormError("value", "partnerEldestChildDateOfBirth.error.afterMaximum", Seq(maxDate.format(dateFormatter)))
    )

    behave like dateFieldWithMin(
      form      = form,
      key       = "value",
      min       = minDate,
      formError = FormError("value", "partnerEldestChildDateOfBirth.error.beforeMinimum", Seq(minDate.format(dateFormatter)))
    )

    behave like mandatoryDateField(form, "value", "partnerEldestChildDateOfBirth.error.required.all", Seq(name))
  }
}
