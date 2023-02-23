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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class AdditionalInformationFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "additionalInformation.error.required"
  val lengthKey = "additionalInformation.error.length"

  val form = new AdditionalInformationFormProvider()()

  val fieldName = "value"

  "must bind strings up to 1000 characters" in {

    forAll(stringsWithMaxLength(1000).map(_.trim)) {
      str =>
        val result = form.bind(Map(fieldName -> str))
        result.value.value mustEqual str
    }
  }

  "must not bind strings longer than 1000 characters" in {

    forAll(stringsLongerThan(1000)) {
      str =>
        val result = form.bind(Map(fieldName -> str))
        result.errors must contain only  FormError(fieldName, lengthKey, Seq(1000))
    }
  }
}
