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

package forms.child

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import models.AdultName
import play.api.data.FormError

class PreviousGuardianUkAddressFormProviderSpec extends StringFieldBehaviours {

  private val name = AdultName(None, "first", None, "last")
  private val form = new PreviousGuardianUkAddressFormProvider()(name)

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "previousGuardianUkAddress.error.line1.required"
    val lengthKey = "previousGuardianUkAddress.error.line1.length"
    val invalidKey = "previousGuardianUkAddress.error.line1.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeAddressInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.addressInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, "first"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq("first"))
    )
  }

  ".line2" - {

    val fieldName = "line2"
    val lengthKey = "previousGuardianUkAddress.error.line2.length"
    val invalidKey = "previousGuardianUkAddress.error.line2.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeAddressInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.addressInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, "first"))
    )
  }

  ".town" - {

    val fieldName = "town"
    val requiredKey = "previousGuardianUkAddress.error.town.required"
    val lengthKey = "previousGuardianUkAddress.error.town.length"
    val invalidKey = "previousGuardianUkAddress.error.town.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeAddressInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.addressInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, "first"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq("first"))
    )
  }

  ".county" - {

    val fieldName = "county"
    val lengthKey = "previousGuardianUkAddress.error.county.length"
    val invalidKey = "previousGuardianUkAddress.error.county.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeAddressInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.addressInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, "first"))
    )
  }

  ".postcode" - {

    val fieldName = "postcode"
    val requiredKey = "previousGuardianUkAddress.error.postcode.required"
    val lengthKey = "previousGuardianUkAddress.error.postcode.length"
    val invalidKey = "previousGuardianUkAddress.error.postcode.invalid"
    val maxLength = 8

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      ukPostcode
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.ukPostcodePattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, "first"))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq("first"))
    )
  }

}
