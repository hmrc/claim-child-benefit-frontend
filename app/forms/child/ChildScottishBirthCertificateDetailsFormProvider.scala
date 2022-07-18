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

import forms.Validation
import forms.mappings.Mappings
import models.ChildScottishBirthCertificateDetails
import play.api.data.Form
import play.api.data.Forms._

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class ChildScottishBirthCertificateDetailsFormProvider @Inject()(clock: Clock) extends Mappings {

  def apply(): Form[ChildScottishBirthCertificateDetails] = {

    val maxYear = LocalDate.now(clock).getYear
    val minYear = maxYear - 20

    Form(
      mapping(
        "district" -> text("childScottishBirthCertificateDetails.error.district.required")
          .verifying(regexp(Validation.districtPattern, "childScottishBirthCertificateDetails.error.district.invalid")),
        "year" -> int(
          requiredKey    = "childScottishBirthCertificateDetails.error.year.required",
          wholeNumberKey = "childScottishBirthCertificateDetails.error.year.invalid",
          nonNumericKey  = "childScottishBirthCertificateDetails.error.year.invalid"
        ).verifying(minimumValue(minYear, "childScottishBirthCertificateDetails.error.year.belowMinimum"))
          .verifying(maximumValue(maxYear, "childScottishBirthCertificateDetails.error.year.aboveMaximum")),
        "entryNumber" -> text("childScottishBirthCertificateDetails.error.entryNumber.required")
          .verifying(regexp(Validation.entryNumberPattern, "childScottishBirthCertificateDetails.error.entryNumber.invalid"))
      )(ChildScottishBirthCertificateDetails.apply)(ChildScottishBirthCertificateDetails.unapply)
    )
  }
}
