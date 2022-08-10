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

package journey

import generators.ModelGenerators
import models.RelationshipStatus._
import models.{Address, EmploymentStatus, BestTimeToContact, Index, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.RelationshipStatusPage
import pages.applicant._
import pages.child.ChildNamePage
import pages.partner.PartnerNamePage
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ApplicantJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users without any previous names or previous addresses, who do not know their NINO" - {

    "must be asked all of the applicant questions" in {

      val address = Address("line 1", None, "town", None, "postcode")

      startingFrom(ApplicantHasPreviousFamilyNamePage)
        .run(
          submitAnswer(ApplicantHasPreviousFamilyNamePage, false),
          submitAnswer(ApplicantNinoKnownPage, false),
          submitAnswer(ApplicantDateOfBirthPage, LocalDate.now),
          submitAnswer(ApplicantCurrentAddressPage, address),
          submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, true),
          submitAnswer(ApplicantPhoneNumberPage, "07777 7777777"),
          submitAnswer(BestTimeToContactPage, Set[BestTimeToContact](BestTimeToContact.Morning)),
          submitAnswer(ApplicantNationalityPage, "nationality"),
          pageMustBe(ApplicantEmploymentStatusPage)
        )
    }
  }

  "users who know their NINO must be asked for it" in {

    val nino = arbitrary[Nino].sample.value

    startingFrom(ApplicantNinoKnownPage)
      .run(
        submitAnswer(ApplicantNinoKnownPage, true),
        submitAnswer(ApplicantNinoPage, nino),
        pageMustBe(ApplicantDateOfBirthPage)
      )
  }

  "users with previous names must be asked for as many as necessary" in {

    startingFrom(ApplicantHasPreviousFamilyNamePage)
      .run(
        submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
        submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), "name"),
        submitAnswer(AddApplicantPreviousFamilyNamePage, true),
        submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), "name"),
        submitAnswer(AddApplicantPreviousFamilyNamePage, false),
        pageMustBe(ApplicantNinoKnownPage)
      )
  }

  "users with previous names must be able to remove them" in {

    startingFrom(ApplicantHasPreviousFamilyNamePage)
      .run(
        submitAnswer(ApplicantHasPreviousFamilyNamePage, true),
        submitAnswer(ApplicantPreviousFamilyNamePage(Index(0)), "name"),
        submitAnswer(AddApplicantPreviousFamilyNamePage, true),
        submitAnswer(ApplicantPreviousFamilyNamePage(Index(1)), "name"),
        goTo(RemoveApplicantPreviousFamilyNamePage(Index(1))),
        removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(1))),
        pageMustBe(AddApplicantPreviousFamilyNamePage),
        goTo(RemoveApplicantPreviousFamilyNamePage(Index(0))),
        removeAddToListItem(ApplicantPreviousFamilyNamePage(Index(0))),
        pageMustBe(ApplicantHasPreviousFamilyNamePage)
      )
  }

  "users who need to give a previous address must be asked for it" in {

    val address = Address("line 1", None, "town", None, "postcode")

    startingFrom(ApplicantLivedAtCurrentAddressOneYearPage)
      .run(
        submitAnswer(ApplicantLivedAtCurrentAddressOneYearPage, false),
        submitAnswer(ApplicantPreviousAddressPage, address),
        pageMustBe(ApplicantPhoneNumberPage)
      )
  }

  "users proceeding from Applicant Employment Status" - {

    "must go to Partner Name if they are Married" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Married).success.value
      val employmentStatus = Set(arbitrary[EmploymentStatus].sample.value)

      startingFrom(ApplicantEmploymentStatusPage, answers = answers)
        .run(
          submitAnswer(ApplicantEmploymentStatusPage, employmentStatus),
          pageMustBe(PartnerNamePage)
        )
    }

    "must go to Partner Name if they are Cohabiting" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Cohabiting).success.value
      val employmentStatus = Set(arbitrary[EmploymentStatus].sample.value)

      startingFrom(ApplicantEmploymentStatusPage, answers = answers)
        .run(
          submitAnswer(ApplicantEmploymentStatusPage, employmentStatus),
          pageMustBe(PartnerNamePage)
        )
    }

    "must go to Child Name (for index 0) if they are Single" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Single).success.value
      val employmentStatus = Set(arbitrary[EmploymentStatus].sample.value)

      startingFrom(ApplicantEmploymentStatusPage, answers = answers)
        .run(
          submitAnswer(ApplicantEmploymentStatusPage, employmentStatus),
          pageMustBe(ChildNamePage(Index(0)))
        )
    }

    "must go to Child Name (for index 0) if they are Separated" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Separated).success.value
      val employmentStatus = Set(arbitrary[EmploymentStatus].sample.value)

      startingFrom(ApplicantEmploymentStatusPage, answers = answers)
        .run(
          submitAnswer(ApplicantEmploymentStatusPage, employmentStatus),
          pageMustBe(ChildNamePage(Index(0)))
      )
    }

    "must go to Child Name (for index 0) if they are Divorced" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Divorced).success.value
      val employmentStatus = Set(arbitrary[EmploymentStatus].sample.value)

      startingFrom(ApplicantEmploymentStatusPage, answers = answers)
        .run(
          submitAnswer(ApplicantEmploymentStatusPage, employmentStatus),
          pageMustBe(ChildNamePage(Index(0)))
        )
    }

    "must go to Child Name (for index 0) if they are Widowed" in {

      val answers = UserAnswers("id").set(RelationshipStatusPage, Widowed).success.value
      val employmentStatus = Set(arbitrary[EmploymentStatus].sample.value)

      startingFrom(ApplicantEmploymentStatusPage, answers = answers)
        .run(
          submitAnswer(ApplicantEmploymentStatusPage, employmentStatus),
          pageMustBe(ChildNamePage(Index(0)))
        )
    }
  }
}
