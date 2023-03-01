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

class DesignatoryInternationalAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[InternationalAddress] = Form(
    mapping(
      "line1" -> text("designatoryInternationalAddress.error.line1.required")
        .verifying(firstError(
          maxLength(35, "designatoryInternationalAddress.error.line1.length"),
          regexp(Validation.addressInputPattern, "designatoryInternationalAddress.error.line1.invalid")
        )),
      "line2" -> optional(text("designatoryInternationalAddress.error.line2.required")
        .verifying(firstError(
          maxLength(35, "designatoryInternationalAddress.error.line2.length"),
          regexp(Validation.addressInputPattern, "designatoryInternationalAddress.error.line2.invalid")
        ))),
      "town" -> text("designatoryInternationalAddress.error.town.required")
        .verifying(firstError(
          maxLength(35, "designatoryInternationalAddress.error.town.length"),
          regexp(Validation.addressInputPattern, "designatoryInternationalAddress.error.town.invalid")
        )),
      "state" -> optional(text("designatoryInternationalAddress.error.state.required")
        .verifying(firstError(
          maxLength(35, "designatoryInternationalAddress.error.state.length"),
          regexp(Validation.addressInputPattern, "designatoryInternationalAddress.error.state.invalid")
        ))),
      "postcode" -> optional(text("designatoryInternationalAddress.error.postcode.required")
        .verifying(firstError(
          maxLength(8, "designatoryInternationalAddress.error.postcode.length"),
          regexp(Validation.addressInputPattern, "designatoryInternationalAddress.error.postcode.invalid")
        ))),
      "country" -> text("designatoryInternationalAddress.error.country.required")
        .verifying("designatoryInternationalAddress.error.country.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}