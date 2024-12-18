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

package forms.child

import forms.Validation
import forms.mappings.Mappings
import models.{AdultName, UkAddress}
import play.api.data.Form
import play.api.data.Forms._
import utils.RequestOps.MappingOps

import javax.inject.Inject

class PreviousGuardianUkAddressFormProvider @Inject() extends Mappings {

  def apply(previousGuardian: AdultName): Form[UkAddress] = Form(
    mapping(
      "line1" -> text("previousGuardianUkAddress.error.line1.required", args = Seq(previousGuardian.firstName))
        .verifying(firstError(
          maxLength(35, "previousGuardianUkAddress.error.line1.length", previousGuardian.firstName),
          regexp(Validation.addressInputPattern, "previousGuardianUkAddress.error.line1.invalid", previousGuardian.firstName)
        )),
      "line2" -> optional(text("previousGuardianUkAddress.error.line2.required", args = Seq(previousGuardian.firstName))
        .verifying(firstError(
          maxLength(35, "previousGuardianUkAddress.error.line2.length", previousGuardian.firstName),
          regexp(Validation.addressInputPattern, "previousGuardianUkAddress.error.line2.invalid", previousGuardian.firstName)
        ))),
      "town" -> text("previousGuardianUkAddress.error.town.required", args = Seq(previousGuardian.firstName))
        .verifying(firstError(
          maxLength(35, "previousGuardianUkAddress.error.town.length", previousGuardian.firstName),
          regexp(Validation.addressInputPattern, "previousGuardianUkAddress.error.town.invalid", previousGuardian.firstName)
        )),
      "county" -> optional(text("previousGuardianUkAddress.error.county.required", args = Seq(previousGuardian.firstName))
        .verifying(firstError(
          maxLength(35, "previousGuardianUkAddress.error.county.length", previousGuardian.firstName),
          regexp(Validation.addressInputPattern, "previousGuardianUkAddress.error.county.invalid", previousGuardian.firstName)
        ))),
      "postcode" -> text("previousGuardianUkAddress.error.postcode.required", args = Seq(previousGuardian.firstName))
        .removeWhitespaces()
        .verifying(
          maxLength(8, "previousGuardianUkAddress.error.postcode.length", previousGuardian.firstName),
          regexp(Validation.ukPostcodePattern, "previousGuardianUkAddress.error.postcode.invalid", previousGuardian.firstName)
        )
    )(UkAddress.apply)(o => Some(Tuple.fromProductTyped(o)))
  )
}
