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

package journey

import generators.ModelGenerators
import models.{AdultName, InternationalAddress, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.applicant._

class DesignatoryDetailsJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def name = arbitrary[AdultName].sample.value
  private def ukAddress = arbitrary[UkAddress].sample.value
  private def internationalAddress = arbitrary[InternationalAddress].sample.value

  "users must go from Designatory Name to Check Designatory Details" in {

    startingFrom(DesignatoryNamePage)
      .run(
        submitAnswer(DesignatoryNamePage, name),
        pageMustBe(CheckDesignatoryDetailsPage)
      )
  }

  "users must be able to give a UK designatory address then go to Check Designatory Details" in {

    startingFrom(DesignatoryAddressInUkPage)
      .run(
        submitAnswer(DesignatoryAddressInUkPage, true),
        submitAnswer(DesignatoryUkAddressPage, ukAddress),
        pageMustBe(CheckDesignatoryDetailsPage)
      )
  }

  "users must be able to give an international designatory address then go to Check Designatory Details" in {

    startingFrom(DesignatoryAddressInUkPage)
      .run(
        submitAnswer(DesignatoryAddressInUkPage, false),
        submitAnswer(DesignatoryInternationalAddressPage, internationalAddress),
        pageMustBe(CheckDesignatoryDetailsPage)
      )
  }

  "users must be able to give a UK correspondence address then go to Check Designatory Details" in {

    startingFrom(CorrespondenceAddressInUkPage)
      .run(
        submitAnswer(CorrespondenceAddressInUkPage, true),
        submitAnswer(CorrespondenceUkAddressPage, ukAddress),
        pageMustBe(CheckDesignatoryDetailsPage)
      )
  }

  "users must be able to give an international correspondence address then go to Check Designatory Details" in {

    startingFrom(CorrespondenceAddressInUkPage)
      .run(
        submitAnswer(CorrespondenceAddressInUkPage, false),
        submitAnswer(CorrespondenceInternationalAddressPage, internationalAddress),
        pageMustBe(CheckDesignatoryDetailsPage)
      )
  }

  "users must go from Check Designatory Details to Applicant Phone Number" in {

    startingFrom(CheckDesignatoryDetailsPage)
      .run(
        submitAnswer(CheckDesignatoryDetailsPage, true),
        pageMustBe(ApplicantPhoneNumberPage)
      )
  }
}
