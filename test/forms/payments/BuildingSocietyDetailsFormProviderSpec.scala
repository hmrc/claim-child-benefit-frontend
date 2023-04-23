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

package forms.payments

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import models.BuildingSociety
import org.scalacheck.Gen
import play.api.data.FormError

class BuildingSocietyDetailsFormProviderSpec extends StringFieldBehaviours {

  val form = new BuildingSocietyDetailsFormProvider()()

  ".firstName" - {

    val fieldName = "firstName"
    val requiredKey = "buildingSocietyDetails.error.firstName.required"
    val invalidKey = "buildingSocietyDetails.error.firstName.invalid"
    val lengthKey = "buildingSocietyDetails.error.firstName.length"
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
    val requiredKey = "buildingSocietyDetails.error.lastName.required"
    val invalidKey = "buildingSocietyDetails.error.lastName.invalid"
    val lengthKey = "buildingSocietyDetails.error.lastName.length"
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

  ".buildingSociety" - {

    val fieldName = "buildingSociety"
    val requiredKey = "buildingSocietyDetails.error.buildingSociety.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(BuildingSociety.allBuildingSocieties).map(_.id)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".rollNumber" - {

    val fieldName = "rollNumber"
    val requiredKey = "buildingSocietyDetails.error.rollNumber.required"

    val validChars = Gen.oneOf(Gen.alphaNumChar, Gen.oneOf(' ', '.', '/', '-'))

    val validRollNumberGen = {
      for {
        length <- Gen.choose(1, 18)
        chars  <- Gen.listOfN(length, validChars)
      } yield chars.mkString
    }.suchThat(_.trim.nonEmpty)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validRollNumberGen
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings longer than 18 characters" in {

      val data = {
        for {
          length <- Gen.choose(19, 100)
          chars <- Gen.listOfN(length, validChars)
        } yield chars.mkString
      }.suchThat(_.trim.length > 18)

      forAll(data) {
        invalidString =>

          val result = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          val expectedError = FormError(fieldName, "buildingSocietyDetails.error.rollNumber.length", Seq(18))
          result.errors must contain only expectedError
      }
    }

    "must not bind strings with invalid characters" in {

      val result = form.bind(Map(fieldName -> "*foo*")).apply(fieldName)
      val expectedError = FormError(fieldName, "buildingSocietyDetails.error.rollNumber.invalid", Seq(Validation.rollNumberPattern.toString))
      result.errors must contain only expectedError
    }
  }
}
