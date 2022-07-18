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

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import models.AdultName

class ApplicantNameFormProvider @Inject() extends Mappings {

   def apply(): Form[AdultName] = Form(
     mapping(
       "title" -> optional(text("applicantName.error.title.required")
         .verifying(maxLength(20, "applicantName.error.title.length"))),
       "firstName" -> text("applicantName.error.firstName.required")
         .verifying(maxLength(100, "applicantName.error.firstName.length")),
       "middleNames" -> optional(text("applicantName.error.middleNames.required")
         .verifying(maxLength(100, "applicantName.error.middleNames.length"))),
       "lastName" -> text("applicantName.error.lastName.required")
         .verifying(maxLength(100, "applicantName.error.lastName.length"))
    )(AdultName.apply)(AdultName.unapply)
   )
 }
