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

import forms.Validation
import forms.mappings.Mappings
import models.UkAddress
import play.api.data.Form
import play.api.data.Forms._
import utils.RequestOps.MappingOps

import javax.inject.Inject

class DesignatoryUkAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[UkAddress] = Form(
    mapping(
      "line1" -> text("designatoryUkAddress.error.line1.required")
        .verifying(firstError(
          maxLength(35, "designatoryUkAddress.error.line1.length"),
          regexp(Validation.addressInputPattern, "designatoryUkAddress.error.line1.invalid")
        )),
      "line2" -> optional(text("designatoryUkAddress.error.line2.required")
        .verifying(firstError(
          maxLength(35, "designatoryUkAddress.error.line2.length"),
          regexp(Validation.addressInputPattern, "designatoryUkAddress.error.line2.invalid")
        ))),
      "town" -> text("designatoryUkAddress.error.town.required")
        .verifying(firstError(
          maxLength(35, "designatoryUkAddress.error.town.length"),
          regexp(Validation.addressInputPattern, "designatoryUkAddress.error.town.invalid")
        )),
      "county" -> optional(text("designatoryUkAddress.error.county.required")
        .verifying(firstError(
          maxLength(35, "designatoryUkAddress.error.county.length"),
          regexp(Validation.addressInputPattern, "designatoryUkAddress.error.county.invalid")
        ))),
      "postcode" -> text("designatoryUkAddress.error.postcode.required")
        .removeWhitespaces()
        .verifying(
          maxLength(8, "designatoryUkAddress.error.postcode.length"),
          regexp(Validation.ukPostcodePattern, "designatoryUkAddress.error.postcode.invalid")
        )
    )(UkAddress.apply)(UkAddress.unapply)
  )
}