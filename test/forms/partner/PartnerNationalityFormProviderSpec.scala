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

package forms.partner

import forms.behaviours.StringFieldBehaviours
import models.Nationality
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class PartnerNationalityFormProviderSpec extends StringFieldBehaviours {

  val name = "name"
  val requiredKey = "usualCountryOfResidence.error.required"
  val invalidKey = "usualCountryOfResidence.error.invalid"

  val form = new PartnerNationalityFormProvider()(name)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "partnerNationality.error.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Nationality.allNationalities.map(_.name))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(name))
    )

    "must not bind any values other than valid nationalities" in {

      val invalidAnswers = arbitrary[String] suchThat (x => x.trim.nonEmpty && !Nationality.allNationalities.map(_.name).contains(x))

      forAll(invalidAnswers) {
        answer =>
          val result = form.bind(Map("value" -> answer)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey)
      }
    }
  }
}
