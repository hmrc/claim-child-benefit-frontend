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

package forms.child

import forms.mappings.Mappings
import models.{AdultName, Country, InternationalAddress}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class PreviousGuardianInternationalAddressFormProvider @Inject() extends Mappings {

  def apply(previousGuardian: AdultName): Form[InternationalAddress] = Form(
    mapping(
      "line1" -> text("previousGuardianInternationalAddress.error.line1.required", args = Seq(previousGuardian.firstName))
        .verifying(maxLength(100, "previousGuardianInternationalAddress.error.line1.length", args = previousGuardian.firstName)),
      "line2" -> optional(text("previousGuardianInternationalAddress.error.line2.required", args = Seq(previousGuardian.firstName))
        .verifying(maxLength(100, "previousGuardianInternationalAddress.error.line2.length", args = previousGuardian.firstName))),
      "town" -> text("previousGuardianInternationalAddress.error.town.required", args = Seq(previousGuardian.firstName))
        .verifying(maxLength(100, "previousGuardianInternationalAddress.error.town.length", args = previousGuardian.firstName)),
      "state" -> optional(text("previousGuardianInternationalAddress.error.state.required", args = Seq(previousGuardian.firstName))
        .verifying(maxLength(100, "previousGuardianInternationalAddress.error.state.length", args = previousGuardian.firstName))),
      "postcode" -> optional(text("previousGuardianInternationalAddress.error.postcode.required", args = Seq(previousGuardian.firstName))
        .verifying(maxLength(100, "previousGuardianInternationalAddress.error.postcode.length", args = previousGuardian.firstName))),
      "country" -> text("previousGuardianInternationalAddress.error.country.required", args = Seq(previousGuardian.firstName))
        .verifying("previousGuardianInternationalAddress.error.country.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}