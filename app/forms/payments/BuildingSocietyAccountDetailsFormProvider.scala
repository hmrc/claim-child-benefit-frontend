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

package forms.payments

import forms.Validation
import forms.mappings.Mappings
import models.BuildingSocietyAccountDetails
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class BuildingSocietyAccountDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[BuildingSocietyAccountDetails] = Form(
    mapping(
      "buildingSocietyName" -> text("buildingSocietyAccountDetails.error.buildingSocietyName.required")
        .verifying(maxLength(100, "buildingSocietyAccountDetails.error.buildingSocietyName.length")),
      "accountNumber" -> text("buildingSocietyAccountDetails.error.accountNumber.required")
        .verifying(regexp(Validation.accountNumberPattern.toString, "buildingSocietyAccountDetails.error.accountNumber.invalid")),
      "sortCode" -> text("buildingSocietyAccountDetails.error.sortCode.required")
        .verifying(regexp(Validation.sortCodePattern.toString, "buildingSocietyAccountDetails.error.sortCode.invalid")),
      "rollNumber" -> optional(
        text("buildingSocietyAccountDetails.error.rollNumber.required")
          .verifying(regexp(Validation.rollNumberPattern.toString, "buildingSocietyAccountDetails.error.rollNumber.required"))
      )
    )(BuildingSocietyAccountDetails.apply)(BuildingSocietyAccountDetails.unapply)
  )
}
