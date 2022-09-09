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
import models.ChildBirthRegistrationCountry._
import models.{ApplicantRelationshipToChild => Relationship, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.CheckYourAnswersPage
import pages.child._
import queries.ChildQuery

import java.time.LocalDate

class ChangingChildDetailsJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

    private val childName         = arbitrary[ChildName].sample.value
    private val sex               = arbitrary[ChildBiologicalSex].sample.value
    private val systemNumber      = Gen.listOfN(9, Gen.numChar).sample.value.mkString
    private val claimantName      = arbitrary[AdultName].sample.value
    private val claimantAddress   = arbitrary[UkAddress].sample.value
    private val scottishBcDetails = Gen.listOfN(10, Gen.numChar).sample.value.mkString
    private val relationship      = arbitrary[Relationship].sample.value

  "when a user has added a child" - {

    val basicChildJourney =
      journeyOf(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
        submitAnswer(ChildBiologicalSexPage(Index(0)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(0)), LocalDate.now),
        submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
        submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
        submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
        submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
        submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
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
            submitAnswer(PreviousClaimantUkAddressPage(Index(0)), claimantAddress),
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
              setUserAnswerTo(PreviousClaimantUkAddressPage(Index(0)), claimantAddress)
            )

      "changing `Anyone claimed before` to `false` must remove the previous claimant details and return the user to the Check page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(PreviousClaimantNamePage(Index(0))),
            answersMustNotContain(PreviousClaimantUkAddressPage(Index(0)))
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
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Northern Ireland must remove Birth Certificate System Number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Other must remove Birth Certificate System Number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Unknown must remove Birth Certificate System Number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
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
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Northern Ireland must remove Birth Certificate System Number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Other must remove Birth Certificate System Number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }

      "changing the country to Unknown must remove Birth Certificate System Number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }
    }

    "that the user said was registered in Scotland" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
          setUserAnswerTo(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
          setUserAnswerTo(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System Number, remove Scottish Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ScottishBirthCertificateHasNumbersPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System Number, remove Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ScottishBirthCertificateHasNumbersPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Northern Ireland must remove Scottish  Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Other must remove Scottish  Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }

      "changing the country to Unknown must remove Scottish Details, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(BirthCertificateHasSystemNumberPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }
    }

    "that the user said was registered in another country" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Other),
          remove(BirthCertificateHasSystemNumberPage(Index(0))),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Scotland must collect Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Northern Ireland must return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
            pageMustBe(CheckChildDetailsPage(Index(0)))
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

    "that the user said was registered in Northern Ireland" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Scotland must collect Scottish Details, then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0)))
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

    "that the user said was registered in an Unknown country" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
          remove(ChildBirthCertificateSystemNumberPage(Index(0)))
        )

      "changing the country to England must collect the Birth Certificate System number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Wales must collect the Birth Certificate System number, then return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Scotland must collect Scottish Details,  then return to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }

      "changing the country to Northern Ireland must return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ChildBirthRegistrationCountryPage(Index(0))),
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
            pageMustBe(CheckChildDetailsPage(Index(0)))
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

    "whose birth certificate had a system number" - {

      "changing to say it does not have a system number must remove the system number and return to Check Child Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            goToChangeAnswer(BirthCertificateHasSystemNumberPage(Index(0))),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildBirthCertificateSystemNumberPage(Index(0)))
          )
      }
    }

    "whose birth certificate did not have a system number" - {

      "changing to say it does have a system number must collect the system number and return to Check Child Details" in {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(BirthCertificateHasSystemNumberPage(Index(0)), false),
            remove(ChildBirthCertificateSystemNumberPage(Index(0)))
          )

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(BirthCertificateHasSystemNumberPage(Index(0))),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), "123456789"),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }

    "whose Scottish birth certificate had numbers" - {

      "changing to say it does not have number must remove the system number and return to Check Child Details" in {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            setUserAnswerTo(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            setUserAnswerTo(ChildScottishBirthCertificateDetailsPage(Index(0)), "1234567890"),
            remove(BirthCertificateHasSystemNumberPage(Index(0))),
            remove(ChildBirthCertificateSystemNumberPage(Index(0)))
          )

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ScottishBirthCertificateHasNumbersPage(Index(0))),
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildScottishBirthCertificateDetailsPage(Index(0)))
          )
      }
    }

    "whose Scottish birth certificate did not have numbers" - {

      "changing to say it does have numbers must collect the number and return to Check Child Details" in {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            setUserAnswerTo(ScottishBirthCertificateHasNumbersPage(Index(0)), false),
            remove(BirthCertificateHasSystemNumberPage(Index(0))),
            remove(ChildBirthCertificateSystemNumberPage(Index(0)))
          )

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(ScottishBirthCertificateHasNumbersPage(Index(0))),
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), "1234567890"),
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
  }
  
  "when a user has added multiple children" - {

    val initialise =
      journeyOf(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
        submitAnswer(ChildBiologicalSexPage(Index(0)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(0)), LocalDate.now),
        submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
        submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
        submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
        submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
        submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
        submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
        next,
        submitAnswer(AddChildPage, true),
        submitAnswer(ChildNamePage(Index(1)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(1)), false),
        submitAnswer(ChildBiologicalSexPage(Index(1)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(1)), LocalDate.now),
        submitAnswer(ChildBirthRegistrationCountryPage(Index(1)), England),
        submitAnswer(BirthCertificateHasSystemNumberPage(Index(1)), true),
        submitAnswer(ChildBirthCertificateSystemNumberPage(Index(1)), systemNumber),
        submitAnswer(ApplicantRelationshipToChildPage(Index(1)), relationship),
        submitAnswer(AdoptingThroughLocalAuthorityPage(Index(1)), false),
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
          submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
          submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          next,
          submitAnswer(AddChildPage, false),
          pageMustBe(CheckYourAnswersPage)
        )
    }
  }
}
