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

package journey

import generators.ModelGenerators
import models.ChildBirthRegistrationCountry._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages.TaskListPage
import pages.child._

import java.time.LocalDate

class ChildSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  private val adultName = AdultName(None, "first", None, "last")
  private val internationalAddress = InternationalAddress("line 1", None, "town", None, Some("postcode"), Country.internationalCountries.head)
  private val ukAddress = UkAddress("line 1", None, "town", None, "postcode")
  private val childName = ChildName("first", None, "last")
  private val sex = arbitrary[ChildBiologicalSex].sample.value

  "users whose child has no previous names must not be asked for them" in {

    startingFrom(ChildNamePage(Index(0)))
      .run(
        submitAnswer(ChildNamePage(Index(0)), childName),
        submitAnswer(ChildHasPreviousNamePage(Index(0)), false),
        submitAnswer(ChildDateOfBirthPage(Index(0)), LocalDate.now),
        submitAnswer(ChildBiologicalSexPage(Index(0)), sex),
        pageMustBe(ChildBirthRegistrationCountryPage(Index(0)))
      )
  }

  "users whose child has previous names" - {

    "must be asked for as many as necessary" in {

      startingFrom(ChildNamePage(Index(0)))
        .run(
          submitAnswer(ChildNamePage(Index(0)), childName),
          submitAnswer(ChildHasPreviousNamePage(Index(0)), true),
          submitAnswer(ChildNameChangedByDeedPollPage(Index(0)), true),
          submitAnswer(ChildPreviousNamePage(Index(0), Index(0)), childName),
          submitAnswer(AddChildPreviousNamePage(Index(0), Some(Index(0))), true),
          submitAnswer(ChildPreviousNamePage(Index(0), Index(1)), childName),
          submitAnswer(AddChildPreviousNamePage(Index(0), Some(Index(1))), false),
          pageMustBe(ChildDateOfBirthPage(Index(0)))
        )
    }

    "must be able to remove them" in {

      startingFrom(ChildNamePage(Index(0)))
        .run(
          submitAnswer(ChildNamePage(Index(0)), childName),
          submitAnswer(ChildHasPreviousNamePage(Index(0)), true),
          submitAnswer(ChildNameChangedByDeedPollPage(Index(0)), true),
          submitAnswer(ChildPreviousNamePage(Index(0), Index(0)), childName),
          submitAnswer(AddChildPreviousNamePage(Index(0), Some(Index(0))), true),
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
  }

  "users whose child was registered in England" - {

    "where their birth certificate has a system number" - {

      "must be asked for the birth certificate system number" in {

        val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

        startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
          .run(
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), BirthCertificateSystemNumber("123456789")),
            submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
            pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
          )
      }
    }

    "where their birth certificate does not have a system number" - {

      "must not be asked for the birth certificate system number" in {

        val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

        startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
          .run(
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), England),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), false),
            submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
            pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
          )
      }
    }
  }

  "users whose child was registered in Wales" - {

    "where their birth certificate has a system number" - {

      "must be asked for the birth certificate system number" in {

        val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

        startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
          .run(
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), true),
            submitAnswer(ChildBirthCertificateSystemNumberPage(Index(0)), BirthCertificateSystemNumber("123456789")),
            submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
            pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
          )
      }
    }

    "where their birth certificate does not have a system number" - {

      "must not be asked for the birth certificate system number" in {

        val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

        startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
          .run(
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Wales),
            submitAnswer(BirthCertificateHasSystemNumberPage(Index(0)), false),
            submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
            pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
          )
      }
    }
  }

  "users whose child was registered in Scotland" - {

    "where their birth certificate has a system number" - {

      "must be asked for the birth certificate system number" in {

        val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

        startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
          .run(
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), true),
            submitAnswer(ChildScottishBirthCertificateDetailsPage(Index(0)), arbitrary[ScottishBirthCertificateDetails].sample.value),
            submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
            pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
          )
      }
    }

    "where their birth certificate does not have a system number" - {

      "must not be asked for the birth certificate system number" in {

        val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

        startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
          .run(
            submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Scotland),
            submitAnswer(ScottishBirthCertificateHasNumbersPage(Index(0)), false),
            submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
            submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
            pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
          )
      }
    }
  }

  "users whose child was registered in Northern Ireland" - {

    "must not be asked for birth certificate details" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland),
          submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
        )
    }
  }

  "users whose child was registered outside of the UK" - {

    "must not be asked for birth certificate details" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Other),
          submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
        )
    }
  }

  "users whose child's country of registration is unknown" - {

    "must not be asked for birth certificate details" in {

      val relationship = arbitrary[ApplicantRelationshipToChild].sample.value

      startingFrom(ChildBirthRegistrationCountryPage(Index(0)))
        .run(
          submitAnswer(ChildBirthRegistrationCountryPage(Index(0)), Unknown),
          submitAnswer(AdoptingThroughLocalAuthorityPage(Index(0)), false),
          submitAnswer(ApplicantRelationshipToChildPage(Index(0)), relationship),
          pageMustBe(AnyoneClaimedForChildBeforePage(Index(0)))
        )
    }
  }

  "users whose child has been claimed for before" - {

    "who know the person who claimed for the child before" - {

      "and know that person's UK address" - {

        "must be asked for the person's name and address and proceed to be asked if the child lives with them" in {

          val country = arbitrary[ChildBirthRegistrationCountry].sample.value

          val initialise = journeyOf(
            setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), country)
          )

          startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
            .run(
              initialise,
              submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
              submitAnswer(PreviousClaimantNameKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantNamePage(Index(0)), adultName),
              submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), true),
              submitAnswer(PreviousClaimantUkAddressPage(Index(0)), ukAddress),
              pageMustBe(ChildLivesWithApplicantPage(Index(0)))
            )
        }
      }

      "and know that person's international address" - {

        "must be asked for the person's name address and proceed to be asked if the child lives with them" in {

          val country = arbitrary[ChildBirthRegistrationCountry].sample.value

          val initialise = journeyOf(
            setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), country)
          )

          startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
            .run(
              initialise,
              submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
              submitAnswer(PreviousClaimantNameKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantNamePage(Index(0)), adultName),
              submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantAddressInUkPage(Index(0)), false),
              submitAnswer(PreviousClaimantInternationalAddressPage(Index(0)), internationalAddress),
              pageMustBe(ChildLivesWithApplicantPage(Index(0)))
            )
        }
      }

      "and do not know that person's address" - {

        "must be asked for the person's name and proceed to be asked if the child lives with them" in {

          val country = arbitrary[ChildBirthRegistrationCountry].sample.value

          val initialise = journeyOf(
            setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), country)
          )

          startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
            .run(
              initialise,
              submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
              submitAnswer(PreviousClaimantNameKnownPage(Index(0)), true),
              submitAnswer(PreviousClaimantNamePage(Index(0)), adultName),
              submitAnswer(PreviousClaimantAddressKnownPage(Index(0)), false),
              pageMustBe(ChildLivesWithApplicantPage(Index(0)))
            )
        }
      }

    }

    "who do not know the person who claimed for the child before" - {

      "must proceed to be asked if the child lives with them" in {

        val country = arbitrary[ChildBirthRegistrationCountry].sample.value

        val initialise = journeyOf(
          setUserAnswerTo(ChildBirthRegistrationCountryPage(Index(0)), country)
        )

        startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
          .run(
            initialise,
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), true),
            submitAnswer(PreviousClaimantNameKnownPage(Index(0)), false),
            pageMustBe(ChildLivesWithApplicantPage(Index(0)))
          )
      }
    }
  }

  "users whose child lives with them" - {

    "must be asked if the child has lived with anyone else in the past year" in {

      startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
        .run(
          submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
          submitAnswer(ChildLivesWithApplicantPage(Index(0)), true),
          pageMustBe(ChildLivedWithAnyoneElsePage(Index(0)))
        )
    }
  }


  "users whose child does not live with them" - {

    "who know the person the child lives with" - {

      "and know that person's UK address" - {

        "must be asked for that person's name and address, then proceed to Check Child Details" in {

          startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
            .run(
              submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
              submitAnswer(ChildLivesWithApplicantPage(Index(0)), false),
              submitAnswer(GuardianNameKnownPage(Index(0)), true),
              submitAnswer(GuardianNamePage(Index(0)), adultName),
              submitAnswer(GuardianAddressKnownPage(Index(0)), true),
              submitAnswer(GuardianAddressInUkPage(Index(0)), true),
              submitAnswer(GuardianUkAddressPage(Index(0)), ukAddress),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }

      "and know that person's international address" - {

        "must be asked for that person's name and address, then proceed to Check Child Details" in {

          startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
            .run(
              submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
              submitAnswer(ChildLivesWithApplicantPage(Index(0)), false),
              submitAnswer(GuardianNameKnownPage(Index(0)), true),
              submitAnswer(GuardianNamePage(Index(0)), adultName),
              submitAnswer(GuardianAddressKnownPage(Index(0)), true),
              submitAnswer(GuardianAddressInUkPage(Index(0)), false),
              submitAnswer(GuardianInternationalAddressPage(Index(0)), internationalAddress),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }

      "and do not know that person's address" - {

        "must be asked for that person's name, then proceed to Check Child Details" in {

          startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
            .run(
              submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
              submitAnswer(ChildLivesWithApplicantPage(Index(0)), false),
              submitAnswer(GuardianNameKnownPage(Index(0)), true),
              submitAnswer(GuardianNamePage(Index(0)), adultName),
              submitAnswer(GuardianAddressKnownPage(Index(0)), false),
              pageMustBe(CheckChildDetailsPage(Index(0)))
            )
        }
      }
    }

    "who do not know the person the child lives with" - {

      "must proceed to Check Child Details" in {

        startingFrom(AnyoneClaimedForChildBeforePage(Index(0)))
          .run(
            submitAnswer(AnyoneClaimedForChildBeforePage(Index(0)), false),
            submitAnswer(ChildLivesWithApplicantPage(Index(0)), false),
            submitAnswer(GuardianNameKnownPage(Index(0)), false),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }
  }

  "users whose child has not lived with someone else in the past year" - {

    "must go to Check Child Details" in {

      startingFrom(ChildLivedWithAnyoneElsePage(Index(0)))
        .run(
          submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), false),
          pageMustBe(CheckChildDetailsPage(Index(0)))
        )
    }
  }


  "users whose child lived with someone else in the past year" - {

    "who know who the child lived with" - {

      "and know their UK address" - {

        "must be asked for the persons name and address, and whether they know their phone number" in {

          startingFrom(ChildLivedWithAnyoneElsePage(Index(0)))
            .run(
              submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), true),
              submitAnswer(PreviousGuardianNameKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianNamePage(Index(0)), adultName),
              submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), true),
              submitAnswer(PreviousGuardianUkAddressPage(Index(0)), ukAddress),
              pageMustBe(PreviousGuardianPhoneNumberKnownPage(Index(0)))
            )
        }
      }

      "and know their international address" - {

        "must be asked for the persons name and address, and whether they know their phone number" in {

          startingFrom(ChildLivedWithAnyoneElsePage(Index(0)))
            .run(
              submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), true),
              submitAnswer(PreviousGuardianNameKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianNamePage(Index(0)), adultName),
              submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianAddressInUkPage(Index(0)), false),
              submitAnswer(PreviousGuardianInternationalAddressPage(Index(0)), internationalAddress),
              pageMustBe(PreviousGuardianPhoneNumberKnownPage(Index(0)))
            )
        }
      }

      "and do not know their address" - {

        "must be asked whether they know their phone number" in {

          startingFrom(ChildLivedWithAnyoneElsePage(Index(0)))
            .run(
              submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), true),
              submitAnswer(PreviousGuardianNameKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianNamePage(Index(0)), adultName),
              submitAnswer(PreviousGuardianAddressKnownPage(Index(0)), false),
              pageMustBe(PreviousGuardianPhoneNumberKnownPage(Index(0)))
            )
        }
      }

      "and know their phone number" - {

        "must be asked for the phone number" in {

          startingFrom(PreviousGuardianPhoneNumberKnownPage(Index(0)))
            .run(
              submitAnswer(PreviousGuardianPhoneNumberKnownPage(Index(0)), true),
              submitAnswer(PreviousGuardianPhoneNumberPage(Index(0)), "0777777777"),
              pageMustBe(DateChildStartedLivingWithApplicantPage(Index(0)))
            )
        }
      }

      "and do not know their phone number" - {

        "must proceed to Date Child Started Living with Applicant" in {

          startingFrom(PreviousGuardianPhoneNumberKnownPage(Index(0)))
            .run(
              submitAnswer(PreviousGuardianPhoneNumberKnownPage(Index(0)), false),
              pageMustBe(DateChildStartedLivingWithApplicantPage(Index(0)))
            )
        }
      }
    }

    "who do not know who the child lived with" - {

      "must be asked when the child came to live with them" in {

        startingFrom(ChildLivedWithAnyoneElsePage(Index(0)))
          .run(
            submitAnswer(ChildLivedWithAnyoneElsePage(Index(0)), true),
            submitAnswer(PreviousGuardianNameKnownPage(Index(0)), false),
            submitAnswer(DateChildStartedLivingWithApplicantPage(Index(0)), LocalDate.now),
            pageMustBe(CheckChildDetailsPage(Index(0)))
          )
      }
    }
  }

  "users must be able to add more children" in {

    startingFrom(AddChildPage())
      .run(
        setUserAnswerTo(ChildNamePage(Index(0)), childName),
        submitAnswer(AddChildPage(), true),
        pageMustBe(ChildNamePage(Index(1)))
      )
  }

  "when users do not want to add any more children" - {

    "they must go to the task list" in {

      startingFrom(AddChildPage())
        .run(
          submitAnswer(AddChildPage(), false),
          pageMustBe(TaskListPage)
        )
    }
  }
}
