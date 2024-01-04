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

package forms.payments

import forms.Validation
import forms.mappings.Mappings
import models.{BuildingSociety, BuildingSocietyDetails}
import play.api.data.Form
import play.api.data.Forms._

import javax.inject.Inject

class BuildingSocietyDetailsFormProvider @Inject() extends Mappings {

  def apply(): Form[BuildingSocietyDetails] = Form(
    mapping(
      "firstName" -> text("buildingSocietyDetails.error.firstName.required")
        .verifying(firstError(
          maxLength(35, "buildingSocietyDetails.error.firstName.length"),
          regexp(Validation.nameInputPattern, "buildingSocietyDetails.error.firstName.invalid")
        )),
      "lastName" -> text("buildingSocietyDetails.error.lastName.required")
        .verifying(firstError(
          maxLength(35, "buildingSocietyDetails.error.lastName.length"),
          regexp(Validation.nameInputPattern, "buildingSocietyDetails.error.lastName.invalid")
        )),
      "buildingSociety" -> text("buildingSocietyDetails.error.buildingSociety.required")
        .verifying("buildingSocietyDetails.error.buildingSociety.required", x => BuildingSociety.allBuildingSocieties.exists(_.id == x))
        .transform[BuildingSociety](x => BuildingSociety.allBuildingSocieties.find(_.id == x).get, _.id),
      "rollNumber" -> text("buildingSocietyDetails.error.rollNumber.required")
        .verifying(firstError(
          maxLength(18, "buildingSocietyDetails.error.rollNumber.length"),
          regexp(Validation.rollNumberPattern, "buildingSocietyDetails.error.rollNumber.invalid")
        ))
    )(BuildingSocietyDetails.apply)(BuildingSocietyDetails.unapply)
  )
}
