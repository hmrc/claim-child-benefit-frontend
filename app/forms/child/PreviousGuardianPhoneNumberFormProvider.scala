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

import com.google.i18n.phonenumbers.PhoneNumberUtil
import forms.mappings.Mappings
import models.AdultName
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

import javax.inject.Inject
import scala.util.Try

class PreviousGuardianPhoneNumberFormProvider @Inject() extends Mappings {

  private val util = PhoneNumberUtil.getInstance

  def apply(previousGuardian: AdultName): Form[String] =
    Form(
      "value" -> text("previousGuardianPhoneNumber.error.required", args = Seq(previousGuardian.firstName))
        .verifying(validPhoneNumber(previousGuardian))
    )

  private def validPhoneNumber(previousGuardian: AdultName): Constraint[String] =
    Constraint {
      input =>
        val valid = Try(util.isPossibleNumber(util.parse(input, "GB"))).getOrElse(false)

        if (valid) {
          Valid
        } else {
          Invalid("previousGuardianPhoneNumber.error.invalid", previousGuardian.firstName)
        }
    }
}
