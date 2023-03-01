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
import models.UkAddress
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ApplicantPreviousUkAddressFormProvider @Inject() extends Mappings {

   def apply(): Form[UkAddress] = Form(
     mapping(
       "line1" -> text("applicantPreviousUkAddress.error.line1.required")
         .verifying(firstError(
           maxLength(35, "applicantPreviousUkAddress.error.line1.length"),
           regexp(Validation.addressInputPattern, "applicantPreviousUkAddress.error.line1.invalid")
         )),
       "line2" -> optional(text("applicantPreviousUkAddress.error.line2.required")
         .verifying(firstError(
           maxLength(35, "applicantPreviousUkAddress.error.line2.length"),
           regexp(Validation.addressInputPattern, "applicantPreviousUkAddress.error.line2.invalid")
         ))),
       "town" -> text("applicantPreviousUkAddress.error.town.required")
         .verifying(firstError(
           maxLength(35, "applicantPreviousUkAddress.error.town.length"),
           regexp(Validation.addressInputPattern, "applicantPreviousUkAddress.error.town.invalid")
         )),
       "county" -> optional(text("applicantPreviousUkAddress.error.county.required")
         .verifying(firstError(
           maxLength(35, "applicantPreviousUkAddress.error.county.length"),
           regexp(Validation.addressInputPattern, "applicantPreviousUkAddress.error.county.invalid")
         ))),
       "postcode" -> text("applicantPreviousUkAddress.error.postcode.required")
         .verifying(firstError(
           maxLength(8, "applicantPreviousUkAddress.error.postcode.length"),
           regexp(Validation.ukPostcodePattern, "applicantPreviousUkAddress.error.postcode.invalid")
         ))
    )(UkAddress.apply)(UkAddress.unapply)
   )
 }
