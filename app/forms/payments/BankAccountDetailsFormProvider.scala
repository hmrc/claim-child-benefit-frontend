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
import models.BankAccountDetails
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class BankAccountDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[BankAccountDetails] = Form(
    mapping(
      "accountName" -> text("bankAccountDetails.error.accountName.required")
        .verifying(maxLength(100, "bankAccountDetails.error.accountName.length")),
      "accountNumber" -> text("bankAccountDetails.error.accountNumber.required")
        .verifying(regexp(Validation.accountNumberPattern.toString, "bankAccountDetails.error.accountNumber.invalid")),
      "sortCode" -> text("bankAccountDetails.error.sortCode.required")
        .verifying(regexp(Validation.sortCodePattern.toString, "bankAccountDetails.error.sortCode.invalid")),
      "rollNumber" -> optional(text("bankAccountDetails.error.sortCode.required")
        .verifying(firstError(
          maxLength(18, "bankAccountDetails.error.rollNumber.length"),
          regexp(Validation.rollNumberPattern.toString, "bankAccountDetails.error.rollNumber.invalid")
        )))
    )(BankAccountDetails.apply)(BankAccountDetails.unapply)
  )
}
