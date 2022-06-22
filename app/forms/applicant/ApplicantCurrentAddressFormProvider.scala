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
import models.ApplicantCurrentAddress
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ApplicantCurrentAddressFormProvider @Inject() extends Mappings {

  def apply(): Form[ApplicantCurrentAddress] = Form(
    mapping(
      "line1" -> text("applicantCurrentAddress.error.line1.required")
        .verifying(maxLength(100, "applicantCurrentAddress.error.line1.length")),
      "line2" -> optional(text("applicantCurrentAddress.error.line2.required")
        .verifying(maxLength(100, "applicantCurrentAddress.error.line2.length"))),
      "line3" -> optional(text("applicantCurrentAddress.error.line3.required")
        .verifying(maxLength(100, "applicantCurrentAddress.error.line3.length"))),
      "postcode" -> text("applicantCurrentAddress.error.postcode.required")
        .verifying(maxLength(100, "applicantCurrentAddress.error.postcode.length"))
    )(ApplicantCurrentAddress.apply)(ApplicantCurrentAddress.unapply)
  )
}