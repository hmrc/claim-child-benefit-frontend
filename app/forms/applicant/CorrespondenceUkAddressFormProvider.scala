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

package forms.applicant

import forms.mappings.Mappings
import models.UkAddress
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class CorrespondenceUkAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[UkAddress] = Form(
    mapping(
      "line1" -> text("correspondenceUkAddress.error.line1.required")
        .verifying(maxLength(100, "correspondenceUkAddress.error.line1.length")),
      "line2" -> optional(text("correspondenceUkAddress.error.line2.required")
        .verifying(maxLength(100, "correspondenceUkAddress.error.line2.length"))),
      "town" -> text("correspondenceUkAddress.error.town.required")
        .verifying(maxLength(100, "correspondenceUkAddress.error.town.length")),
      "county" -> optional(text("correspondenceUkAddress.error.county.required")
        .verifying(maxLength(100, "correspondenceUkAddress.error.county.length"))),
      "postcode" -> text("correspondenceUkAddress.error.postcode.required")
        .verifying(maxLength(100, "correspondenceUkAddress.error.postcode.length"))
    )(UkAddress.apply)(UkAddress.unapply)
  )
}