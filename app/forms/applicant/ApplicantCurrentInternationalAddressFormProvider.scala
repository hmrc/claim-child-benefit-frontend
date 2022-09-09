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

package forms.applicant

import forms.mappings.Mappings
import models.{Country, InternationalAddress}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ApplicantCurrentInternationalAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[InternationalAddress] = Form(
    mapping(
      "line1" -> text("applicantCurrentInternationalAddress.error.line1.required")
        .verifying(maxLength(100, "applicantCurrentInternationalAddress.error.line1.length")),
      "line2" -> optional(text("applicantCurrentInternationalAddress.error.line2.required")
        .verifying(maxLength(100, "applicantCurrentInternationalAddress.error.line2.length"))),
      "town" -> text("applicantCurrentInternationalAddress.error.town.required")
        .verifying(maxLength(100, "applicantCurrentInternationalAddress.error.town.length")),
      "state" -> optional(text("applicantCurrentInternationalAddress.error.state.required")
        .verifying(maxLength(100, "applicantCurrentInternationalAddress.error.state.length"))),
      "postcode" -> optional(text("applicantCurrentInternationalAddress.error.postcode.required")
        .verifying(maxLength(100, "applicantCurrentInternationalAddress.error.postcode.length"))),
      "country" -> text("applicantCurrentInternationalAddress.error.country.required")
        .verifying("applicantCurrentInternationalAddress.error.country.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}