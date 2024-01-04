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
import models.EmploymentStatus
import play.api.data.Form
import play.api.data.Forms.set

import javax.inject.Inject

class PartnerEmploymentStatusFormProvider @Inject() extends Mappings {

  def apply(partnerFirstName: String): Form[Set[EmploymentStatus]] =
    Form(
      "value" ->
        set(enumerable[EmploymentStatus]("partnerEmploymentStatus.error.required", args = Seq(partnerFirstName)))
          .verifying(
            nonEmptySet("partnerEmploymentStatus.error.required", args = partnerFirstName),
            noMutuallyExclusiveAnswers[EmploymentStatus](
              EmploymentStatus.activeStatuses,
              Set(EmploymentStatus.NoneOfThese),
              "partnerEmploymentStatus.error.mutuallyExclusive",
              args = partnerFirstName
            )
          )
    )
}
