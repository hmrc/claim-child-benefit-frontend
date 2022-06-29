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
import models.ChildBirthRegistrationCountry.{England, Other, Scotland, Unknown, Wales}
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.RelationshipStatusPage
import pages.child._

import java.time.LocalDate

class ChildJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users whose child has no previous names must not be asked for them" in {

    val childName = ChildName("first", None, "last")
    val sex = arbitrary[ChildBiologicalSex].sample.value

    startingFrom(ChildNamePage(Index(0)))
      .run(
        answerPage(ChildNamePage(Index(0)), childName, ChildHasPreviousNamePage(Index(0))),
        answerPage(ChildHasPreviousNamePage(Index(0)), false, ChildBiologicalSexPage(Index(0))),
        answerPage(ChildBiologicalSexPage(Index(0)), sex, ChildDateOfBirthPage(Index(0))),
        answerPage(ChildDateOfBirthPage(Index(0)), LocalDate.now, ChildBirthRegistrationCountryPage(Index(0)))
      )
  }

  "users whose child has previous names must be asked for as many as necessary" in {

    val childName = ChildName("first", None, "last")

    startingFrom(ChildNamePage(Index(0)))
      .run(
        answerPage(ChildNamePage(Index(0)), childName, ChildHasPreviousNamePage(Index(0))),
        answerPage(ChildHasPreviousNamePage(Index(0)), true, ChildNameChangedByDeedPollPage(Index(0))),
        answerPage(ChildNameChangedByDeedPollPage(Index(0)), true, ChildPreviousNamePage(Index(0), Index(0))),
        answerPage(ChildPreviousNamePage(Index(0), Index(0)), childName, AddChildPreviousNamePage(Index(0))),
        answerPage(AddChildPreviousNamePage(Index(0)), true, ChildPreviousNamePage(Index(0), Index(1))),
        answerPage(ChildPreviousNamePage(Index(0), Index(1)), childName, AddChildPreviousNamePage(Index(0))),
        answerPage(AddChildPreviousNamePage(Index(0)), false, ChildBiologicalSexPage(Index(0)))
      )
  }

  "users whose child has previous names must be able to remove them" in {

    val childName = ChildName("first", None, "last")

    startingFrom(ChildNamePage(Index(0)))
      .run(
        answerPage(ChildNamePage(Index(0)), childName, ChildHasPreviousNamePage(Index(0))),
        answerPage(ChildHasPreviousNamePage(Index(0)), true, ChildNameChangedByDeedPollPage(Index(0))),
        answerPage(ChildNameChangedByDeedPollPage(Index(0)), true, ChildPreviousNamePage(Index(0), Index(0))),
        answerPage(ChildPreviousNamePage(Index(0), Index(0)), childName, AddChildPreviousNamePage(Index(0))),
        answerPage(AddChildPreviousNamePage(Index(0)), true, ChildPreviousNamePage(Index(0), Index(1))),
        answerPage(ChildPreviousNamePage(Index(0), Index(1)), childName, AddChildPreviousNamePage(Index(0))),
        goTo(RemoveChildPreviousNamePage(Index(0), Index(1))),
        remove(ChildPreviousNamePage(Index(0), Index(1))),
        next,
        pageMustBe(AddChildPreviousNamePage(Index(0))),
        goTo(RemoveChildPreviousNamePage(Index(0), Index(0))),
        remove(ChildPreviousNamePage(Index(0), Index(0))),
        next,
        pageMustBe(ChildHasPreviousNamePage(Index(0))),
        answersMustNotContain(ChildNameChangedByDeedPollPage(Index(0)))
      )
  }

  "users whose child was registered in England" - {

    "must be asked for the birth certificate system number, and not for any documents" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          answerPage(ChildBirthRegistrationCountryPage(Index(0)), England, ChildBirthCertificateSystemNumberPage(Index(0))),
          answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), "123456789", ApplicantRelationshipToChildPage(Index(0))),
          answerPage(ApplicantRelationshipToChildPage(Index(0)), relationship, AnyoneClaimedForChildBeforePage(Index(0))),
          answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.No, AdoptingChildPage(Index(0))),
          answerPage(AdoptingChildPage(Index(0)), false, CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child was registered in Wales" - {

    "must be asked for the birth certificate system number, and not for any documents" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          answerPage(ChildBirthRegistrationCountryPage(Index(0)), Wales, ChildBirthCertificateSystemNumberPage(Index(0))),
          answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), "123456789", ApplicantRelationshipToChildPage(Index(0))),
          answerPage(ApplicantRelationshipToChildPage(Index(0)), relationship, AnyoneClaimedForChildBeforePage(Index(0))),
          answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.No, AdoptingChildPage(Index(0))),
          answerPage(AdoptingChildPage(Index(0)), false, CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child was registered in Scotland" - {

    "must be asked for the birth certificate details, and not for any documents" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value
      val certificateDetails = arbitrary[ChildScottishBirthCertificateDetails].sample.value

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          answerPage(ChildBirthRegistrationCountryPage(Index(0)), Scotland, ChildScottishBirthCertificateDetailsPage(Index(0))),
          answerPage(ChildScottishBirthCertificateDetailsPage(Index(0)), certificateDetails, ApplicantRelationshipToChildPage(Index(0))),
          answerPage(ApplicantRelationshipToChildPage(Index(0)), relationship, AnyoneClaimedForChildBeforePage(Index(0))),
          answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.No, AdoptingChildPage(Index(0))),
          answerPage(AdoptingChildPage(Index(0)), false, CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child has been claimed for before" - {

    "by them" - {

      "must not be asked for details of the previous claimant" in {

        val relationshipStatus = arbitrary[RelationshipStatus].sample.value

        val answers =
          UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value

        startingFrom(AnyoneClaimedForChildBeforePage(Index(0)), answers = answers)
          .run(
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.Applicant, AdoptingChildPage(Index(0)))
          )
      }
    }

    "by their partner" - {

      "must not be asked for details of the previous claimant" in {

        val relationshipStatus = Gen.oneOf(RelationshipStatus.Married, RelationshipStatus.Cohabiting).sample.value

        val answers =
          UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value

        startingFrom(AnyoneClaimedForChildBeforePage(Index(0)), answers = answers)
          .run(
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.Partner, AdoptingChildPage(Index(0)))
          )
      }
    }

    "by someone other than them or their partner" - {

      "must be asked for details of the previous claimant" in {

        val relationshipStatus = arbitrary[RelationshipStatus].sample.value
        val claimantName       = PreviousClaimantName(None, "first", None, "last")
        val claimantAddress    = Address("line 1", None, "town", None, "postcode")

        val answers =
          UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value

        startingFrom(AnyoneClaimedForChildBeforePage(Index(0)), answers = answers)
          .run(
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.SomeoneElse, PreviousClaimantNamePage(Index(0))),
            answerPage(PreviousClaimantNamePage(Index(0)), claimantName, PreviousClaimantAddressPage(Index(0))),
            answerPage(PreviousClaimantAddressPage(Index(0)), claimantAddress, AdoptingChildPage(Index(0)))
          )
      }
    }
  }

  "users whose child was registered outside of Great Britain" - {

    "must be asked for documents" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value
      val documents = Set(arbitrary[IncludedDocuments].sample.value)

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          answerPage(ChildBirthRegistrationCountryPage(Index(0)), Other, ApplicantRelationshipToChildPage(Index(0))),
          answerPage(ApplicantRelationshipToChildPage(Index(0)), relationship, AnyoneClaimedForChildBeforePage(Index(0))),
          answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.No, AdoptingChildPage(Index(0))),
          answerPage(AdoptingChildPage(Index(0)), false, IncludedDocumentsPage(Index(0))),
          answerPage(IncludedDocumentsPage(Index(0)), documents, CheckChildDetailsPage(Index(0)))
        )
    }
  }

  "users whose child's country of registration is unknown" - {

    "must be asked for documents" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value
      val documents = Set(arbitrary[IncludedDocuments].sample.value)

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          answerPage(ChildBirthRegistrationCountryPage(Index(0)), Unknown, ApplicantRelationshipToChildPage(Index(0))),
          answerPage(ApplicantRelationshipToChildPage(Index(0)), relationship, AnyoneClaimedForChildBeforePage(Index(0))),
          answerPage(AnyoneClaimedForChildBeforePage(Index(0)), AnyoneClaimedForChildBefore.No, AdoptingChildPage(Index(0))),
          answerPage(AdoptingChildPage(Index(0)), false, IncludedDocumentsPage(Index(0))),
          answerPage(IncludedDocumentsPage(Index(0)), documents, CheckChildDetailsPage(Index(0)))
        )
    }
  }
}