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

import forms.Validation
import forms.mappings.Mappings
import models.AdultName
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class PartnerNameFormProvider @Inject() extends Mappings {

  def apply(): Form[AdultName] = Form(
    mapping(
      "title" -> optional(text("partnerName.error.title.required")
        .verifying(firstError(
          maxLength(35, "partnerName.error.title.length"),
          regexp(Validation.nameInputPattern, "partnerName.error.title.invalid")
        ))),
      "firstName" -> text("partnerName.error.firstName.required")
        .verifying(firstError(
          maxLength(35, "partnerName.error.firstName.length"),
          regexp(Validation.nameInputPattern, "partnerName.error.firstName.invalid")
        )),
      "middleNames" -> optional(text("partnerName.error.middleNames.required")
        .verifying(firstError(
          maxLength(35, "partnerName.error.middleNames.length"),
          regexp(Validation.nameInputPattern, "partnerName.error.middleNames.invalid")
        ))),
      "lastName" -> text("partnerName.error.lastName.required")
        .verifying(firstError(
          maxLength(35, "partnerName.error.lastName.length"),
          regexp(Validation.nameInputPattern, "partnerName.error.lastName.invalid")
        ))
    )(AdultName.apply)(AdultName.unapply)
  )
}
