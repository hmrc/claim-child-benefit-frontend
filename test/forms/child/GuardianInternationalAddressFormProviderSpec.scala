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
import models.{AdultName, Country}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class GuardianInternationalAddressFormProviderSpec extends StringFieldBehaviours {

  private val name = AdultName(None, "first", None, "last")
  val form = new GuardianInternationalAddressFormProvider()(name)

  ".line1" - {

    val fieldName = "line1"
    val requiredKey = "guardianInternationalAddress.error.line1.required"
    val lengthKey = "guardianInternationalAddress.error.line1.length"
    val invalidKey = "guardianInternationalAddress.error.line1.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, name.firstName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(name.firstName))
    )
  }

  ".line2" - {

    val fieldName = "line2"
    val lengthKey = "guardianInternationalAddress.error.line2.length"
    val invalidKey = "guardianInternationalAddress.error.line2.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, name.firstName))
    )
  }

  ".town" - {

    val fieldName = "town"
    val requiredKey = "guardianInternationalAddress.error.town.required"
    val lengthKey = "guardianInternationalAddress.error.town.length"
    val invalidKey = "guardianInternationalAddress.error.town.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, name.firstName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(name.firstName))
    )
  }

  ".state" - {

    val fieldName = "state"
    val lengthKey = "guardianInternationalAddress.error.state.length"
    val invalidKey = "guardianInternationalAddress.error.state.invalid"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, name.firstName))
    )
  }

  ".postcode" - {

    val fieldName = "postcode"
    val lengthKey = "guardianInternationalAddress.error.postcode.length"
    val invalidKey = "guardianInternationalAddress.error.postcode.invalid"
    val maxLength = 8

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeInputWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.safeInputPattern.toString, name.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, name.firstName))
    )
  }

  ".country" - {

    val fieldName = "country"
    val requiredKey = "guardianInternationalAddress.error.country.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Country.internationalCountries.map(_.code))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(name.firstName))
    )

    "must not bind any values other than valid country codes" in {

      val invalidAnswers = arbitrary[String] suchThat (x => !Country.internationalCountries.map(_.code).contains(x))

      forAll(invalidAnswers) {
        answer =>
          val result = form.bind(Map("value" -> answer)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey, Seq(name.firstName))
      }
    }
  }
}
