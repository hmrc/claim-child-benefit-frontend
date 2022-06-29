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
import models.AnyoneClaimedForChildBefore._
import models.ChildBirthRegistrationCountry._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.child._

import java.time.LocalDate

class ChangingChildDetailsJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "when a user has added a child" - {

    val childName         = arbitrary[ChildName].sample.value
    val sex               = arbitrary[ChildBiologicalSex].sample.value
    val systemNumber      = Gen.listOfN(9, Gen.numChar).sample.value.mkString
    val relationship      = arbitrary[ApplicantRelationshipToChild].sample.value
    val claimantName      = arbitrary[PreviousClaimantName].sample.value
    val claimantAddress   = arbitrary[Address].sample.value
    val scottishBcDetails = arbitrary[ChildScottishBirthCertificateDetails].sample.value
    val includedDocuments = Set(arbitrary[IncludedDocuments].sample.value)

    val basicChildJourney =
      journeyOf(
        answerPage(ChildNamePage(Index(0)), childName, ChildHasPreviousNamePage(Index(0))),
        answerPage(ChildHasPreviousNamePage(Index(0)), false, ChildBiologicalSexPage(Index(0))),
        answerPage(ChildBiologicalSexPage(Index(0)), sex, ChildDateOfBirthPage(Index(0))),
        answerPage(ChildDateOfBirthPage(Index(0)), LocalDate.now, ChildBirthRegistrationCountryPage(Index(0))),
        answerPage(ChildBirthRegistrationCountryPage(Index(0)), England, ChildBirthCertificateSystemNumberPage(Index(0))),
        answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber, ApplicantRelationshipToChildPage(Index(0))),
        answerPage(ApplicantRelationshipToChildPage(Index(0)), relationship, AnyoneClaimedForChildBeforePage(Index(0))),
        answerPage(AnyoneClaimedForChildBeforePage(Index(0)), No, AdoptingChildPage(Index(0))),
        answerPage(AdoptingChildPage(Index(0)), false, CheckChildDetailsPage(Index(0)))
      )

    "that no one has claimed Child Benefit for previously" - {

      "changing `Anyone claimed before` to `Applicant` must update that answer and return the user to the Check page" in{

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), Applicant, CheckChildDetailsPage(Index(0))),
            answerMustEqual(AnyoneClaimedForChildBeforePage(Index(0)), Applicant)
          )
      }

      "changing `Anyone claimed before` to `Partner` must update that answer and return the user to the Check page" in{

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), Partner, CheckChildDetailsPage(Index(0))),
            answerMustEqual(AnyoneClaimedForChildBeforePage(Index(0)), Partner)
          )
      }

      "changing `Anyone claimed before` to `Someone Else` must gather previous claimant details then return the user to the Check page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), SomeoneElse, PreviousClaimantNamePage(Index(0))),
            answerPage(PreviousClaimantNamePage(Index(0)), claimantName, PreviousClaimantAddressPage(Index(0))),
            answerPage(PreviousClaimantAddressPage(Index(0)), claimantAddress, CheckChildDetailsPage(Index(0))),
            answerMustEqual(AnyoneClaimedForChildBeforePage(Index(0)), SomeoneElse)
          )
      }
    }

    "that someone other than the applicant or their partner has claimed Child Benefit for previously" - {

        val initialState =
          journeyOf(
              basicChildJourney,
              answer(AnyoneClaimedForChildBeforePage(Index(0)), SomeoneElse),
              answer(PreviousClaimantNamePage(Index(0)), claimantName),
              answer(PreviousClaimantAddressPage(Index(0)), claimantAddress)
            )

      "changing `Anyone claimed before` to `No` must remove the previous claimant details and return the user to the Check page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), No, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(PreviousClaimantNamePage(Index(0))),
            answersMustNotContain(PreviousClaimantAddressPage(Index(0)))
          )
      }

      "changing `Anyone claimed before` to `Applicant` must remove the previous claimant details and return the user to the Check page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), Applicant, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(PreviousClaimantNamePage(Index(0))),
            answersMustNotContain(PreviousClaimantAddressPage(Index(0)))
          )
      }

      "changing `Anyone claimed before` to `Partner` must remove the previous claimant details and return the user to the Check page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            answerPage(AnyoneClaimedForChildBeforePage(Index(0)), Partner, CheckChildDetailsPage(Index(0))),
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
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Wales, CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Scotland must remove Birth Certificate System Number, collect Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Scotland, ChildScottishBirthCertificateDetailsPage(Index(0))),
            answerPage(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Other must remove Birth Certificate System Number, collect Included Documents, then return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Other, IncludedDocumentsPage(Index(0))),
            answerPage(IncludedDocumentsPage(Index(0)), includedDocuments, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to unknown must remove Birth Certificate System Number, collect Included Documents, then return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Unknown, IncludedDocumentsPage(Index(0))),
            answerPage(IncludedDocumentsPage(Index(0)), includedDocuments, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }
    }

    "that the user said was registered in Wales" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          answer(ChildBirthRegistrationCountryPage(Index(0)), Wales)
        )

      "changing the country to England must return the user to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), England, CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Scotland must remove Birth Certificate System Number, collect Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Scotland, ChildScottishBirthCertificateDetailsPage(Index(0))),
            answerPage(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Other must remove Birth Certificate System Number, collect Included Documents, then return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Other, IncludedDocumentsPage(Index(0))),
            answerPage(IncludedDocumentsPage(Index(0)), includedDocuments, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to unknown must remove Birth Certificate System Number, collect Included Documents, then return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Unknown, IncludedDocumentsPage(Index(0))),
            answerPage(IncludedDocumentsPage(Index(0)), includedDocuments, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }
    }

    "that the user said was registered in Scotland" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          answer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
          answer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System Number, remove Scottish Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), England, ChildBirthCertificateSystemNumberPage(Index(0))),
            answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System Number, remove Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Wales, ChildBirthCertificateSystemNumberPage(Index(0))),
            answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Other must collect Included Documents, remove Scottish  Details, then return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Other, IncludedDocumentsPage(Index(0))),
            answerPage(IncludedDocumentsPage(Index(0)), includedDocuments, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to unknown must collect Included Documents, remove Scottish Details, then return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Unknown, IncludedDocumentsPage(Index(0))),
            answerPage(IncludedDocumentsPage(Index(0)), includedDocuments, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }
    }

    "that the user said was registered in another country" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          answer(ChildBirthRegistrationCountryPage(Index(0)), Other),
          answer(IncludedDocumentsPage(Index(0)), includedDocuments),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), England, ChildBirthCertificateSystemNumberPage(Index(0))),
            answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Wales, ChildBirthCertificateSystemNumberPage(Index(0))),
            answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Scotland must collect Scottish Details, remove Included Documents, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Scotland, ChildScottishBirthCertificateDetailsPage(Index(0))),
            answerPage(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Unknown must return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Unknown, CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that the user said was registered in an unknown country" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          answer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
          answer(IncludedDocumentsPage(Index(0)), includedDocuments),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), England, ChildBirthCertificateSystemNumberPage(Index(0))),
            answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System number, remove Included Documents, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Wales, ChildBirthCertificateSystemNumberPage(Index(0))),
            answerPage(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Scotland must collect Scottish Details, remove Included Documents, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Scotland, ChildScottishBirthCertificateDetailsPage(Index(0))),
            answerPage(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails, CheckChildDetailsPage(Index(0))),
            answersMustNotContain(IncludedDocumentsPage(Index(0)))
          )
      }

      "changing the country to Other must return to Child Benefit" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            answerPage(ChildBirthRegistrationCountryPage(Index(0)), Other, CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that the user said had no previous names" - {

      "changing that answer must collect whether the name was changed by deed poll, and the name(s)" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildHasPreviousNamePage(Index(0))),
            answerPage(ChildHasPreviousNamePage(Index(0)), true, ChildNameChangedByDeedPollPage(Index(0))),
            answerPage(ChildNameChangedByDeedPollPage(Index(0)), true, ChildPreviousNamePage(Index(0), Index(0))),
            answerPage(ChildPreviousNamePage(Index(0), Index(0)), childName, AddChildPreviousNamePage(Index(0))),
            answerPage(AddChildPreviousNamePage(Index(0)), false, CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "that they provided previous names for" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          answer(ChildHasPreviousNamePage(Index(0)), true),
          answer(ChildNameChangedByDeedPollPage(Index(0)), true),
          answer(ChildPreviousNamePage(Index(0), Index(0)), childName),
          answer(ChildPreviousNamePage(Index(0), Index(1)), childName)
        )

      "changing to say they had no previous names must remove all names, and whether the name was changed by deed poll, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildHasPreviousNamePage(Index(0))),
            answerPage(ChildHasPreviousNamePage(Index(0)), false, CheckChildDetailsPage(Index(0))),
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
            remove(ChildPreviousNamePage(Index(0), Index(1))),
            next,
            pageMustBe(AddChildPreviousNamePage(Index(0))),
            answerPage(AddChildPreviousNamePage(Index(0)), false, CheckChildDetailsPage(Index(0)))
          )
      }

      "removing the last name must also remove whether the name was changed by deed poll, and go to Child Has Previous Name" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AddChildPreviousNamePage(Index(0))),
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

      "must allow the user to add another previous name" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AddChildPreviousNamePage(Index(0))),
            answerPage(AddChildPreviousNamePage(Index(0)), true, ChildPreviousNamePage(Index(0), Index(2))),
            answerPage(ChildPreviousNamePage(Index(0), Index(2)), childName, AddChildPreviousNamePage(Index(0))),
            answerPage(AddChildPreviousNamePage(Index(0)), false, CheckChildDetailsPage(Index(0)))
          )
      }

      "must allow the user to change a name" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AddChildPreviousNamePage(Index(0))),
            goToChangeAnswer(ChildPreviousNamePage(Index(0), Index(0))),
            answerPage(ChildPreviousNamePage(Index(0), Index(0)), childName, AddChildPreviousNamePage(Index(0))),
            answerPage(AddChildPreviousNamePage(Index(0)), false, CheckChildDetailsPage(Index(0)))
          )
      }
    }
  }
}
