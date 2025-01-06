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

package forms.payments

import forms.Validation
import forms.behaviours.{BooleanFieldBehaviours, StringFieldBehaviours}
import org.scalacheck.Gen
import play.api.data.FormError

class BankAccountDetailsFormProviderSpec extends StringFieldBehaviours with BooleanFieldBehaviours {

  val form = new BankAccountDetailsFormProvider()()

  ".firstName" - {

    val fieldName = "firstName"
    val requiredKey = "bankAccountDetails.error.firstName.required"
    val invalidKey = "bankAccountDetails.error.firstName.invalid"
    val lengthKey = "bankAccountDetails.error.firstName.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeNameInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.nameInputPattern.toString))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".lastName" - {

    val fieldName = "lastName"
    val requiredKey = "bankAccountDetails.error.lastName.required"
    val invalidKey = "bankAccountDetails.error.lastName.invalid"
    val lengthKey = "bankAccountDetails.error.lastName.length"
    val maxLength = 35

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      safeNameInputsWithMaxLength(maxLength)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form,
      fieldName,
      unsafeInputsWithMaxLength(maxLength),
      FormError(fieldName, invalidKey, Seq(Validation.nameInputPattern.toString))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".accountNumber" - {

    val fieldName = "accountNumber"
    val requiredKey = "bankAccountDetails.error.accountNumber.required"
    val minLength = 6
    val maxLength = 8

    val validAccountNumberGen = for {
      length <- Gen.choose(minLength, maxLength)
      digits <- Gen.listOfN(length, Gen.numChar)
    } yield digits.mkString

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validAccountNumberGen
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind account number in format with any number of spaces nn   nn    nn format" in {
      val result = form.bind(Map(fieldName -> "12   34   56")).apply(fieldName)
      result.value.value `mustBe` "12   34   56"
    }

    "must not bind strings with characters" in {
      val result = form.bind(Map(fieldName -> "abcdef")).apply(fieldName)
      val expectedError = FormError(
        fieldName,
        "bankAccountDetails.error.accountNumber.invalid",
        Seq(Validation.accountNumberPattern.toString)
      )
      result.errors `must` contain `only` expectedError
    }

    "must not bind strings with less than 6 digit" in {
      val result = form.bind(Map(fieldName -> "12 34   5")).apply(fieldName)
      val expectedError = FormError(
        fieldName,
        "bankAccountDetails.error.accountNumber.invalid",
        Seq(Validation.accountNumberPattern.toString)
      )
      result.errors `must` contain `only` expectedError
    }

    "must not bind strings with more than 8 digit" in {
      val result = form.bind(Map(fieldName -> "12 34 56 789")).apply(fieldName)
      val expectedError = FormError(
        fieldName,
        "bankAccountDetails.error.accountNumber.invalid",
        Seq(Validation.accountNumberPattern.toString)
      )
      result.errors `must` contain `only` expectedError
    }
  }

  ".sortCode" - {

    val fieldName = "sortCode"
    val requiredKey = "bankAccountDetails.error.sortCode.required"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind sort codes in nnnnnn format" in {
      val result = form.bind(Map(fieldName -> "123456")).apply(fieldName)
      result.value.value `mustBe` "123456"
    }

    "must bind sort codes in nn-nn-nn format" in {
      val result = form.bind(Map(fieldName -> "12-34-56")).apply(fieldName)
      result.value.value `mustBe` "12-34-56"
    }

    "bind sort codes in nn nn nn format" in {
      val result = form.bind(Map(fieldName -> "12 34 56")).apply(fieldName)
      result.value.value `mustBe` "12 34 56"
    }

    "must bind sort codes in nn   nn    nn format" in {
      val result = form.bind(Map(fieldName -> "12   34   56")).apply(fieldName)
      result.value.value `mustBe` "12   34   56"
    }

    "must not bind sort codes with characters" in {
      val result = form.bind(Map(fieldName -> "abcdef")).apply(fieldName)
      val expectedError =
        FormError(fieldName, "bankAccountDetails.error.sortCode.invalid", Seq(Validation.sortCodePattern.toString))
      result.errors `must` contain `only` expectedError
    }

    "must not bind sort codes with less than 6 digit" in {
      val result = form.bind(Map(fieldName -> "12   34  5")).apply(fieldName)
      val expectedError =
        FormError(fieldName, "bankAccountDetails.error.sortCode.invalid", Seq(Validation.sortCodePattern.toString))
      result.errors `must` contain `only` expectedError
    }

    "must not bind sort codes with more than 6 digit" in {
      val result = form.bind(Map(fieldName -> "12   34  5678")).apply(fieldName)
      val expectedError =
        FormError(fieldName, "bankAccountDetails.error.sortCode.invalid", Seq(Validation.sortCodePattern.toString))
      result.errors `must` contain `only` expectedError
    }
  }

  ".softError" - {

    val invalidKey = "error.boolean"
    val fieldName = "softError"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )
  }
}
