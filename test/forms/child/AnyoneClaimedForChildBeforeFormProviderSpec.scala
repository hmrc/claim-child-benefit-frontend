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

import forms.behaviours.OptionFieldBehaviours
import models.{AnyoneClaimedForChildBefore, ChildName, RelationshipStatus}
import models.AnyoneClaimedForChildBefore._
import models.RelationshipStatus._
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.{stubMessages, stubMessagesApi}

class AnyoneClaimedForChildBeforeFormProviderSpec extends OptionFieldBehaviours {

  private implicit val messages: Messages = stubMessages(stubMessagesApi())

  private val childName          = ChildName("first", None, "last")
  private val relationshipStatus = RelationshipStatus.Married
  private val formProvider       = new AnyoneClaimedForChildBeforeFormProvider()
  private val form               = formProvider(childName, relationshipStatus)

  ".value" - {

    val fieldName = "value"
    val requiredKey = "anyoneClaimedForChildBefore.error.required"

    behave like optionsField[AnyoneClaimedForChildBefore](
      form,
      fieldName,
      validValues  = AnyoneClaimedForChildBefore.allValues,
      invalidError = FormError(fieldName, "error.invalid", Seq(childName.safeFirstName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(childName.safeFirstName))
    )

    "bind `Partner` when the relationship is Married or Cohabiting" in {

      Seq(Married, Cohabiting).foreach {
        relationshipStatus =>

          val data = Map("value" -> Partner.toString)
          val form = formProvider(childName, relationshipStatus)

          val result = form.bind(data).apply(fieldName)
          result.value.value mustEqual Partner.toString
          result.errors mustBe empty
      }
    }

    "not bind `Partner` when the relationship is Single, Divorced, Separated or Widowed" in {

      Seq(Single, Divorced, Separated, Widowed).foreach {
        relationshipStatus =>

          val data = Map("value" -> Partner.toString)
          val form = formProvider(childName, relationshipStatus)
          val errorKey = messages(requiredKey, childName.safeFirstName)

          val result = form.bind(data).apply(fieldName)
          result.errors must contain only FormError(fieldName, errorKey)
      }
    }
  }
}
