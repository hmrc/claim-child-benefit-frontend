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

package forms.applicant

import forms.behaviours.StringFieldBehaviours
import models.{Country, Index}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class CountryApplicantWorkedFormProviderSpec extends StringFieldBehaviours {

  val index = Index(0)
  val emptyExistingAnswers = Seq.empty[Country]
  val form = new CountryApplicantWorkedFormProvider()(index, emptyExistingAnswers)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "countryApplicantWorked.error.required"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(Country.internationalCountries.map(_.code))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind any values other than valid country codes" in {

      val invalidAnswers = arbitrary[String] suchThat (x => !Country.internationalCountries.map(_.code).contains(x))

      forAll(invalidAnswers) {
        answer =>
          val result = form.bind(Map("value" -> answer)).apply(fieldName)
          result.errors must contain only FormError(fieldName, requiredKey)
      }
    }

    "must fail to bind when given a duplicate value" in {
      val existingAnswers = Seq(Country.internationalCountries.head, Country.internationalCountries.tail.head)
      val answer = Country.internationalCountries.tail.head
      val form = new CountryApplicantWorkedFormProvider()(index, existingAnswers)

      val result = form.bind(Map(fieldName -> answer.code)).apply(fieldName)
      result.errors must contain only FormError(fieldName, "countryApplicantWorked.error.duplicate")
    }
  }
}
