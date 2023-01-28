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

package forms.partner

import forms.mappings.Mappings
import models.Nationality
import play.api.data.Form

import javax.inject.Inject

class PartnerNationalityFormProvider @Inject() extends Mappings {

  def apply(partnerFirstName: String): Form[Nationality] =
    Form(
      "value" -> text("partnerNationality.error.required", args = Seq(partnerFirstName))
        .verifying("partnerNationality.error.required", value => Nationality.allNationalities.exists(_.name == value))
        .transform[Nationality](value => Nationality.allNationalities.find(_.name == value).get, _.name)
    )
}
