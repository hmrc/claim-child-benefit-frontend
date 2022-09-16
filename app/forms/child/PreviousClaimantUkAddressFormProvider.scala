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
import models.{AdultName, UkAddress}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class PreviousClaimantUkAddressFormProvider @Inject() extends Mappings {

  def apply(previousClaimant: AdultName): Form[UkAddress] = Form(
    mapping(
      "line1" -> text("previousClaimantUkAddress.error.line1.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantUkAddress.error.line1.length", previousClaimant.firstName)),
      "line2" -> optional(text("previousClaimantUkAddress.error.line2.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantUkAddress.error.line2.length", previousClaimant.firstName))),
      "town" -> text("previousClaimantUkAddress.error.town.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantUkAddress.error.town.length", previousClaimant.firstName)),
      "county" -> optional(text("previousClaimantUkAddress.error.county.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantUkAddress.error.county.length", previousClaimant.firstName))),
      "postcode" -> text("previousClaimantUkAddress.error.postcode.required", args = Seq(previousClaimant.firstName))
        .verifying(maxLength(100, "previousClaimantUkAddress.error.postcode.length", previousClaimant.firstName))
    )(UkAddress.apply)(UkAddress.unapply)
  )
}