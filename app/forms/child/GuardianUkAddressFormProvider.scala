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

class GuardianUkAddressFormProvider @Inject() extends Mappings {

  def apply(guardian: AdultName): Form[UkAddress] = Form(
    mapping(
      "line1" -> text("guardianUkAddress.error.line1.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianUkAddress.error.line1.length", guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianUkAddress.error.line1.invalid", guardian.firstName)
        )),
      "line2" -> optional(text("guardianUkAddress.error.line2.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianUkAddress.error.line2.length", guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianUkAddress.error.line2.invalid", guardian.firstName)
        ))),
      "town" -> text("guardianUkAddress.error.town.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianUkAddress.error.town.length", guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianUkAddress.error.town.invalid", guardian.firstName)
        )),
      "county" -> optional(text("guardianUkAddress.error.county.required", args = Seq(guardian.firstName))
        .verifying(firstError(
          maxLength(35, "guardianUkAddress.error.county.length", guardian.firstName),
          regexp(Validation.addressInputPattern, "guardianUkAddress.error.county.invalid", guardian.firstName)
        ))),
      "postcode" -> text("guardianUkAddress.error.postcode.required", args = Seq(guardian.firstName))
        .removeWhitespaces()
        .verifying(
          regexp(Validation.ukPostcodePattern, "guardianUkAddress.error.postcode.invalid", guardian.firstName),
          maxLength(8, "guardianUkAddress.error.postcode.length", guardian.firstName)
        )
    )(UkAddress.apply)(UkAddress.unapply)
  )
}
