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

package forms.child

import forms.mappings.Mappings
import models.{AdultName, ChildName}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class GuardianNameFormProvider @Inject() extends Mappings {

  def apply(childName: ChildName): Form[AdultName] = Form(
    mapping(
      "firstName" -> text("guardianName.error.firstName.required", args = Seq(childName.firstName))
        .verifying(maxLength(100, "guardianName.error.firstName.length", childName.firstName)),
      "middleNames" -> optional(text("guardianName.error.middleNames.required", args = Seq(childName.firstName))
        .verifying(maxLength(100, "guardianName.error.middleNames.length", childName.firstName))),
      "lastName" -> text("guardianName.error.lastName.required", args = Seq(childName.firstName))
        .verifying(maxLength(100, "guardianName.error.lastName.length", childName.firstName))
    )(AdultName.apply(None, _, _, _))(name => Some(name.firstName, name.middleNames, name.lastName))
  )
}
