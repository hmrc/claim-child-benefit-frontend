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

import forms.Validation
import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

import java.time.{Clock, Instant, LocalDate, ZoneId}

class ChildScottishBirthCertificateDetailsFormProviderSpec extends StringFieldBehaviours {

  private val clock = Clock.systemUTC
  private val form = new ChildScottishBirthCertificateDetailsFormProvider(clock)()

  ".district" - {

    val fieldName = "district"
    val requiredKey = "childScottishBirthCertificateDetails.error.district.required"
    val invalidKey = "childScottishBirthCertificateDetails.error.district.invalid"

    val gen = Gen.listOfN(3, Gen.numChar).map(_.mkString)

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

    "must not bind fewer than three digits" in {

      val gen = Gen.listOfN(2, Gen.numChar).map(_.mkString)

      forAll(gen) {
        input =>
          val result = form.bind(Map(fieldName -> input)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.districtPattern.toString))
      }
    }

    "must not bind more than three digits" in {

      val gen = for {
        number <- Gen.choose(4, 100)
        chars  <- Gen.listOfN(number, Gen.numChar)
      } yield chars.mkString

      forAll(gen) {
        input =>
          val result = form.bind(Map(fieldName -> input)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.districtPattern.toString))
      }
    }

    "must not bind strings that contain non-numerics" in {

      val gen = arbitrary[String].suchThat(_.trim.nonEmpty).suchThat(x => !x.forall(_.isDigit))

      forAll(gen) {
        inputWithNonNumeric =>
          val result = form.bind(Map(fieldName -> inputWithNonNumeric)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.districtPattern.toString))
      }
    }
  }

  ".year" - {

    val fieldName       = "year"
    val requiredKey     = "childScottishBirthCertificateDetails.error.year.required"
    val belowMinimumKey = "childScottishBirthCertificateDetails.error.year.belowMinimum"
    val aboveMaximumKey = "childScottishBirthCertificateDetails.error.year.aboveMaximum"
    val invalidKey      = "childScottishBirthCertificateDetails.error.year.invalid"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind values from 20 years ago to the current year" in {

      val currentYear = LocalDate.now(clock).getYear
      val earliestYear = currentYear - 20

      forAll(Gen.choose(earliestYear, currentYear).map(_.toString)) {
        validYear =>
          val result = form.bind(Map(fieldName -> validYear)).apply(fieldName)
          result.value.value mustEqual validYear
          result.errors mustBe empty
      }
    }

    "must not bind values below the minimum" in {

      val minValidYear = LocalDate.now(clock).getYear - 20

      forAll(Gen.choose(0, minValidYear - 1).map(_.toString)) {
        invalidYear =>
          val result = form.bind(Map(fieldName -> invalidYear)).apply(fieldName)
          result.errors must contain only FormError(fieldName, belowMinimumKey, Seq(minValidYear))

      }
    }

    "must not bind values above the maximum" in {

      val maxValidYear = LocalDate.now(clock).getYear

      forAll(Gen.choose(maxValidYear + 1, maxValidYear + 1000).map(_.toString)) {
        invalidYear =>
          val result = form.bind(Map(fieldName -> invalidYear)).apply(fieldName)
          result.errors must contain only FormError(fieldName, aboveMaximumKey, Seq(maxValidYear))

      }
    }

    "must not bind values that contain non-numerics" in {

      val gen = arbitrary[String].suchThat(_.trim.nonEmpty).suchThat(x => !x.forall(_.isDigit))

      forAll(gen) {
        inputWithNonNumeric =>
          val result = form.bind(Map(fieldName -> inputWithNonNumeric)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey)
      }
    }
  }

  ".entryNumber" - {

    val fieldName = "entryNumber"
    val requiredKey = "childScottishBirthCertificateDetails.error.entryNumber.required"
    val invalidKey = "childScottishBirthCertificateDetails.error.entryNumber.invalid"

    val validData = for {
      number <- Gen.choose(1, 3)
      digits <- Gen.listOfN(number, Gen.numChar)
    } yield digits.mkString

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validData
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind more than three digits" in {

      val gen = for {
        number <- Gen.choose(4, 100)
        digits <- Gen.listOfN(number, Gen.numChar)
      } yield digits.mkString

      forAll(gen) {
        input =>
          val result = form.bind(Map(fieldName -> input)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.entryNumberPattern.toString))
      }
    }

    "must not bind strings that contain non-numerics" in {

      val gen = arbitrary[String].suchThat(_.trim.nonEmpty).suchThat(x => !x.forall(_.isDigit))

      forAll(gen) {
        inputWithNonNumeric =>
          val result = form.bind(Map(fieldName -> inputWithNonNumeric)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.entryNumberPattern.toString))
      }
    }
  }
}
