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

package forms.partner

import forms.behaviours.StringFieldBehaviours
import models.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class CountryPartnerReceivedBenefitsFormProviderSpec extends StringFieldBehaviours {

  val name = "name"
  val requiredKey = "countryPartnerReceivedBenefits.error.required"

  val form = new CountryPartnerReceivedBenefitsFormProvider()(name)

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Country.internationalCountries.map(_.code))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(name))
    )

    "must not bind any values other than valid international countries" in {

      val invalidAnswers = arbitrary[String].suchThat(x => x.trim.nonEmpty && !Country.internationalCountries.map(_.code).contains(x))

      forAll(invalidAnswers) {
        answer =>
          val result = form.bind(Map("value" -> answer)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey)
      }
    }
  }
}
