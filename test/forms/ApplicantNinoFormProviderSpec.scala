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

package forms

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class ApplicantNinoFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "applicantNino.error.required"

  val form = new ApplicantNinoFormProvider()()

  ".value" - {

    val fieldName = "value"

    val ninoGen = arbitraryNino.arbitrary.map(_.value)
    val ninoWithSpacesGen = for {
      spaceBefore <- Gen.stringOf(Gen.const(' '))
      spaceAfter  <- Gen.stringOf(Gen.const(' '))
      nino        <- ninoGen
    } yield s"$spaceBefore$nino$spaceAfter"
    val gen = Gen.oneOf(ninoGen, ninoWithSpacesGen)

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      gen
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind values in the wrong format" in {
      val result = form.bind(Map(fieldName -> "GB123456A")).apply(fieldName)
      result.errors.head mustBe FormError(fieldName, "applicantNino.error.invalid")
    }
  }
}
