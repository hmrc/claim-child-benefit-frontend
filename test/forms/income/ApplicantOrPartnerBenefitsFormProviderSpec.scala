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

package forms.income

import forms.behaviours.CheckboxFieldBehaviours
import models.Benefits
import play.api.data.FormError

class ApplicantOrPartnerBenefitsFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new ApplicantOrPartnerBenefitsFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "applicantOrPartnerBenefits.error.required"

    behave like checkboxField[Benefits](
      form,
      fieldName,
      validValues  = Benefits.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )

    behave like checkboxFieldWithMutuallyExclusiveAnswers[Benefits](
      form,
      fieldName,
      Benefits.qualifyingBenefits,
      Set(Benefits.NoneOfTheAbove),
      FormError(fieldName, "applicantOrPartnerBenefits.error.mutuallyExclusive")
    )
  }
}
