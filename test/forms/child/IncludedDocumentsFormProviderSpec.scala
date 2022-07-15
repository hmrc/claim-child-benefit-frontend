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

import forms.behaviours.CheckboxFieldBehaviours
import models.ApplicantRelationshipToChild._
import models.IncludedDocuments.{AdoptionCertificate, BirthCertificate, OtherDocument}
import models.{ChildName, IncludedDocuments}
import org.scalacheck.Gen
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers._

class IncludedDocumentsFormProviderSpec extends CheckboxFieldBehaviours {

  private implicit val messages: Messages = stubMessages(stubMessagesApi())
  private val childName = ChildName("first", None, "last")
  val formProvider = new IncludedDocumentsFormProvider()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "includedDocuments.error.required"

    "must bind all standard values" in {

      val allValues     = IncludedDocuments.standardDocuments(AdoptedChild)
      val form          = formProvider(childName, AdoptedChild)

      for {
        (value, i) <- allValues.zipWithIndex
      } yield {
        val data = Map(s"$fieldName[$i]" -> value.toString)

        val result = form.bind(data)
        result.value.value mustEqual Set(value)
        result.errors mustBe empty
      }
    }

    "must not bind Adoption Certificate when the relationship to the child is not `AdoptedChld`" in {

      val relationship = Gen.oneOf(BirthChild, StepChild, AdoptingChild, Other).sample.value
      val form          = formProvider(childName, relationship)

      val data = Map(s"$fieldName[0]" -> AdoptionCertificate.toString)
      val result = form.bind(data)
      result.errors must contain(FormError(s"$fieldName[0]", requiredKey))
    }

    "must not bind invalid values" in {

      val form          = formProvider(childName, AdoptingChild)

      val data = Map(s"$fieldName[0]" -> "invalid value")
      val result = form.bind(data)
      result.errors must contain(FormError(s"$fieldName[0]", requiredKey))
    }

    "must not bind when no options are selected" in {

      val form          = formProvider(childName, AdoptingChild)

      val data = Map.empty[String, String]
      val result = form.bind(data)
      result.errors must contain(FormError(fieldName, requiredKey))
    }
  }

  "must bind `other documents` when provided" in {

    val form = formProvider(childName, BirthChild)

    val data = Map(
      "value[0]"      -> "otherDocument",
      "value[1]"      -> BirthCertificate.toString,
      "otherDocument" -> "foo"
    )

    val result = form.bind(data)
    result.value.value mustEqual Set[IncludedDocuments](BirthCertificate, OtherDocument("foo"))
    result.errors mustBe empty
  }

  "must not bind if `other document` is selected but no value is provided for it" in {

    val form = formProvider(childName, BirthChild)

    val data = Map(
      "value[0]" -> "otherDocument",
      "value[1]" -> BirthCertificate.toString
    )

    val result = form.bind(data)
    result.errors must contain(FormError("otherDocument", "includedDocuments.error.otherDocument.required", Seq(childName.safeFirstName)))
  }

  "must not bind if `other document` is selected but an empty value is provided for it" in {

    val form = formProvider(childName, BirthChild)

    val data = Map(
      "value[0]"      -> "otherDocument",
      "value[1]"      -> BirthCertificate.toString,
      "otherDocument" -> ""
    )

    val result = form.bind(data)
    result.errors must contain(FormError("otherDocument", "includedDocuments.error.otherDocument.required", Seq(childName.safeFirstName)))
  }

  "must not bind an `other document` if the string is present but that option is not selected" in {

    val form = formProvider(childName, BirthChild)

    val data = Map(
      "value[0]"      -> BirthCertificate.toString,
      "otherDocument" -> "foo"
    )

    val result = form.bind(data)
    result.value.value mustEqual Set[IncludedDocuments](BirthCertificate)
    result.errors mustBe empty
  }
}
