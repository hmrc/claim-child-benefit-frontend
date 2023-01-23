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

import forms.mappings.Mappings
import models.Country
import play.api.data.Form

import javax.inject.Inject

class ApplicantUsualCountryOfResidenceFormProvider @Inject() extends Mappings {

  def apply(): Form[Country] =
    Form(
      "value" -> text("applicantUsualCountryOfResidence.error.required")
        .verifying("applicantUsualCountryOfResidence.error.required", value => Country.internationalCountries.exists(_.code == value))
        .transform[Country](value => Country.internationalCountries.find(_.code == value).get, _.code)
    )
}