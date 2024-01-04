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

import forms.behaviours.BooleanFieldBehaviours
import models.AdultName
import play.api.data.FormError

class PreviousGuardianPhoneNumberKnownFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "previousGuardianPhoneNumberKnown.error.required"
  val invalidKey = "error.boolean"

  private val previousGuardian = AdultName(None, "first", None, "last")
  val form = new PreviousGuardianPhoneNumberKnownFormProvider()(previousGuardian)

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey, Seq(previousGuardian.firstName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(previousGuardian.firstName))
    )
  }
}
