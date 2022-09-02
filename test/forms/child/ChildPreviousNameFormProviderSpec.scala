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
import models.ChildName
import play.api.data.FormError

class ChildPreviousNameFormProviderSpec extends StringFieldBehaviours {

  private val childName = ChildName("first", None, "last")
  private val form = new ChildPreviousNameFormProvider()(childName)

  ".firstName" - {

    val fieldName = "firstName"
    val requiredKey = "childPreviousName.error.firstName.required"
    val lengthKey = "childPreviousName.error.firstName.length"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
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
