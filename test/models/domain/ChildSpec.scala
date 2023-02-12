/*
 * Copyright 2023 HM Revenue & Customs
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

package models.domain

import models.{BirthRegistrationMatchingResult, JourneyModel}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class ChildSpec extends AnyFreeSpec with Matchers {

  ".build" - {

      val basicChild = JourneyModel.Child(
        name = models.ChildName("first", None, "last"),
        nameChangedByDeedPoll = None,
        previousNames = Nil,
        biologicalSex = models.ChildBiologicalSex.Female,
        dateOfBirth = LocalDate.now,
        countryOfRegistration = models.ChildBirthRegistrationCountry.England,
        birthCertificateNumber = None,
        birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
        relationshipToApplicant = models.ApplicantRelationshipToChild.BirthChild,
        adoptingThroughLocalAuthority = false,
        previousClaimant = None,
        guardian = None,
        previousGuardian = None,
        dateChildStartedLivingWithApplicant = None
      )

    "must return a Child" in {

      val result = Child.build(basicChild)

      result mustEqual Child(
        name = ChildName("first", None, "last"),
        gender = BiologicalSex.Female,
        dateOfBirth = LocalDate.now,
        birthRegistrationNumber = None,
        crn = None,
        countryOfRegistration = CountryOfRegistration.EnglandWales,
        dateOfBirthVerified = false,
        livingWithClaimant = true,
        claimantIsParent = true,
        adoptionStatus = false
      )
    }

    "must set `living with claimant` to false when the child has a guardian" in {

      val child = basicChild.copy(guardian = Some(JourneyModel.Guardian(None, None)))

      val result = Child.build(child)

      result.livingWithClaimant mustEqual false
    }

    "must set `adoption status` to true when the child is adopted" in {

      val child = basicChild.copy(relationshipToApplicant = models.ApplicantRelationshipToChild.AdoptedChild)

      val result = Child.build(child)

      result.adoptionStatus mustEqual true
    }

    "must set `adoption status` to true when the applicant is adopting the child" in {

      val child = basicChild.copy(adoptingThroughLocalAuthority = true)

      val result = Child.build(child)

      result.adoptionStatus mustEqual true
    }

    "must set `claimant is parent` to false when the relationship is `other`" in {

      val child = basicChild.copy(relationshipToApplicant = models.ApplicantRelationshipToChild.Other)

      val result = Child.build(child)

      result.claimantIsParent mustEqual false
    }
  }

  ".writes" - {

    "must write a child" in {

      val dateOfBirth = LocalDate.of(2022, 1, 2)
      val child = Child(
        name = ChildName("first", Some("middle"), "last"),
        gender = BiologicalSex.Female,
        dateOfBirth = dateOfBirth,
        birthRegistrationNumber = Some("123456789"),
        crn = None,
        countryOfRegistration = CountryOfRegistration.EnglandWales,
        dateOfBirthVerified = false,
        livingWithClaimant = true,
        claimantIsParent = true,
        adoptionStatus = false
      )

      val expectedJson = Json.obj(
        "name" -> Json.obj(
          "forenames" -> "first",
          "middleNames" -> "middle",
          "surname" -> "last"
        ),
        "gender" -> "FEMALE",
        "dateOfBirth" -> "02/01/2022",
        "birthRegistrationNumber" -> "123456789",
        "countryOfRegistration" -> "ENGLAND_WALES",
        "dateOfBirthVerified" -> false,
        "livingWithClaimant" -> true,
        "claimantIsParent" -> true,
        "adoptionStatus" -> false
      )

      Json.toJson(child) mustEqual expectedJson
    }
  }
}
