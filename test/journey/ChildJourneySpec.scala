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
import models.ChildBirthRegistrationCountry.{England, NorthernIreland, Other, Scotland, Unknown, Wales}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.child._

import java.time.LocalDate

class ChildJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users whose child has no previous names must not be asked for them" in {

    val childName = ChildName("first", None, "last")
    val sex = arbitrary[ChildBiologicalSex].sample.value

    startingFrom(ChildNamePage(Index(0)))
      .run(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
        submitAnswer(ChildBiologicalSexPage(Index(0)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(0)), LocalDate.now),
        pageMustBe(ChildBirthRegistrationCountryPage(Index(0)))
      )
  }

  "users whose child has previous names must be asked for as many as necessary" in {

    val childName = ChildName("first", None, "last")

    startingFrom(ChildNamePage(Index(0)))
      .run(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), true),
        submitAnswer(ChildNameChangedByDeedPollPage(Index(0)), true),
        submitAnswer(ChildPreviousNamePage(Index(0), Index(0)), childName),
        submitAnswer(AddChildPreviousNamePage(Index(0)), true),
        submitAnswer(ChildPreviousNamePage(Index(0), Index(1)), childName),
        submitAnswer(AddChildPreviousNamePage(Index(0)), false),
        pageMustBe(ChildBiologicalSexPage(Index(0)))
      )
  }

  "users whose child has previous names must be able to remove them" in {

    val childName = ChildName("first", None, "last")

    startingFrom(ChildNamePage(Index(0)))
      .run(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), true),
        submitAnswer(ChildNameChangedByDeedPollPage(Index(0)), true),
        submitAnswer(ChildPreviousNamePage(Index(0), Index(0)), childName),
        submitAnswer(AddChildPreviousNamePage(Index(0)), true),
        submitAnswer(ChildPreviousNamePage(Index(0), Index(1)), childName),
        goTo(RemoveChildPreviousNamePage(Index(0), Index(1))),
        removeAddToListItem(ChildPreviousNamePage(Index(0), Index(1))),
        pageMustBe(AddChildPreviousNamePage(Index(0))),
        goTo(RemoveChildPreviousNamePage(Index(0), Index(0))),
        removeAddToListItem(ChildPreviousNamePage(Index(0), Index(0))),
        pageMustBe(ChildHasPreviousNamePage(Index(0))),
        answersMustNotContain(ChildNameChangedByDeedPollPage(Index(0)))
      )
  }

  "users whose child was registered in England" - {

    "must be asked for the birth certificate system number, and not for any documents" in {

      val relationship = ApplicantRelationshipToChild.BirthChild

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
          submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), "123456789"),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child was registered in Wales" - {

    "must be asked for the birth certificate system number, and not for any documents" in {

      val relationship = ApplicantRelationshipToChild.BirthChild

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
          submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), "123456789"),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child was registered in Scotland" - {

    "must be asked for the birth certificate details, and not for any documents" in {

      val relationship       = ApplicantRelationshipToChild.BirthChild
      val certificateDetails =Gen.listOfN(10, Gen.numChar).sample.value.mkString

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
          submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), certificateDetails),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child was registered in Northern Ireland" - {

    "must be asked for documents" in {

      val relationship = ApplicantRelationshipToChild.BirthChild
      val documents    = Set(arbitrary[IncludedDocuments].sample.value)

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          submitAnswer(IncludedDocumentsPage(Index(0)), documents),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child was registered outside of the UK" - {

    "must be asked for documents" in {

      val relationship = ApplicantRelationshipToChild.BirthChild
      val documents    = Set(arbitrary[IncludedDocuments].sample.value)

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          submitAnswer(IncludedDocumentsPage(Index(0)), documents),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child's country of registration is unknown" - {

    "must be asked for documents" in {

      val relationship = ApplicantRelationshipToChild.BirthChild
      val documents    = Set(arbitrary[IncludedDocuments].sample.value)

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          submitAnswer(IncludedDocumentsPage(Index(0)), documents),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose relationship to the child is Adopting" - {

    "must be asked if they are adopting through a local authority" in {

      val relationship = ApplicantRelationshipToChild.AdoptingChild

      startingFrom(ApplicantRelationshipToChildPage(Index(0)))
        .run(
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), true),
          pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
        )
    }
  }

  "users whose relationship to the child is Birth Child, Adopted Child, Step Child or Other" - {

    "must not be asked if they are adopting through a local authority" in {

      val relationship = Gen.oneOf(
        ApplicantRelationshipToChild.BirthChild,
        ApplicantRelationshipToChild.AdoptedChild,
        ApplicantRelationshipToChild.StepChild,
        ApplicantRelationshipToChild.Other
      ).sample.value

      startingFrom(ApplicantRelationshipToChildPage(Index(0)))
        .run(
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
        )
    }
  }

  "users whose child has been claimed for before" - {

    "must be asked for details of the previous claimant" - {

      "when their country of birth registration means we do not need documents" in {

        val country      = Gen.oneOf(England, Wales, Scotland).sample.value
        val claimantName = AdultName(None, "first", None, "last")
        val claimantAddress = Address("line 1", None, "town", None, "postcode")

        val initialise = journeyOf(
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), country)
        )

        startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
          .run(
            initialise,
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
            submitAnswer(PreviousClaimantNamePage(Index(0)), claimantName),
            submitAnswer(PreviousClaimantAddressPage(Index(0)), claimantAddress),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "when their country of birth registration means we  need documents" in {

        val country         = Gen.oneOf(Other, Unknown).sample.value
        val claimantName    = AdultName(None, "first", None, "last")
        val claimantAddress = Address("line 1", None, "town", None, "postcode")
        val documents       = Set(arbitrary[IncludedDocuments].sample.value)

        val initialise = journeyOf(
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), country)
        )

        startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
          .run(
            initialise,
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
            submitAnswer(PreviousClaimantNamePage(Index(0)), claimantName),
            submitAnswer(PreviousClaimantAddressPage(Index(0)), claimantAddress),
            submitAnswer(IncludedDocumentsPage(Index(0)), documents),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }
  }
}