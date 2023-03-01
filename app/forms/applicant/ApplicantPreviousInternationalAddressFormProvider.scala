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
import models.{Country, InternationalAddress}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ApplicantPreviousInternationalAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[InternationalAddress] = Form(
    mapping(
      "line1" -> text("applicantPreviousInternationalAddress.error.line1.required")
        .verifying(firstError(
          maxLength(35, "applicantPreviousInternationalAddress.error.line1.length"),
          regexp(Validation.addressInputPattern, "applicantPreviousInternationalAddress.error.line1.invalid")
        )),
      "line2" -> optional(text("applicantPreviousInternationalAddress.error.line2.required")
        .verifying(firstError(
          maxLength(35, "applicantPreviousInternationalAddress.error.line2.length"),
          regexp(Validation.addressInputPattern, "applicantPreviousInternationalAddress.error.line2.invalid")
        ))),
      "town" -> text("applicantPreviousInternationalAddress.error.town.required")
        .verifying(firstError(
          maxLength(35, "applicantPreviousInternationalAddress.error.town.length"),
          regexp(Validation.addressInputPattern, "applicantPreviousInternationalAddress.error.town.invalid")
        )),
      "state" -> optional(text("applicantPreviousInternationalAddress.error.state.required")
        .verifying(firstError(
          maxLength(35, "applicantPreviousInternationalAddress.error.state.length"),
          regexp(Validation.addressInputPattern, "applicantPreviousInternationalAddress.error.state.invalid")
        ))),
      "postcode" -> optional(text("applicantPreviousInternationalAddress.error.postcode.required")
        .verifying(firstError(
          maxLength(8, "applicantPreviousInternationalAddress.error.postcode.length"),
          regexp(Validation.addressInputPattern, "applicantPreviousInternationalAddress.error.postcode.invalid")
        ))),
      "country" -> text("applicantPreviousInternationalAddress.error.country.required")
        .verifying("applicantPreviousInternationalAddress.error.country.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}