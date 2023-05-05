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

import forms.Validation
import forms.mappings.Mappings
import models.{NorthernIrishBirthCertificateNumber, ChildName}
import play.api.data.Form

import javax.inject.Inject

class ChildNorthernIrishBirthCertificateNumberFormProvider @Inject() extends Mappings {

  def apply(childName: ChildName): Form[NorthernIrishBirthCertificateNumber] =
    Form(
      "value" -> text("childNorthernIrishBirthCertificateNumber.error.required", args = Seq(childName.firstName))
        .verifying(regexp(Validation.northernIrelandBirthCertificateNumberPattern, "childNorthernIrishBirthCertificateNumber.error.invalid", childName.firstName))
        .transform[NorthernIrishBirthCertificateNumber](x => NorthernIrishBirthCertificateNumber(x.replace(" ", "").replace("-", "")), x => x.value)
    )
}