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
import models.{ChildName, ScottishBirthCertificateDetails}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.{Constraint, Invalid, Valid}

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class ChildScottishBirthCertificateDetailsFormProvider @Inject()(clock: Clock) extends Mappings {

  def apply(childName: ChildName): Form[ScottishBirthCertificateDetails] = {

    val maxYear = LocalDate.now(clock).getYear
    val minYear = maxYear - 20

    Form(
      mapping(
        "district" -> int(
          "childScottishBirthCertificateDetails.district.error.required",
          "childScottishBirthCertificateDetails.district.error.invalid",
          "childScottishBirthCertificateDetails.district.error.invalid",
          args = Seq(childName.firstName)
        ).verifying(inRange(100, 999, "childScottishBirthCertificateDetails.district.error.outOfRange", args = childName.firstName)),
        "year" -> int(
          "childScottishBirthCertificateDetails.year.error.required",
          "childScottishBirthCertificateDetails.year.error.invalid",
          "childScottishBirthCertificateDetails.year.error.invalid",
          args = Seq(childName.firstName)
        ).verifying(yearInRange(minYear, maxYear, "childScottishBirthCertificateDetails.year.error.outOfRange", args = childName.firstName)),
        "entry" -> int(
          "childScottishBirthCertificateDetails.entry.error.required",
          "childScottishBirthCertificateDetails.entry.error.invalid",
          "childScottishBirthCertificateDetails.entry.error.invalid",
          args = Seq(childName.firstName)
        ).verifying(inRange(1, 999, "childScottishBirthCertificateDetails.entry.error.outOfRange", args = childName.firstName))
      )(ScottishBirthCertificateDetails.apply)(ScottishBirthCertificateDetails.unapply)
    )
  }

  private def yearInRange(minimum: Int, maximum: Int, errorKey: String, args: Any*): Constraint[Int] =
    Constraint {
      input =>

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, Seq(minimum.toString, maximum.toString) ++ args: _*)
        }
    }

}
