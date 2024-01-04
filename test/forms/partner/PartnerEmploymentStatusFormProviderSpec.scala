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

package forms.partner

import forms.behaviours.CheckboxFieldBehaviours
import models.EmploymentStatus
import play.api.data.FormError

class PartnerEmploymentStatusFormProviderSpec extends CheckboxFieldBehaviours {

  val name = "name"
  val form = new PartnerEmploymentStatusFormProvider()(name)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "partnerEmploymentStatus.error.required"

    behave like checkboxField[EmploymentStatus](
      form,
      fieldName,
      validValues  = EmploymentStatus.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid", Seq(name))
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey,
      args = name
    )

    behave like checkboxFieldWithMutuallyExclusiveAnswers[EmploymentStatus](
      form,
      fieldName,
      EmploymentStatus.activeStatuses,
      Set(EmploymentStatus.NoneOfThese),
      FormError(fieldName, "partnerEmploymentStatus.error.mutuallyExclusive", Seq(name))
    )
  }
}
