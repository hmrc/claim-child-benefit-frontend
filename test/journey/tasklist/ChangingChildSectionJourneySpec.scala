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

package journey.tasklist

import generators.ModelGenerators
import journey.JourneyHelpers
import models.ChildBirthRegistrationCountry._
import models.{ApplicantRelationshipToChild => Relationship, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.TaskListPage
import pages.child._
import queries.ChildQuery

import java.time.LocalDate

class ChangingChildSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val childName            = arbitrary[ChildName].sample.value
  private val sex                  = arbitrary[ChildBiologicalSex].sample.value
  private val systemNumber         = Gen.listOfN(9, Gen.numChar).map(chars => BirthCertificateSystemNumber(chars.mkString)).sample.value
  private val adultName            = arbitrary[AdultName].sample.value
  private val ukAddress            = arbitrary[UkAddress].sample.value
  private val internationalAddress = arbitrary[InternationalAddress].sample.value
  private val scottishBcDetails    = arbitrary[ScottishBirthCertificateDetails].sample.value
  private val relationship         = arbitrary[Relationship].sample.value

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
        submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
        submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
        submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
        submitAnswer(ChildLivesWithApplicantPage(Index(0)), true),
        submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), false)
      )

    "that no one has claimed Child Benefit for previously" - {

      "changing `Anyone claimed before` to `true` must gather previous claimant details then return the user to the Check page" - {

        "when the previous claimant is known" - {

          "and their address is in the UK" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                basicChildJourney,
                goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
                submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
                submitAnswer(PreviousClaimantNameKnownPage(Index(0)), true),
                submitAnswer(PreviousClaimantNamePage(Index(0)), adultName),
                submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), true),
                submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), true),
                submitAnswer(PreviousClaimantUkAddressPage(Index(0)), ukAddress),
                pageMustBe(CheckChildDetailsPage(Index(0)))
              )
          }

          "and their address is not in the UK" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                basicChildJourney,
                goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
                submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
                submitAnswer(PreviousClaimantNameKnownPage(Index(0)), true),
                submitAnswer(PreviousClaimantNamePage(Index(0)), adultName),
                submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), true),
                submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), false),
                submitAnswer(PreviousClaimantInternationalAddressPage(Index(0)), internationalAddress),
                pageMustBe(CheckChildDetailsPage(Index(0)))
              )
          }

          "and their address is not known" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                basicChildJourney,
                goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
                submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
                submitAnswer(PreviousClaimantNameKnownPage(Index(0)), true),
                submitAnswer(PreviousClaimantNamePage(Index(0)), adultName),
                submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), false),
                pageMustBe(CheckChildDetailsPage(Index(0)))
              )
          }
        }

        "when the previous claimant is not known" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              basicChildJourney,
              goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
              submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
              submitAnswer(PreviousClaimantNameKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }
    }

    "that someone has claimed Child Benefit for previously" - {

      val initialState =
        journeyOf(
          basicChildJourney,
          setUserAnswerTo(AnyoneClaimedForChildBeforePage(Index(0)), true),
          setUserAnswerTo(PreviousClaimantNameKnownPage(Index(0)), true),
          setUserAnswerTo(PreviousClaimantNamePage(Index(0)), adultName),
          setUserAnswerTo(PreviousClaimantAddressKnownPage(Index(0)), true),
          setUserAnswerTo(PreviousClaimantAddressInUkPage(Index(0)), true),
          setUserAnswerTo(PreviousClaimantUkAddressPage(Index(0)), ukAddress),
          setUserAnswerTo(PreviousClaimantInternationalAddressPage(Index(0)), internationalAddress)
        )

      "changing `Anyone claimed before` to `false` must remove the previous claimant details and return the user to the Check Details page" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            initialState,
            goToChangeAnswer(AnyoneClaimedForChildBeforePage(Index(0))),
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(PreviousClaimantNameKnownPage(Index(0))),
            answersMustNotContain(PreviousClaimantNamePage(Index(0))),
            answersMustNotContain(PreviousClaimantAddressKnownPage(Index(0))),
            answersMustNotContain(PreviousClaimantAddressInUkPage(Index(0))),
            answersMustNotContain(PreviousClaimantUkAddressPage(Index(0))),
            answersMustNotContain(PreviousClaimantInternationalAddressPage(Index(0)))
          )
      }

      "and the user knew the person who previously claimed and their details" - {

        "changing to say they do not know the previous claimant must remove previous claimant details and return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(PreviousClaimantNameKnownPage(Index(0))),
              submitAnswer(PreviousClaimantNameKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0))),
              answersMustNotContain(PreviousClaimantNamePage(Index(0))),
              answersMustNotContain(PreviousClaimantAddressKnownPage(Index(0))),
              answersMustNotContain(PreviousClaimantAddressInUkPage(Index(0))),
              answersMustNotContain(PreviousClaimantUkAddressPage(Index(0))),
              answersMustNotContain(PreviousClaimantInternationalAddressPage(Index(0)))
            )
        }

        "changing to say they do not know the address of the previous claimant must remove the address and return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(PreviousClaimantAddressKnownPage(Index(0))),
              submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0))),
              answersMustNotContain(PreviousClaimantAddressInUkPage(Index(0))),
              answersMustNotContain(PreviousClaimantUkAddressPage(Index(0))),
              answersMustNotContain(PreviousClaimantInternationalAddressPage(Index(0)))
            )
        }

        "and originally said their address was in the UK" - {

          "changing to say the address is not in the UK must collect the international address, remove the UK address and return to Check Details" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                initialState,
                remove(PreviousClaimantInternationalAddressPage(Index(0))),
                goToChangeAnswer(PreviousClaimantAddressInUkPage(Index(0))),
                submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), false),
                submitAnswer(PreviousClaimantInternationalAddressPage(Index(0)), internationalAddress),
                pageMustBe(CheckChildDetailsPage(Index(0))),
                answersMustNotContain(PreviousClaimantUkAddressPage(Index(0)))
              )
          }
        }

        "and originally said their address was not in the UK" - {

          "changing to say the address is in the UK must collect the UK address, remove the UK address and return to Check Details" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                initialState,
                setUserAnswerTo(PreviousClaimantAddressInUkPage(Index(0)), false),
                goToChangeAnswer(PreviousClaimantAddressInUkPage(Index(0))),
                submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), true),
                submitAnswer(PreviousClaimantUkAddressPage(Index(0)), ukAddress),
                pageMustBe(CheckChildDetailsPage(Index(0))),
                answersMustNotContain(PreviousClaimantInternationalAddressPage(Index(0)))
              )
          }
        }
      }

      "and the user knew the person who previously claimed, but not their address" - {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(AnyoneClaimedForChildBeforePage(Index(0)), true),
            setUserAnswerTo(PreviousClaimantNameKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousClaimantNamePage(Index(0)), adultName),
            setUserAnswerTo(PreviousClaimantAddressKnownPage(Index(0)), false)
          )

        "changing to say they know the UK address must collect the address and go to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              setUserAnswerTo(PreviousClaimantAddressKnownPage(Index(0)), false),
              goToChangeAnswer(PreviousClaimantAddressKnownPage(Index(0))),
              submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), true),
              submitAnswer(PreviousClaimantUkAddressPage(Index(0)), ukAddress),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }

        "changing to say they know the international address must collect the address and go to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              setUserAnswerTo(PreviousClaimantAddressKnownPage(Index(0)), false),
              goToChangeAnswer(PreviousClaimantAddressKnownPage(Index(0))),
              submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), false),
              submitAnswer(PreviousClaimantInternationalAddressPage(Index(0)), internationalAddress),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }

      "and the user did not know the person who previously claimed" - {

        "changing to say they do know the person who previously claimed must collect that person's details then return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              setUserAnswerTo(PreviousClaimantNameKnownPage(Index(0)), false),
              goToChangeAnswer(PreviousClaimantNameKnownPage(Index(0))),
              submitAnswer(PreviousClaimantNameKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantNamePage(Index(0)), adultName),
              submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
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
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber),
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
            setUserAnswerTo(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
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
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), scottishBcDetails),
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

    "that the user said lives with them" - {

      "changing to say they live with someone else must collect the other person's details, remove details of the person the child previously lived with, and go to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            setUserAnswerTo(ChildLivedWithAnyoneElsePage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNameKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNamePage(Index(0)), adultName),
            setUserAnswerTo(PreviousGuardianAddressKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianAddressInUkPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianUkAddressPage(Index(0)), ukAddress),
            setUserAnswerTo(PreviousGuardianInternationalAddressPage(Index(0)), internationalAddress),
            setUserAnswerTo(PreviousGuardianPhoneNumberKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianPhoneNumberPage(Index(0)),  "0777777777"),
            setUserAnswerTo(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now),
            goToChangeAnswer(ChildLivesWithApplicantPage(Index(0))),
            submitAnswer(ChildLivesWithApplicantPage(Index(0)), false),
            submitAnswer(GuardianNameKnownPage(Index(0)), true),
            submitAnswer(GuardianNamePage(Index(0)), adultName),
            submitAnswer(GuardianAddressKnownPage(Index(0)), true),
            submitAnswer(GuardianAddressInUkPage(Index(0)), true),
            submitAnswer(GuardianUkAddressPage(Index(0)), ukAddress),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(ChildLivedWithAnyoneElsePage(Index(0))),
            answersMustNotContain(PreviousGuardianNameKnownPage(Index(0))),
            answersMustNotContain(PreviousGuardianNamePage(Index(0))),
            answersMustNotContain(PreviousGuardianAddressKnownPage(Index(0))),
            answersMustNotContain(PreviousGuardianAddressInUkPage(Index(0))),
            answersMustNotContain(PreviousGuardianUkAddressPage(Index(0))),
            answersMustNotContain(PreviousGuardianInternationalAddressPage(Index(0))),
            answersMustNotContain(PreviousGuardianPhoneNumberKnownPage(Index(0))),
            answersMustNotContain(PreviousGuardianPhoneNumberPage(Index(0))),
            answersMustNotContain(DateChildStartedLivingWithApplicantPage(Index(0)))
          )
      }
    }

    "that the user said does not live with them" - {

      "changing that answer must ask if the child lived with someone else in the past year, remove details of the person the child lives with, and go to Check Details" in {

        startingFrom(ChildNamePage(Index(0)))
          .run(
            basicChildJourney,
            setUserAnswerTo(ChildLivesWithApplicantPage(Index(0)), false),
            setUserAnswerTo(GuardianNameKnownPage(Index(0)), true),
            setUserAnswerTo(GuardianNamePage(Index(0)), adultName),
            setUserAnswerTo(GuardianAddressKnownPage(Index(0)), true),
            setUserAnswerTo(GuardianAddressInUkPage(Index(0)), true),
            setUserAnswerTo(GuardianUkAddressPage(Index(0)), ukAddress),
            setUserAnswerTo(GuardianInternationalAddressPage(Index(0)), internationalAddress),
            goToChangeAnswer(ChildLivesWithApplicantPage(Index(0))),
            submitAnswer(ChildLivesWithApplicantPage(Index(0)), true),
            submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), true),
            submitAnswer(PreviousGuardianNameKnownPage(Index(0)), true),
            submitAnswer(PreviousGuardianNamePage(Index(0)), adultName),
            submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), true),
            submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), true),
            submitAnswer(PreviousGuardianUkAddressPage(Index(0)), ukAddress),
            submitAnswer(PreviousGuardianPhoneNumberKnownPage(Index(0)), true),
            submitAnswer(PreviousGuardianPhoneNumberPage(Index(0)), "077777777"),
            submitAnswer(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now),
            pageMustBe(CheckChildDetailsPage(Index(0))),
            answersMustNotContain(GuardianNameKnownPage(Index(0))),
            answersMustNotContain(GuardianNamePage(Index(0))),
            answersMustNotContain(GuardianAddressKnownPage(Index(0))),
            answersMustNotContain(GuardianAddressInUkPage(Index(0))),
            answersMustNotContain(GuardianUkAddressPage(Index(0))),
            answersMustNotContain(GuardianInternationalAddressPage(Index(0)))
          )
      }

      "that they knew the details for" - {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(ChildLivesWithApplicantPage(Index(0)), false),
            setUserAnswerTo(GuardianNameKnownPage(Index(0)), true),
            setUserAnswerTo(GuardianNamePage(Index(0)), adultName),
            setUserAnswerTo(GuardianAddressKnownPage(Index(0)), true),
            setUserAnswerTo(GuardianAddressInUkPage(Index(0)), true),
            setUserAnswerTo(GuardianUkAddressPage(Index(0)), ukAddress),
            setUserAnswerTo(GuardianInternationalAddressPage(Index(0)), internationalAddress)
          )

        "changing to say they do not know the person they live with must remove that person's details and return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(GuardianNameKnownPage(Index(0))),
              submitAnswer(GuardianNameKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0))),
              answersMustNotContain(GuardianNamePage(Index(0))),
              answersMustNotContain(GuardianAddressKnownPage(Index(0))),
              answersMustNotContain(GuardianAddressInUkPage(Index(0))),
              answersMustNotContain(GuardianUkAddressPage(Index(0))),
              answersMustNotContain(GuardianInternationalAddressPage(Index(0)))
            )
        }

        "changing to say they do not know the person's address must remove the address and return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(GuardianAddressKnownPage(Index(0))),
              submitAnswer(GuardianAddressKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0))),
              answersMustNotContain(GuardianAddressInUkPage(Index(0))),
              answersMustNotContain(GuardianUkAddressPage(Index(0))),
              answersMustNotContain(GuardianInternationalAddressPage(Index(0)))
            )
        }

        "and originally said their address was in the UK" - {

          "changing to say their address is not in the UK must collect the international address, remove the UK address and return to Check Details" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                initialState,
                remove(GuardianInternationalAddressPage(Index(0))),
                goToChangeAnswer(GuardianAddressInUkPage(Index(0))),
                submitAnswer(GuardianAddressInUkPage(Index(0)), false),
                submitAnswer(GuardianInternationalAddressPage(Index(0)), internationalAddress),
                pageMustBe(CheckChildDetailsPage(Index(0))),
                answersMustNotContain(GuardianUkAddressPage(Index(0)))
              )
          }
        }

        "and originally said their address was not in the UK" - {

          "changing to say their address is in the UK must collect the UK address, remove the international address and return to Check Details" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                initialState,
                remove(GuardianUkAddressPage(Index(0))),
                setUserAnswerTo(GuardianAddressInUkPage(Index(0)), false),
                goToChangeAnswer(GuardianAddressInUkPage(Index(0))),
                submitAnswer(GuardianAddressInUkPage(Index(0)), true),
                submitAnswer(GuardianUkAddressPage(Index(0)), ukAddress),
                pageMustBe(CheckChildDetailsPage(Index(0))),
                answersMustNotContain(GuardianInternationalAddressPage(Index(0)))
              )
          }
        }
      }

      "that the user said they did not know the details for" - {

        "changing to say they do know the details must collect them then return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              basicChildJourney,
              setUserAnswerTo(ChildLivesWithApplicantPage(Index(0)), false),
              setUserAnswerTo(GuardianNameKnownPage(Index(0)), false),
              goToChangeAnswer(GuardianNameKnownPage(Index(0))),
              submitAnswer(GuardianNameKnownPage(Index(0)), true),
              submitAnswer(GuardianNamePage(Index(0)), adultName),
              submitAnswer(GuardianAddressKnownPage(Index(0)), true),
              submitAnswer(GuardianAddressInUkPage(Index(0)), true),
              submitAnswer(GuardianUkAddressPage(Index(0)), ukAddress),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }
    }

    "that the user said lived with someone in the past year" - {

      "who they knew the details for" - {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(ChildLivedWithAnyoneElsePage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNameKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNamePage(Index(0)), adultName),
            setUserAnswerTo(PreviousGuardianAddressKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianAddressInUkPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianUkAddressPage(Index(0)), ukAddress),
            setUserAnswerTo(PreviousGuardianInternationalAddressPage(Index(0)), internationalAddress),
            setUserAnswerTo(PreviousGuardianPhoneNumberKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianPhoneNumberPage(Index(0)), "07777 777777"),
            setUserAnswerTo(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now)
          )

        "changing to say they don't know the details should remove them and return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(PreviousGuardianNameKnownPage(Index(0))),
              submitAnswer(PreviousGuardianNameKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0))),
              answersMustNotContain(PreviousGuardianNamePage(Index(0))),
              answersMustNotContain(PreviousGuardianAddressKnownPage(Index(0))),
              answersMustNotContain(PreviousGuardianAddressInUkPage(Index(0))),
              answersMustNotContain(PreviousGuardianUkAddressPage(Index(0))),
              answersMustNotContain(PreviousGuardianInternationalAddressPage(Index(0))),
              answersMustNotContain(PreviousGuardianPhoneNumberKnownPage(Index(0))),
              answersMustNotContain(PreviousGuardianPhoneNumberPage(Index(0)))
            )
        }

        "changing to say they don't know the person's address must remove the address and return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(PreviousGuardianAddressKnownPage(Index(0))),
              submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0))),
              answersMustNotContain(PreviousGuardianAddressInUkPage(Index(0))),
              answersMustNotContain(PreviousGuardianUkAddressPage(Index(0))),
              answersMustNotContain(PreviousGuardianInternationalAddressPage(Index(0)))
            )
        }

        "and who lives in the UK" - {

          "changing to say they do not live in the UK must remove the UK address, collect the international address and return to Check Details" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                initialState,
                remove(PreviousGuardianInternationalAddressPage(Index(0))),
                goToChangeAnswer(PreviousGuardianAddressInUkPage(Index(0))),
                submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), false),
                submitAnswer(PreviousGuardianInternationalAddressPage(Index(0)), internationalAddress),
                pageMustBe(CheckChildDetailsPage(Index(0))),
                answersMustNotContain(PreviousGuardianUkAddressPage(Index(0))),
              )
          }
        }

        "and who does not lives in the UK" - {

          "changing to say they live in the UK must remove the international address, collect the UK address and return to Check Details" in {

            startingFrom(ChildNamePage(Index(0)))
              .run(
                initialState,
                remove(PreviousGuardianUkAddressPage(Index(0))),
                setUserAnswerTo(PreviousGuardianAddressInUkPage(Index(0)), false),
                goToChangeAnswer(PreviousGuardianAddressInUkPage(Index(0))),
                submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), true),
                submitAnswer(PreviousGuardianUkAddressPage(Index(0)), ukAddress),
                pageMustBe(CheckChildDetailsPage(Index(0))),
                answersMustNotContain(PreviousGuardianInternationalAddressPage(Index(0))),
              )
          }
        }
      }

      "who they knew details for, but not the address" - {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(ChildLivedWithAnyoneElsePage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNameKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNamePage(Index(0)), adultName),
            setUserAnswerTo(PreviousGuardianAddressKnownPage(Index(0)), false),
            setUserAnswerTo(PreviousGuardianPhoneNumberKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianPhoneNumberPage(Index(0)), "07777 777777")
          )

        "changing to say they know the UK address must collect it then return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(PreviousGuardianAddressKnownPage(Index(0))),
              submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), true),
              submitAnswer(PreviousGuardianUkAddressPage(Index(0)), ukAddress),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }

        "changing to say they know the international address must collect it then return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(PreviousGuardianAddressKnownPage(Index(0))),
              submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), false),
              submitAnswer(PreviousGuardianInternationalAddressPage(Index(0)), internationalAddress),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }

      "who they knew details for, but not the phone number" - {

        val initialState =
          journeyOf(
            basicChildJourney,
            setUserAnswerTo(ChildLivedWithAnyoneElsePage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNameKnownPage(Index(0)), true),
            setUserAnswerTo(PreviousGuardianNamePage(Index(0)), adultName),
            setUserAnswerTo(PreviousGuardianAddressKnownPage(Index(0)), false),
            setUserAnswerTo(PreviousGuardianPhoneNumberKnownPage(Index(0)), false),
            setUserAnswerTo(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now)
          )

        "changing to say they know the phone number must collect it then return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              initialState,
              goToChangeAnswer(PreviousGuardianPhoneNumberKnownPage(Index(0))),
              submitAnswer(PreviousGuardianPhoneNumberKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianPhoneNumberPage(Index(0)), "07777 777777"),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }

      "who they did not know the details for" - {

        "changing to say they do know the details must collect them then return to Check Details" in {

          startingFrom(ChildNamePage(Index(0)))
            .run(
              basicChildJourney,
              setUserAnswerTo(ChildLivedWithAnyoneElsePage(Index(0)), true),
              setUserAnswerTo(PreviousGuardianNameKnownPage(Index(0)), false),
              setUserAnswerTo(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now),
              goToChangeAnswer(PreviousGuardianNameKnownPage(Index(0))),
              submitAnswer(PreviousGuardianNameKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianNamePage(Index(0)), adultName),
              submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), true),
              submitAnswer(PreviousGuardianUkAddressPage(Index(0)), ukAddress),
              submitAnswer(PreviousGuardianPhoneNumberKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianPhoneNumberPage(Index(0)), "07777 777777"),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
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
        submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
        submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
        submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
        submitAnswer(ChildLivesWithApplicantPage(Index(0)), true),
        submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), false),
        next,
        submitAnswer(AddChildPage, true),
        submitAnswer(ChildNamePage(Index(1)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(1)), false),
        submitAnswer(ChildBiologicalSexPage(Index(1)), sex),
        submitAnswer(ChildDateOfBirthPage(Index(1)), LocalDate.now),
        submitAnswer(ChildBirthRegistrationCountryPage(Index(1)), England),
        submitAnswer(BirthCertificateHasSystemNumberPage(Index(1)), true),
        submitAnswer(ChildBirthCertificateSystemNumberPage(Index(1)), systemNumber),
        submitAnswer(AdoptingThroughLocalAuthorityPage(Index(1)), false),
        submitAnswer(ApplicantRelationshipToChildPage(Index(1)), relationship),
        submitAnswer(AnyoneClaimedForChildBeforePage(Index(1)), false),
        submitAnswer(ChildLivesWithApplicantPage(Index(1)), true),
        submitAnswer(ChildLivedWithAnyoneElsePage(Index(1)), false),
        next,
        submitAnswer(AddChildPage, false),
        pageMustBe(TaskListPage)
      )

    "removing one must let the user return to the task list" in {

      startingFrom(ChildNamePage(Index(0)))
        .run(
          initialise,
          goTo(AddChildPage),
          goToChangeAnswer(AddChildPage),
          goTo(RemoveChildPage(Index(1))),
          removeAddToListItem(ChildQuery(Index(1))),
          pageMustBe(AddChildPage),
          submitAnswer(AddChildPage, false),
          pageMustBe(TaskListPage)
        )
    }

    "removing all children must take the user to Child Name for index 0, and collect all the child's details" in {

      startingFrom(ChildNamePage(Index(0)))
        .run(
          initialise,
          goTo(AddChildPage),
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
          submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          submitAnswer(ChildLivesWithApplicantPage(Index(0)), true),
          submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), false),
          next,
          submitAnswer(AddChildPage, false),
          pageMustBe(TaskListPage)
        )
    }
  }
}
