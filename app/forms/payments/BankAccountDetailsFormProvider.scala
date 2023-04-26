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

package forms.payments

import forms.Validation
import forms.mappings.Mappings
import models.BankAccountDetails
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class BankAccountDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[BankAccountDetailsFormModel] = Form(
    mapping(
      "firstName" -> text("bankAccountDetails.error.firstName.required")
        .verifying(firstError(
          maxLength(35, "bankAccountDetails.error.firstName.length"),
          regexp(Validation.nameInputPattern, "bankAccountDetails.error.firstName.invalid")
        )),
      "lastName" -> text("bankAccountDetails.error.lastName.required")
        .verifying(firstError(
          maxLength(35, "bankAccountDetails.error.lastName.length"),
          regexp(Validation.nameInputPattern, "bankAccountDetails.error.lastName.invalid")
        )),
      "sortCode" -> text("bankAccountDetails.error.sortCode.required")
        .verifying(regexp(Validation.sortCodePattern, "bankAccountDetails.error.sortCode.invalid")),
      "accountNumber" -> text("bankAccountDetails.error.accountNumber.required")
        .verifying(regexp(Validation.accountNumberPattern, "bankAccountDetails.error.accountNumber.invalid")),
      "softError" -> optional(boolean())
    )
    ((f, l, s, a, e) => BankAccountDetailsFormModel(BankAccountDetails(f, l, s, a), e))
    (f => Some(f.details.firstName, f.details.lastName, f.details.sortCode, f.details.accountNumber, f.softError))
  )
}

final case class BankAccountDetailsFormModel(details: BankAccountDetails, softError: Option[Boolean])
