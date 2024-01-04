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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

class ChildNorthernIrishBirthCertificateNumberFormProviderSpec extends StringFieldBehaviours {

  private val childName = ChildName("first", None, "last")

  val requiredKey = "childNorthernIrishBirthCertificateNumber.error.required"
  val invalidKey = "childNorthernIrishBirthCertificateNumber.error.invalid"
  val maxLength = 9

  val form = new ChildNorthernIrishBirthCertificateNumberFormProvider()(childName)

  ".value" - {

    val fieldName = "value"

    "must bind valid strings" in {

      val gen = Gen.listOfN(7, Gen.numChar).map(_.mkString)

      forAll(gen) {
        dataItem: String =>
          val birthCertificateNumber = s"B1$dataItem"
          val result = form.bind(Map(fieldName -> birthCertificateNumber)).apply(fieldName)
          result.value.value mustBe birthCertificateNumber
          result.errors mustBe empty
      }
    }

    "must bind valid strings that include spaces" in {

      val gen = Gen.listOfN(7, Gen.numChar).map(_.mkString(" "))

      forAll(gen) {
        dataItem: String =>
          val biarthCertificateNumber = s" B 1 $dataItem"
          val result = form.bind(Map(fieldName -> biarthCertificateNumber)).apply(fieldName)
          result.value.value mustBe biarthCertificateNumber
          result.errors mustBe empty
      }
    }

    "must not bind values with too few digits" in {

      val gen = for {
        charCount <- Gen.choose(1, 6)
        chars     <- Gen.listOfN(charCount, Gen.numChar)
      } yield s"B1${chars.mkString}"

      forAll(gen) {
        dataItem =>
          val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.northernIrelandBirthCertificateNumberPattern.toString, childName.firstName))
      }
    }

    "must not bind values with too many digits" in {

      val gen = for {
        charCount <- Gen.choose(8, 100)
        chars     <- Gen.listOfN(charCount, Gen.numChar)
      } yield s"B1${chars.mkString}"

      forAll(gen) {
        dataItem =>
          val result = form.bind(Map(fieldName -> dataItem)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.northernIrelandBirthCertificateNumberPattern.toString, childName.firstName))
      }
    }

    "must not bind vales that contain characters other than digits after the first character" in {

      forAll(arbitrary[String]) {
        value =>

          whenever (!value.forall(_.isDigit) && !value.forall(_ == ' ')) {
            val birthCertificateNumber = s"B$value"
            val result = form.bind(Map(fieldName -> birthCertificateNumber)).apply(fieldName)
            result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.northernIrelandBirthCertificateNumberPattern.toString, childName.firstName))
          }
      }
    }

    "must not bind values that do not start with B" in {

      val gen = for {
        firstChar <- arbitrary[Char].suchThat(_ != 'B')
        digits <- Gen.listOfN(7, Gen.numChar)
      } yield (Seq(firstChar, '1') ++ digits).mkString

      forAll(gen) {
        birthCertificateNumber =>
          val result = form.bind(Map(fieldName -> birthCertificateNumber)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.northernIrelandBirthCertificateNumberPattern.toString, childName.firstName))
      }
    }

    "must not bind values that do not have 1 as the second character" in {

      val gen = for {
        secondChar <- arbitrary[Char].suchThat(_ != '1')
        digits <- Gen.listOfN(7, Gen.numChar)
      } yield (Seq('B', secondChar) ++ digits).mkString

      forAll(gen) {
        birthCertificateNumber =>
          val result = form.bind(Map(fieldName -> birthCertificateNumber)).apply(fieldName)
          result.errors must contain only FormError(fieldName, invalidKey, Seq(Validation.northernIrelandBirthCertificateNumberPattern.toString, childName.firstName))
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(childName.firstName))
    )
  }
}
