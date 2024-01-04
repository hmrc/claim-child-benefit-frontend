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
import models.{InternationalAddress, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.applicant._

class ChangingDesignatoryDetailsJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private def ukAddress = arbitrary[UkAddress].sample.value
  private def internationalAddress = arbitrary[InternationalAddress].sample.value

  "when the user originally gave a new UK designatory address" - {

    "changing to say it is international must collect the new address, remove the UK address, and return to Check Applicant" in {

      val initialise = journeyOf(
        setUserAnswerTo(DesignatoryAddressInUkPage, true),
        setUserAnswerTo(DesignatoryUkAddressPage, ukAddress),
        setUserAnswerTo(CheckDesignatoryDetailsPage, true),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(CheckApplicantDetailsPage)
        .run(
          initialise,
          goToChangeAnswer(DesignatoryAddressInUkPage),
          submitAnswer(DesignatoryAddressInUkPage, false),
          submitAnswer(DesignatoryInternationalAddressPage, internationalAddress),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(DesignatoryUkAddressPage)
        )
    }
  }

  "when the user originally gave a new international designatory address" - {

    "changing to say it is in the UK must collect the new address, remove the international address, and return to Check Applicant" in {

      val initialise = journeyOf(
        setUserAnswerTo(DesignatoryAddressInUkPage, false),
        setUserAnswerTo(DesignatoryInternationalAddressPage, internationalAddress),
        setUserAnswerTo(CheckDesignatoryDetailsPage, true),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(CheckApplicantDetailsPage)
        .run(
          initialise,
          goToChangeAnswer(DesignatoryAddressInUkPage),
          submitAnswer(DesignatoryAddressInUkPage, true),
          submitAnswer(DesignatoryUkAddressPage, ukAddress),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(DesignatoryInternationalAddressPage)
        )
    }
  }

  "when the user originally gave a new UK correspondence address" - {

    "changing to say it is international must collect the new address, remove the UK address, and return to Check Applicant" in {

      val initialise = journeyOf(
        setUserAnswerTo(CorrespondenceAddressInUkPage, true),
        setUserAnswerTo(CorrespondenceUkAddressPage, ukAddress),
        setUserAnswerTo(CheckDesignatoryDetailsPage, true),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(CheckApplicantDetailsPage)
        .run(
          initialise,
          goToChangeAnswer(CorrespondenceAddressInUkPage),
          submitAnswer(CorrespondenceAddressInUkPage, false),
          submitAnswer(CorrespondenceInternationalAddressPage, internationalAddress),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(CorrespondenceUkAddressPage)
        )
    }
  }

  "when the user originally gave a new international correspondence address" - {

    "changing to say it is in the UK must collect the new address, remove the international address, and return to Check Applicant" in {

      val initialise = journeyOf(
        setUserAnswerTo(CorrespondenceAddressInUkPage, false),
        setUserAnswerTo(CorrespondenceInternationalAddressPage, internationalAddress),
        setUserAnswerTo(CheckDesignatoryDetailsPage, true),
        goTo(CheckApplicantDetailsPage)
      )

      startingFrom(CheckApplicantDetailsPage)
        .run(
          initialise,
          goToChangeAnswer(CorrespondenceAddressInUkPage),
          submitAnswer(CorrespondenceAddressInUkPage, true),
          submitAnswer(CorrespondenceUkAddressPage, ukAddress),
          pageMustBe(CheckApplicantDetailsPage),
          answersMustNotContain(CorrespondenceInternationalAddressPage)
        )
    }
  }
}
