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

package forms.partner

import forms.mappings.Mappings
import models.{Country, Index}
import play.api.data.Form

import javax.inject.Inject

class CountryPartnerReceivedBenefitsFormProvider @Inject() extends Mappings {

  def apply(partnerFirstName: String, thisIndex: Index, existingAnswers: Seq[Country]): Form[Country] =
    Form(
      "value" -> text("countryPartnerReceivedBenefits.error.required", args = Seq(partnerFirstName))
        .verifying("countryPartnerReceivedBenefits.error.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
        .verifying(notADuplicate(thisIndex, existingAnswers, "countryPartnerReceivedBenefits.error.duplicate", partnerFirstName))
    )
}
