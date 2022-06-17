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

package forms

import com.google.i18n.phonenumbers.PhoneNumberUtil

import javax.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form

import scala.util.Try

class ApplicantPhoneNumberFormProvider @Inject() extends Mappings {

  private val util = PhoneNumberUtil.getInstance

  def apply(): Form[String] =
    Form(
      "value" -> text("applicantPhoneNumber.error.required")
        .verifying("applicantPhoneNumber.error.invalid", isValid(_))
    )

  private def isValid(string: String): Boolean =
    Try(util.isPossibleNumber(util.parse(string, "GB")))
      .getOrElse(false)
}
