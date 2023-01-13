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

class PreviousClaimantInternationalAddressFormProvider @Inject() extends Mappings {

  def apply(previousClaimant: AdultName): Form[InternationalAddress] = Form(
    mapping(
      "line1" -> text("previousClaimantInternationalAddress.error.line1.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantInternationalAddress.error.line1.length", args = previousClaimant.firstName)),
      "line2" -> optional(text("previousClaimantInternationalAddress.error.line2.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantInternationalAddress.error.line2.length", args = previousClaimant.firstName))),
      "town" -> text("previousClaimantInternationalAddress.error.town.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantInternationalAddress.error.town.length", args = previousClaimant.firstName)),
      "state" -> optional(text("previousClaimantInternationalAddress.error.state.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantInternationalAddress.error.state.length", args = previousClaimant.firstName))),
      "postcode" -> optional(text("previousClaimantInternationalAddress.error.postcode.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantInternationalAddress.error.postcode.length", args = previousClaimant.firstName))),
      "country" -> text("previousClaimantInternationalAddress.error.country.required", args = Seq(previousClaimant.firstName))
        .verifying("previousClaimantInternationalAddress.error.country.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
    )(InternationalAddress.apply)(InternationalAddress.unapply)
  )
}