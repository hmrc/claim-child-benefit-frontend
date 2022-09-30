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
import forms.behaviours.IntFieldBehaviours
import models.ChildName
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.data.FormError

import java.time.{Clock, Instant, LocalDate, ZoneId}

class ChildScottishBirthCertificateDetailsFormProviderSpec extends IntFieldBehaviours {


  private val fixedInstant: Instant = LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant
  private val clockAtFixedInstant: Clock = Clock.fixed(fixedInstant, ZoneId.systemDefault)

  private val childName = ChildName("first", None, "last")

  val requiredKey = "childScottishBirthCertificateDetails.error.required"
  val invalidKey = "childScottishBirthCertificateDetails.error.invalid"

  val form = new ChildScottishBirthCertificateDetailsFormProvider(clockAtFixedInstant)(childName)

  ".district" - {

    val fieldName = "district"
    val requiredKey = "childScottishBirthCertificateDetails.district.error.required"
    val invalidKey = "childScottishBirthCertificateDetails.district.error.invalid"
    val outOfRangeKey = "childScottishBirthCertificateDetails.district.error.outOfRange"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.choose(100, 999).map(_.toString)
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      100,
      999,
      FormError(fieldName, outOfRangeKey, Seq(100, 999, childName.firstName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(childName.firstName))
    )
  }

  ".year" - {

    val max = LocalDate.now(clockAtFixedInstant).getYear
    val min = max - 20
    val fieldName = "year"
    val requiredKey = "childScottishBirthCertificateDetails.year.error.required"
    val invalidKey = "childScottishBirthCertificateDetails.year.error.invalid"
    val outOfRangeKey = "childScottishBirthCertificateDetails.year.error.outOfRange"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.choose(min, max).map(_.toString)
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      min,
      max,
      FormError(fieldName, outOfRangeKey, Seq(min.toString, max.toString, childName.firstName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(childName.firstName))
    )
  }

  ".entry" - {

    val fieldName = "entry"
    val requiredKey = "childScottishBirthCertificateDetails.entry.error.required"
    val invalidKey = "childScottishBirthCertificateDetails.entry.error.invalid"
    val outOfRangeKey = "childScottishBirthCertificateDetails.entry.error.outOfRange"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.choose(1, 999).map(_.toString)
    )

    behave like intFieldWithRange(
      form,
      fieldName,
      1,
      999,
      FormError(fieldName, outOfRangeKey, Seq(1, 999, childName.firstName))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey, Seq(childName.firstName))
    )
  }
}
