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

package forms.applicant

import forms.Validation
import forms.mappings.Mappings
import models.AdultName
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class ApplicantNameFormProvider @Inject() extends Mappings {

   def apply(): Form[AdultName] = Form(
     mapping(
       "title" -> optional(text("applicantName.error.title.required")
         .verifying(firstError(
           maxLength(35, "applicantName.error.title.length"),
           regexp(Validation.nameInputPattern, "applicantName.error.title.invalid")
         ))),
       "firstName" -> text("applicantName.error.firstName.required")
         .verifying(firstError(
           maxLength(35, "applicantName.error.firstName.length"),
           regexp(Validation.nameInputPattern, "applicantName.error.firstName.invalid")
         )),
       "middleNames" -> optional(text("applicantName.error.middleNames.required")
         .verifying(firstError(
           maxLength(35, "applicantName.error.middleNames.length"),
           regexp(Validation.nameInputPattern, "applicantName.error.middleNames.invalid")
         ))),
       "lastName" -> text("applicantName.error.lastName.required")
         .verifying(firstError(
           maxLength(35, "applicantName.error.lastName.length"),
           regexp(Validation.nameInputPattern, "applicantName.error.lastName.invalid")
         ))
    )(AdultName.apply)(AdultName.unapply)
   )
 }
