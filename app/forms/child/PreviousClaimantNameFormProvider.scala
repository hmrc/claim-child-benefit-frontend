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

import forms.Validation
import forms.mappings.Mappings
import models.{AdultName, ChildName}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class PreviousClaimantNameFormProvider @Inject() extends Mappings {

  def apply(childName: ChildName): Form[AdultName] = Form(
    mapping(
      "firstName" -> text("previousClaimantName.error.firstName.required", args = Seq(childName.firstName))
        .verifying(firstError(
          maxLength(35, "previousClaimantName.error.firstName.length", childName.firstName),
          regexp(Validation.nameInputPattern, "previousClaimantName.error.firstName.invalid", childName.firstName)
        )),
      "middleNames" -> optional(text("previousClaimantName.error.middleNames.required", args = Seq(childName.firstName))
        .verifying(firstError(
          maxLength(35, "previousClaimantName.error.middleNames.length", childName.firstName),
          regexp(Validation.nameInputPattern, "previousClaimantName.error.middleNames.invalid", childName.firstName)
        ))),
      "lastName" -> text("previousClaimantName.error.lastName.required", args = Seq(childName.firstName))
        .verifying(firstError(
          maxLength(35, "previousClaimantName.error.lastName.length", childName.firstName),
          regexp(Validation.nameInputPattern, "previousClaimantName.error.lastName.invalid", childName.firstName)
        ))
    )(AdultName.apply(None, _, _, _))(name => Some(name.firstName, name.middleNames, name.lastName))
  )
}
