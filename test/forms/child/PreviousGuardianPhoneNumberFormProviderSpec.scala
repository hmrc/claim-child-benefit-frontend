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

package forms.child

import forms.behaviours.StringFieldBehaviours
import models.AdultName
import org.scalacheck.Gen
import play.api.data.FormError

class PreviousGuardianPhoneNumberFormProviderSpec extends StringFieldBehaviours {

  private val name = AdultName("first", None, "last")

  val requiredKey = "previousGuardianPhoneNumber.error.required"
  val invalidKey = "previousGuardianPhoneNumber.error.invalid"

  val form = new PreviousGuardianPhoneNumberFormProvider()(name)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf("07777777777", "+447777777777", "07777777777  ")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(name.firstName))
    )

    "fail to bind an invalid phone number" in {
      val result = form.bind(Map(fieldName -> "invalid"))
      result.error("value").value mustEqual FormError(fieldName, invalidKey, Seq(name.firstName))
    }
  }
}
