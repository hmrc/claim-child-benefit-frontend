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

package forms.child

import forms.mappings.Mappings
import models.ChildScottishBirthCertificateDetails
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ChildScottishBirthCertificateDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[ChildScottishBirthCertificateDetails] = Form(
    mapping(
      "district" -> text("childScottishBirthCertificateDetails.error.district.required")
        .verifying(maxLength(3, "childScottishBirthCertificateDetails.error.district.length")),
      "year" -> text("childScottishBirthCertificateDetails.error.year.required")
        .verifying(maxLength(4, "childScottishBirthCertificateDetails.error.year.length")),
      "entryNumber" -> text("childScottishBirthCertificateDetails.error.entryNumber.required")
        .verifying(maxLength(3, "childScottishBirthCertificateDetails.error.entryNumber.length"))
    )(ChildScottishBirthCertificateDetails.apply)(ChildScottishBirthCertificateDetails.unapply)
  )
}