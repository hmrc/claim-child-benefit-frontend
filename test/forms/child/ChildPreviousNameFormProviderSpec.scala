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

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import models.ChildName
import play.api.data.FormError

class ChildPreviousNameFormProviderSpec extends StringFieldBehaviours {

  private val childName = ChildName("first", None, "last")
  private val form = new ChildPreviousNameFormProvider()(childName)

  ".firstName" - {

    val fieldName = "firstName"
    val requiredKey = "childPreviousName.error.firstName.required"
    val lengthKey = "childPreviousName.error.firstName.length"
    val invalidKey = "childPreviousName.error.firstName.invalid"
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
      FormError(fieldName, invalidKey, Seq(Validation.nameInputPattern.toString, childName.firstName))
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

  ".middleNames" - {

    val fieldName = "middleNames"
    val lengthKey = "childPreviousName.error.middleNames.length"
    val invalidKey = "childPreviousName.error.middleNames.invalid"
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
      FormError(fieldName, invalidKey, Seq(Validation.nameInputPattern.toString, childName.firstName))
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength, "first"))
    )
  }

  ".lastName" - {

    val fieldName = "lastName"
    val requiredKey = "childPreviousName.error.lastName.required"
    val lengthKey = "childPreviousName.error.lastName.length"
    val invalidKey = "childPreviousName.error.lastName.invalid"
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
      FormError(fieldName, invalidKey, Seq(Validation.nameInputPattern.toString, childName.firstName))
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
