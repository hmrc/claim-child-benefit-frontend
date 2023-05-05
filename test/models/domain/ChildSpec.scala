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

import models.ChildBirthRegistrationCountry._
import models.{BirthCertificateSystemNumber, BirthRegistrationMatchingResult, NorthernIrishBirthCertificateNumber, ScottishBirthCertificateDetails, journey}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class ChildSpec extends AnyFreeSpec with Matchers with OptionValues {

  ".build" - {

      val basicChild = journey.Child(
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
        dateOfBirthVerified = None,
        livingWithClaimant = true,
        claimantIsParent = true,
        adoptionStatus = false
      )
    }

    "must set `living with claimant` to false when the child has a guardian" in {

      val child = basicChild.copy(guardian = Some(journey.Guardian(None, None)))

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

      val child = basicChild.copy(relationshipToApplicant = models.ApplicantRelationshipToChild.OtherRelationship)

      val result = Child.build(child)

      result.claimantIsParent mustEqual false
    }

    "must set `claimant is parent` to true when the relationship is `birth child`" in {

      val child = basicChild.copy(relationshipToApplicant = models.ApplicantRelationshipToChild.BirthChild)

      val result = Child.build(child)

      result.claimantIsParent mustEqual true
    }

    "must set `claimant is parent` to true when the relationship is `stepchild`" in {

      val child = basicChild.copy(relationshipToApplicant = models.ApplicantRelationshipToChild.StepChild)

      val result = Child.build(child)

      result.claimantIsParent mustEqual true
    }

    "must set `claimant is parent` to true when the relationship is `adopted child`" in {

      val child = basicChild.copy(relationshipToApplicant = models.ApplicantRelationshipToChild.AdoptedChild)

      val result = Child.build(child)

      result.claimantIsParent mustEqual true
    }

    "must set `date of birth verified` to None when the child is registered in England" in {

      val child = basicChild.copy(countryOfRegistration = models.ChildBirthRegistrationCountry.England)

      val result = Child.build(child)

      result.dateOfBirthVerified must not be defined
    }

    "must set `date of birth verified` to None when the child is registered in Wales" in {

      val child = basicChild.copy(countryOfRegistration = models.ChildBirthRegistrationCountry.Wales)

      val result = Child.build(child)

      result.dateOfBirthVerified must not be defined
    }

    "must set `date of birth verified` to None when the child is registered in Scotland" in {

      val child = basicChild.copy(countryOfRegistration = models.ChildBirthRegistrationCountry.Scotland)

      val result = Child.build(child)

      result.dateOfBirthVerified must not be defined
    }

    "must set `date of birth verified` to `true` when the child is registered in Northern Ireland and a birth cert number is present" in {

      val child = basicChild.copy(
        countryOfRegistration = models.ChildBirthRegistrationCountry.NorthernIreland,
        birthCertificateNumber = Some(NorthernIrishBirthCertificateNumber("B12345678"))
      )

      val result = Child.build(child)

      result.dateOfBirthVerified.value mustBe true
    }

    "must set `date of birth verified` to `false` when the child is registered in Northern Ireland and no birth cert number is present" in {

      val child = basicChild.copy(countryOfRegistration = models.ChildBirthRegistrationCountry.NorthernIreland)

      val result = Child.build(child)

      result.dateOfBirthVerified.value mustBe false
    }

    "must set `date of birth verified` to `false` when the child is registered in another country" in {

      val child = basicChild.copy(countryOfRegistration = models.ChildBirthRegistrationCountry.OtherCountry)

      val result = Child.build(child)

      result.dateOfBirthVerified.value mustBe false
    }

    "must set `date of birth verified` to `false` when the child's birth registration country is Unknown" in {

      val child = basicChild.copy(countryOfRegistration = models.ChildBirthRegistrationCountry.UnknownCountry)

      val result = Child.build(child)

      result.dateOfBirthVerified.value mustBe false
    }

    "must include birth certificate for a child born in England when it is present" in {

      val child = basicChild.copy(birthCertificateNumber = Some(BirthCertificateSystemNumber("123456789")))

      val result = Child.build(child)

      result.birthRegistrationNumber.value mustEqual "123456789"
    }

    "must include birth certificate number for a child born in Wales when it is present" in {

      val child = basicChild.copy(
        birthCertificateNumber = Some(BirthCertificateSystemNumber("123456789")),
        countryOfRegistration = Wales
      )

      val result = Child.build(child)

      result.birthRegistrationNumber.value mustEqual "123456789"
    }

    "must include birth certificate number for a child born in Scotland when it is present" in {

      val child = basicChild.copy(
        birthCertificateNumber = Some(ScottishBirthCertificateDetails(123, 2022, 456)),
        countryOfRegistration = Scotland
      )

      val result = Child.build(child)

      result.birthRegistrationNumber.value mustEqual "2022123456"
    }

    "must not include birth certificate number for a child born in Northern Ireland when it is present" in {

      val child = basicChild.copy(
        birthCertificateNumber = Some(NorthernIrishBirthCertificateNumber("B12345678")),
        countryOfRegistration = NorthernIreland
      )

      val result = Child.build(child)

      result.birthRegistrationNumber must not be defined
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
        dateOfBirthVerified = None,
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
        "livingWithClaimant" -> true,
        "claimantIsParent" -> true,
        "adoptionStatus" -> false
      )

      Json.toJson(child) mustEqual expectedJson
    }
  }
}
