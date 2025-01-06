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

package forms.applicant

import forms.Validation
import forms.mappings.Mappings
import models.UkAddress
import play.api.data.Form
import play.api.data.Forms._
import utils.RequestOps.MappingOps

import javax.inject.Inject

class ApplicantCurrentUkAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[UkAddress] = Form(
    mapping(
      "line1" -> text("applicantCurrentUkAddress.error.line1.required")
        .verifying(firstError(
          maxLength(35, "applicantCurrentUkAddress.error.line1.length"),
          regexp(Validation.addressInputPattern, "applicantCurrentUkAddress.error.line1.invalid")
        )),
      "line2" -> optional(text("applicantCurrentUkAddress.error.line2.required")
        .verifying(firstError(
          maxLength(35, "applicantCurrentUkAddress.error.line2.length"),
          regexp(Validation.addressInputPattern, "applicantCurrentUkAddress.error.line2.invalid")
        ))),
      "town" -> text("applicantCurrentUkAddress.error.town.required")
        .verifying(firstError(
          maxLength(35, "applicantCurrentUkAddress.error.town.length"),
          regexp(Validation.addressInputPattern, "applicantCurrentUkAddress.error.town.invalid")
        )),
      "county" -> optional(text("applicantCurrentUkAddress.error.county.required")
        .verifying(firstError(
          maxLength(35, "applicantCurrentUkAddress.error.county.length"),
          regexp(Validation.addressInputPattern, "applicantCurrentUkAddress.error.county.invalid")
        ))),
      "postcode" -> text("applicantCurrentUkAddress.error.postcode.required")
        .removeWhitespaces()
        .verifying(
          maxLength(8, "applicantCurrentUkAddress.error.postcode.length"),
          regexp(Validation.ukPostcodePattern, "applicantCurrentUkAddress.error.postcode.invalid")
        )
    )(UkAddress.apply)(o => Some(Tuple.fromProductTyped(o)))
  )
}