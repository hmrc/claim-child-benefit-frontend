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
import models.{AdultName, Country, InternationalAddress}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class GuardianInternationalAddressFormProvider @Inject() extends Mappings {

  def apply(guardian: AdultName): Form[InternationalAddress] = Form(
    mapping(
      "line1" -> text("guardianInternationalAddress.error.line1.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianInternationalAddress.error.line1.length", args = guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianInternationalAddress.error.line1.invalid", guardian.firstName)
        )),
      "line2" -> optional(text("guardianInternationalAddress.error.line2.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianInternationalAddress.error.line2.length", args = guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianInternationalAddress.error.line2.invalid", guardian.firstName)
        ))),
      "town" -> text("guardianInternationalAddress.error.town.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianInternationalAddress.error.town.length", args = guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianInternationalAddress.error.town.invalid", guardian.firstName)
        )),
      "state" -> optional(text("guardianInternationalAddress.error.state.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianInternationalAddress.error.state.length", args = guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianInternationalAddress.error.state.invalid", guardian.firstName)
        ))),
      "postcode" -> optional(text("guardianInternationalAddress.error.postcode.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(8, "guardianInternationalAddress.error.postcode.length", args = guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianInternationalAddress.error.postcode.invalid", guardian.firstName)
        ))),
      "country" -> text("guardianInternationalAddress.error.country.required", args = Seq(guardian.firstName))
        .verifying("guardianInternationalAddress.error.country.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}