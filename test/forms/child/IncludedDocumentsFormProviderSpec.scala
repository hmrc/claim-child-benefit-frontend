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
import models.ApplicantRelationshipToChild.{AdoptingChild, BirthChild}
import models.IncludedDocuments.AdoptionCertificate
import models.{ChildName, IncludedDocuments}
import play.api.data.FormError

class IncludedDocumentsFormProviderSpec extends CheckboxFieldBehaviours {

  private val childName = ChildName("first", None, "last")
  val formProvider = new IncludedDocumentsFormProvider()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "includedDocuments.error.required"

    "must bind all valid values passed to the form provider" in {

      val allValues = IncludedDocuments.values(AdoptingChild)
      val form      = formProvider(childName, allValues)

      for {
        (value, i) <- allValues.zipWithIndex
      } yield {
        val data = Map(s"$fieldName[$i]" -> value.toString)

        val result = form.bind(data)
        result.value.value mustEqual Set(value)
        result.errors mustBe empty
      }
    }

    "must not bind otherwise valid values that aren't passed to the form provider" in {

      val limitedValues = IncludedDocuments.values(BirthChild)
      val form          = formProvider(childName, limitedValues)

      val data = Map(s"$fieldName[0]" -> AdoptionCertificate.toString)
      val result = form.bind(data)
      result.errors must contain(FormError(fieldName, requiredKey, args = Seq(childName.safeFirstName)))
    }

    "must not bind invalid values" in {

      val allValues = IncludedDocuments.values(AdoptingChild)
      val form      = formProvider(childName, allValues)

      val data = Map(s"$fieldName[0]" -> "invalid value")
      val result = form.bind(data)
      result.errors must contain(FormError(s"$fieldName[0]", "error.invalid", args = Seq(childName.safeFirstName)))
    }

    "must not bind when no options are selected" in {

      val allValues = IncludedDocuments.values(AdoptingChild)
      val form      = formProvider(childName, allValues)

      val data = Map.empty[String, String]
      val result = form.bind(data)
      result.errors must contain(FormError(fieldName, requiredKey, args = Seq(childName.safeFirstName)))
    }
  }
}

/*


    "fail to bind when the answer is invalid" in {
      val data = Map(
        s"$fieldName[0]" -> "invalid value"
      )
      form.bind(data).errors must contain(invalidError)
    }



        "fail to bind when no answers are selected" in {
      val data = Map.empty[String, String]
      val errorArgs = if (args.isEmpty) Nil else Seq(args)
      form.bind(data).errors must contain(FormError(s"$fieldName", requiredKey, errorArgs))
    }

    "fail to bind when blank answer provided" in {
      val data = Map(
        s"$fieldName[0]" -> ""
      )
      form.bind(data).errors must contain(FormError(s"$fieldName[0]", requiredKey, args))
    }
 */