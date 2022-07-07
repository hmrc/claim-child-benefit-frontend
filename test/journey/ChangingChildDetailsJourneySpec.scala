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
import models.{ApplicantRelationshipToChild => Relationship}
import models.ChildBirthRegistrationCountry._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.CheckYourAnswersPage
import pages.child._
import queries.ChildQuery

import java.time.LocalDate

class ChangingChildDetailsJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

    private val childName               = arbitrary[ChildName].sample.value
    private val sex                     = arbitrary[ChildBiologicalSex].sample.value
    private val systemNumber            = Gen.listOfN(9, Gen.numChar).sample.value.mkString
    private val claimantName            = arbitrary[PreviousClaimantName].sample.value
    private val claimantAddress         = arbitrary[Address].sample.value
    private val scottishBcDetails       = arbitrary[ChildScottishBirthCertificateDetails].sample.value
    private val includedDocuments       = Set(arbitrary[IncludedDocuments].sample.value)
    private val notAdoptingRelationship = Gen.oneOf(Relationship.BirthChild, Relationship.StepChild, Relationship.AdoptedChild, Relationship.Other).sample.value

  "when a user has added a child" - {

    val basicChildJourney =
      journeyOf(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
        submitAnswer(ChildBiologicalSexPage(Index(0)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(0)), LocalDate.now),
        submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
        submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
        submitAnswer(ApplicantRelationshipToChildPage(Index(0)), notAdoptingRelationship),
        submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false)
      )

    "that no one has claimed Child Benefit for previously" - {

      "changing `Anyone claimed before` to `true` must gather previous claimant details then return the user to the Check page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
            submitAnswer(PreviousClaimantNamePage(Index(0)), claimantName),
            submitAnswer(PreviousClaimantAddressPage(Index(0)), claimantAddress),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that someone has claimed Child Benefit for previously" - {

        val initialState =
          journeyOf(
              basicChildJourney,
              setUserAnswerTo(AnyoneClaimedForChildBeforePage(Index(0)), true),
              setUserAnswerTo(PreviousClaimantNamePage(Index(0)), claimantName),
              setUserAnswerTo(PreviousClaimantAddressPage(Index(0)), claimantAddress)
            )

      "changing `Anyone claimed before` to `false` must remove the previous claimant details and return the user to the Check page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(PreviousClaimantNamePage(Index(0))),
            answersMustNotContain(PreviousClaimantAddressPage(Index(0)))
          )
      }
    }

    "that the user said was registered in England" - {

      "changing the country to Wales must return the user to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Scotland must remove Birth Certificate System Number, collect Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Other must remove Birth Certificate System Number, collect Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
            submitAnswer(IncludedDocumentsPage(Index(0)), includedDocuments),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to unknown must remove Birth Certificate System Number, collect Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
            submitAnswer(IncludedDocumentsPage(Index(0)), includedDocuments),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }
    }

    "that the user said was registered in Wales" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Wales)
        )

      "changing the country to England must return the user to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Scotland must remove Birth Certificate System Number, collect Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Other must remove Birth Certificate System Number, collect Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
            submitAnswer(IncludedDocumentsPage(Index(0)), includedDocuments),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to unknown must remove Birth Certificate System Number, collect Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
            submitAnswer(IncludedDocumentsPage(Index(0)), includedDocuments),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }
    }

    "that the user said was registered in Scotland" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
          setUserAnswerTo(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System Number, remove Scottish Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System Number, remove Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Other must collect Included Documents, remove Scottish  Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
            submitAnswer(IncludedDocumentsPage(Index(0)), includedDocuments),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to unknown must collect Included Documents, remove Scottish Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
            submitAnswer(IncludedDocumentsPage(Index(0)), includedDocuments),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }
    }

    "that the user said was registered in another country" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Other),
          setUserAnswerTo(IncludedDocumentsPage(Index(0)), includedDocuments),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Scotland must collect Scottish Details, remove Included Documents, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Unknown must return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that the user said was registered in an unknown country" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
          setUserAnswerTo(IncludedDocumentsPage(Index(0)), includedDocuments),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Scotland must collect Scottish Details, remove Included Documents, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Other must return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that the user said had no previous names" - {

      "changing that answer must collect whether the name was changed by deed poll, and the name(s)" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildHasPreviousNamePage(Index(0))),
            submitAnswer(ChildHasPreviousNamePage(Index(0)), true),
            submitAnswer(ChildNameChangedByDeedPollPage(Index(0)), true),
            submitAnswer(ChildPreviousNamePage(Index(0), Index(0)), childName),
            submitAnswer(AddChildPreviousNamePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that they provided previous names for" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildHasPreviousNamePage(Index(0)), true),
          setUserAnswerTo(ChildNameChangedByDeedPollPage(Index(0)), true),
          setUserAnswerTo(ChildPreviousNamePage(Index(0), Index(0)), childName),
          setUserAnswerTo(ChildPreviousNamePage(Index(0), Index(1)), childName),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )

      "changing to say they had no previous names must remove all names, and whether the name was changed by deed poll, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildHasPreviousNamePage(Index(0))),
            submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildNameChangedByDeedPollPage(Index(0))),
            answersMustNotContain(ChildPreviousNamePage(Index(0), Index(0))),
            answersMustNotContain(ChildPreviousNamePage(Index(0), Index(1)))
          )
      }

      "must allow the user to remove a name, leaving at least one" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AddChildPreviousNamePage(Index(0))),
            goTo(RemoveChildPreviousNamePage(Index(0), Index(1))),
            removeAddToListItem(ChildPreviousNamePage(Index(0), Index(1))),
            pageMustBe(AddChildPreviousNamePage(Index(0))),
            submitAnswer(AddChildPreviousNamePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "removing the last name must also remove whether the name was changed by deed poll, and go to Child Has Previous Name" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AddChildPreviousNamePage(Index(0))),
            goTo(RemoveChildPreviousNamePage(Index(0), Index(1))),
            removeAddToListItem(ChildPreviousNamePage(Index(0), Index(1))),
            pageMustBe(AddChildPreviousNamePage(Index(0))),
            goTo(RemoveChildPreviousNamePage(Index(0), Index(0))),
            removeAddToListItem(ChildPreviousNamePage(Index(0), Index(0))),
            pageMustBe(ChildHasPreviousNamePage(Index(0))),
            answersMustNotContain(ChildNameChangedByDeedPollPage(Index(0))),
            pageMustBe(ChildHasPreviousNamePage(Index(0)))
          )
      }

      "must allow the user to add another previous name" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AddChildPreviousNamePage(Index(0))),
            submitAnswer(AddChildPreviousNamePage(Index(0)), true),
            submitAnswer(ChildPreviousNamePage(Index(0), Index(2)), childName),
            submitAnswer(AddChildPreviousNamePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "must allow the user to change a name" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AddChildPreviousNamePage(Index(0))),
            goToChangeAnswer(ChildPreviousNamePage(Index(0), Index(0))),
            submitAnswer(ChildPreviousNamePage(Index(0), Index(0)), childName),
            submitAnswer(AddChildPreviousNamePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that they were not adopting or planning to adopt" - {

      "changing the relationship to `adopting or planning to adopt` must ask if they are adopting through a LA, then return to check child details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ApplicantRelationshipToChildPage(Index(0))),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), Relationship.AdoptingChild),
            submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), true),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that they were adopting or planning to adopt" - {

      "changing the relationship to anything else must remove if they are adopting through a LA, then return to check child details" in {

        val initialise = journeyOf(
          basicChildJourney,
          setUserAnswerTo(ApplicantRelationshipToChildPage(Index(0)), Relationship.AdoptingChild),
          setUserAnswerTo(AdoptingThroughLocalAuthorityPage(Index(0)), false),
          goTo(CheckChildDetailsPage(Index(0)))
        )

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialise,
            goToChangeAnswer(ApplicantRelationshipToChildPage(Index(0))),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), notAdoptingRelationship),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(AdoptingThroughLocalAuthorityPage(Index(0)))
          )
      }
    }
  }
  
  "when a user has added multiple children" - {

    val initialise =
      journeyOf(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
        submitAnswer(ChildBiologicalSexPage(Index(0)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(0)), LocalDate.now),
        submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
        submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
        submitAnswer(ApplicantRelationshipToChildPage(Index(0)), notAdoptingRelationship),
        submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
        next,
        submitAnswer(AddChildPage, true),
        submitAnswer(ChildNamePage(Index(1)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(1)), false),
        submitAnswer(ChildBiologicalSexPage(Index(1)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(1)), LocalDate.now),
        submitAnswer(ChildBirthRegistrationCountryPage(Index(1)), England),
        submitAnswer(ChildBirthCertificateSystemNumberPage(Index(1)), systemNumber),
        submitAnswer(ApplicantRelationshipToChildPage(Index(1)), notAdoptingRelationship),
        submitAnswer(AnyoneClaimedForChildBeforePage(Index(1)), false),
        next,
        submitAnswer(AddChildPage, false),
        pageMustBe(CheckYourAnswersPage)
      )

    "removing one must let the user return to Check Answers" in {

      startingFrom(ChildNamePage(Index(0)))
        .run(
          initialise,
          goToChangeAnswer(AddChildPage),
          goTo(RemoveChildPage(Index(1))),
          removeAddToListItem(ChildQuery(Index(1))),
          pageMustBe(AddChildPage),
          submitAnswer(AddChildPage, false),
          pageMustBe(CheckYourAnswersPage)
        )
    }

    "removing all children must take the user to Child Name for index 0, and collect all the child's details" in {

      startingFrom(ChildNamePage(Index(0)))
        .run(
          initialise,
          goToChangeAnswer(AddChildPage),
          goTo(RemoveChildPage(Index(1))),
          removeAddToListItem(ChildQuery(Index(1))),
          goTo(RemoveChildPage(Index(0))),
          removeAddToListItem(ChildQuery(Index(0))),
          submitAnswer(ChildNamePage(Index(0)), childName),
          submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
          submitAnswer(ChildBiologicalSexPage(Index(0)), sex),
          submitAnswer(ChildDateOfBirthPage(Index(0)), LocalDate.now),
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
          submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), notAdoptingRelationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          next,
          submitAnswer(AddChildPage, false),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }
}
