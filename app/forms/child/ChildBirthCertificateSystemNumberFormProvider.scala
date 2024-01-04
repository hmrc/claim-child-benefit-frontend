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

package forms.child

import forms.Validation
import forms.mappings.Mappings
import models.{BirthCertificateSystemNumber, ChildName}
import play.api.data.Form

import javax.inject.Inject

class ChildBirthCertificateSystemNumberFormProvider @Inject() extends Mappings {

  def apply(childName: ChildName): Form[BirthCertificateSystemNumber] =
    Form(
      "value" -> text("childBirthCertificateSystemNumber.error.required", args = Seq(childName.firstName))
        .verifying(regexp(Validation.systemNumberPattern, "childBirthCertificateSystemNumber.error.invalid", childName.firstName))
        .transform[BirthCertificateSystemNumber](x => BirthCertificateSystemNumber(x.replace(" ", "").replace("-", "")), x => x.value)
    )
}
