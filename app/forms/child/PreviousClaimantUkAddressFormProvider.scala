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
import models.{AdultName, UkAddress}
import play.api.data.Form
import play.api.data.Forms._
import utils.RequestOps.MappingOps

import javax.inject.Inject

class PreviousClaimantUkAddressFormProvider @Inject() extends Mappings {

  def apply(previousClaimant: AdultName): Form[UkAddress] = Form(
    mapping(
      "line1" -> text("previousClaimantUkAddress.error.line1.required", args = Seq(previousClaimant.firstName))
        .verifying(firstError(
          maxLength(35, "previousClaimantUkAddress.error.line1.length", previousClaimant.firstName),
          regexp(Validation.addressInputPattern, "previousClaimantUkAddress.error.line1.invalid", previousClaimant.firstName)
        )),
      "line2" -> optional(text("previousClaimantUkAddress.error.line2.required", args = Seq(previousClaimant.firstName))
        .verifying(firstError(
          maxLength(35, "previousClaimantUkAddress.error.line2.length", previousClaimant.firstName),
          regexp(Validation.addressInputPattern, "previousClaimantUkAddress.error.line2.invalid", previousClaimant.firstName)
        ))),
      "town" -> text("previousClaimantUkAddress.error.town.required", args = Seq(previousClaimant.firstName))
        .verifying(firstError(
          maxLength(35, "previousClaimantUkAddress.error.town.length", previousClaimant.firstName),
          regexp(Validation.addressInputPattern, "previousClaimantUkAddress.error.town.invalid", previousClaimant.firstName)
        )),
      "county" -> optional(text("previousClaimantUkAddress.error.county.required", args = Seq(previousClaimant.firstName))
        .verifying(firstError(
          maxLength(35, "previousClaimantUkAddress.error.county.length", previousClaimant.firstName),
          regexp(Validation.addressInputPattern, "previousClaimantUkAddress.error.county.invalid", previousClaimant.firstName)
        ))),
      "postcode" -> text("previousClaimantUkAddress.error.postcode.required", args = Seq(previousClaimant.firstName))
        .removeWhitespaces()
        .verifying(
          maxLength(8, "previousClaimantUkAddress.error.postcode.length", previousClaimant.firstName),
          regexp(Validation.ukPostcodePattern, "previousClaimantUkAddress.error.postcode.invalid", previousClaimant.firstName)
        )
    )(UkAddress.apply)(UkAddress.unapply)
  )
}
