/*
 * Copyright 2024 HM Revenue & Customs
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

package models.journey

import generators.ModelGenerators
import models.ApplicantRelationshipToChild._
import models.ChildBirthRegistrationCountry._
import models.DocumentType._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.child._
import play.api.i18n.{DefaultMessagesApi, Messages}
import play.api.test.FakeRequest
import queries.{AllChildPreviousNames, AllChildSummaries}

import java.time.LocalDate

class ChildSpec
    extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues
    with ScalaCheckPropertyChecks {

  val testMessages = Map(
    "default" -> Map("title" -> "foo bar")
  )
  val messagesApi = new DefaultMessagesApi(testMessages)
  implicit val messages: Messages = messagesApi.preferred(FakeRequest("GET", "/"))

  private val adultName = arbitrary[AdultName].sample.value
  private val childName = arbitrary[ChildName].sample.value
  private val sex = arbitrary[ChildBiologicalSex].sample.value
  private val ukAddress = arbitrary[UkAddress].sample.value
  private val internationalAddress = arbitrary[InternationalAddress].sample.value
  private val systemNumber = BirthCertificateSystemNumber(Gen.listOfN(10, Gen.numChar).map(_.mkString).sample.value)
  private val relationshipToChild = Gen.oneOf(ApplicantRelationshipToChild.values).sample.value

  private val baseChild = Child(
    name = childName,
    nameChangedByDeedPoll = None,
    previousNames = Nil,
    biologicalSex = sex,
    dateOfBirth = LocalDate.now,
    countryOfRegistration = England,
    birthCertificateNumber = None,
    birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
    relationshipToApplicant = BirthChild,
    adoptingThroughLocalAuthority = false,
    previousClaimant = None,
    guardian = None,
    previousGuardian = None,
    dateChildStartedLivingWithApplicant = None
  )

  ".requiredDocuments" - {

    "must be empty if the child was born in E/S/W/NI and is not adopted" in {

      val gen = for {
        country      <- Gen.oneOf(England, Scotland, Wales, NorthernIreland)
        relationship <- Gen.oneOf(BirthChild, StepChild, OtherRelationship)
      } yield (country, relationship)

      forAll(gen) { case (country, relationship) =>
        val child = baseChild.copy(
          countryOfRegistration = country,
          relationshipToApplicant = relationship
        )

        child.requiredDocuments `mustBe` empty
      }
    }

    "must contain Birth Certificate and Travel Document if the child was born outside the UK" in {

      forAll(Gen.oneOf(OtherCountry, UnknownCountry)) { country =>
        baseChild.copy(countryOfRegistration = country).requiredDocuments `must` contain theSameElementsAs Seq(
          BirthCertificate,
          TravelDocument
        )
      }
    }

    "must contain Adoption Certificate if the child is adopted" in {

      baseChild.copy(relationshipToApplicant = AdoptedChild).requiredDocuments `must` contain `only` AdoptionCertificate
    }
  }

  ".possiblyLivedAbroadSeparately" - {

    "must be true when the date the child started living with the applicant is less than 3 months ago, and the previous guardian's address is known to be non-UK" in {

      val child = baseChild.copy(
        previousGuardian = Some(PreviousGuardian(Some(adultName), Some(internationalAddress), None)),
        dateChildStartedLivingWithApplicant = Some(LocalDate.now.minusMonths(3).plusDays(1))
      )

      child.possiblyLivedAbroadSeparately `mustEqual` true
    }

    "must be true when the date the child started living with the applicant is less than 3 months ago, and the previous guardian's address is not known" in {

      val child = baseChild.copy(
        previousGuardian = Some(PreviousGuardian(Some(adultName), None, None)),
        dateChildStartedLivingWithApplicant = Some(LocalDate.now.minusMonths(3).plusDays(1))
      )

      child.possiblyLivedAbroadSeparately `mustEqual` true
    }

    "must be false when the date the child started living with the applicant is less than 3 months ago or more, and the previous guardian's address is known to be UK" in {

      val child = baseChild.copy(
        previousGuardian = Some(PreviousGuardian(Some(adultName), Some(ukAddress), None)),
        dateChildStartedLivingWithApplicant = Some(LocalDate.now.minusMonths(3).plusDays(1))
      )

      child.possiblyLivedAbroadSeparately `mustEqual` false
    }

    "must be false when the date the child started living with the applicant is 3 months ago or more, and the previous guardian's address is known to be non-UK" in {

      val child = baseChild.copy(
        previousGuardian = Some(PreviousGuardian(Some(adultName), Some(internationalAddress), None)),
        dateChildStartedLivingWithApplicant = Some(LocalDate.now.minusMonths(3))
      )

      child.possiblyLivedAbroadSeparately `mustEqual` false
    }

    "must be false when the child has not lived with anyone else" in {

      baseChild.possiblyLivedAbroadSeparately `mustEqual` false
    }
  }

  ".possiblyRecentlyCaredForByLocalAuthority" - {

    "must be true when the date the child started living with the applicant is less than 3 months ago, and the previous guardian's UK address contains an LA keyword" in {

      val laAddress = ukAddress.copy(line1 = "Civic Council")
      val child = baseChild.copy(
        previousGuardian = Some(PreviousGuardian(Some(adultName), Some(laAddress), None)),
        dateChildStartedLivingWithApplicant = Some(LocalDate.now.minusMonths(3).plusDays(1))
      )

      child.possiblyRecentlyCaredForByLocalAuthority `mustEqual` true
    }

    "must be false when the date the child started living with the applicant is less than 3 months ago, and the previous guardian's UK address does not contain an LA keyword" in {

      val child = baseChild.copy(
        previousGuardian = Some(PreviousGuardian(Some(adultName), Some(ukAddress), None)),
        dateChildStartedLivingWithApplicant = Some(LocalDate.now.minusMonths(3).plusDays(1))
      )

      child.possiblyRecentlyCaredForByLocalAuthority `mustEqual` false
    }

    "must be false when the date the child started living with the applicant is 3 months ago or more, and the previous guardian's UK address contains an LA keyword" in {

      val laAddress = ukAddress.copy(line1 = "Civic Council")
      val child = baseChild.copy(
        previousGuardian = Some(PreviousGuardian(Some(adultName), Some(laAddress), None)),
        dateChildStartedLivingWithApplicant = Some(LocalDate.now.minusMonths(3))
      )

      child.possiblyRecentlyCaredForByLocalAuthority `mustEqual` false
    }
  }

  ".buildChildren" - {

    val minimalChildAnswers: UserAnswers =
      UserAnswers("id")
        .set(ChildNamePage(Index(0)), childName)
        .success
        .value
        .set(ChildHasPreviousNamePage(Index(0)), false)
        .success
        .value
        .set(ChildBiologicalSexPage(Index(0)), sex)
        .success
        .value
        .set(ChildDateOfBirthPage(Index(0)), LocalDate.now)
        .success
        .value
        .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.England)
        .success
        .value
        .set(BirthCertificateHasSystemNumberPage(Index(0)), false)
        .success
        .value
        .set(ApplicantRelationshipToChildPage(Index(0)), relationshipToChild)
        .success
        .value
        .set(AdoptingThroughLocalAuthorityPage(Index(0)), false)
        .success
        .value
        .set(AnyoneClaimedForChildBeforePage(Index(0)), false)
        .success
        .value
        .set(ChildLivesWithApplicantPage(Index(0)), true)
        .success
        .value
        .set(ChildLivedWithAnyoneElsePage(Index(0)), false)
        .success
        .value

    def addFullChildAnswers(answers: UserAnswers, index: Index): UserAnswers =
      answers
        .set(ChildNamePage(index), childName)
        .success
        .value
        .set(ChildHasPreviousNamePage(index), true)
        .success
        .value
        .set(ChildNameChangedByDeedPollPage(index), true)
        .success
        .value
        .set(ChildPreviousNamePage(index, Index(0)), childName)
        .success
        .value
        .set(ChildBiologicalSexPage(index), sex)
        .success
        .value
        .set(ChildDateOfBirthPage(index), LocalDate.now)
        .success
        .value
        .set(ChildBirthRegistrationCountryPage(index), ChildBirthRegistrationCountry.England)
        .success
        .value
        .set(BirthCertificateHasSystemNumberPage(index), true)
        .success
        .value
        .set(ChildBirthCertificateSystemNumberPage(index), systemNumber)
        .success
        .value
        .set(ApplicantRelationshipToChildPage(index), relationshipToChild)
        .success
        .value
        .set(AdoptingThroughLocalAuthorityPage(index), false)
        .success
        .value
        .set(AnyoneClaimedForChildBeforePage(index), true)
        .success
        .value
        .set(PreviousClaimantNameKnownPage(index), false)
        .success
        .value
        .set(ChildLivesWithApplicantPage(index), false)
        .success
        .value
        .set(GuardianNameKnownPage(index), false)
        .success
        .value

    "must return a single child (sparsest case)" in {

      val (errors, data) = Child.buildChildren(minimalChildAnswers).pad

      data.value.toList `must` contain `only` Child(
        name = childName,
        nameChangedByDeedPoll = None,
        previousNames = Nil,
        biologicalSex = sex,
        dateOfBirth = LocalDate.now,
        countryOfRegistration = England,
        birthCertificateNumber = None,
        birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
        relationshipToApplicant = relationshipToChild,
        adoptingThroughLocalAuthority = false,
        previousClaimant = None,
        guardian = None,
        previousGuardian = None,
        dateChildStartedLivingWithApplicant = None
      )
      errors `must` `not` `be` defined
    }

    "must return multiple children (including full case)" in {

      val answers = addFullChildAnswers(minimalChildAnswers, Index(1))

      val (errors, data) = Child.buildChildren(answers).pad

      data.value.toList must contain theSameElementsAs Seq(
        Child(
          name = childName,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = sex,
          dateOfBirth = LocalDate.now,
          countryOfRegistration = England,
          birthCertificateNumber = None,
          birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
          relationshipToApplicant = relationshipToChild,
          adoptingThroughLocalAuthority = false,
          previousClaimant = None,
          guardian = None,
          previousGuardian = None,
          dateChildStartedLivingWithApplicant = None
        ),
        Child(
          name = childName,
          nameChangedByDeedPoll = Some(true),
          previousNames = List(childName),
          biologicalSex = sex,
          dateOfBirth = LocalDate.now,
          countryOfRegistration = England,
          birthCertificateNumber = Some(systemNumber),
          birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
          relationshipToApplicant = relationshipToChild,
          adoptingThroughLocalAuthority = false,
          previousClaimant = Some(PreviousClaimant(None, None)),
          guardian = Some(Guardian(None, None)),
          previousGuardian = None,
          dateChildStartedLivingWithApplicant = None
        )
      )
      errors `must` not `be` defined
    }

    "must return errors" - {

      "when mandatory details are missing" in {

        val answers = UserAnswers("id")

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` AllChildSummaries
      }

      "when the user said the child had other names, but they are missing and so is whether it was changed by deed poll" in {

        val answers =
          addFullChildAnswers(UserAnswers("id"), Index(0))
            .remove(ChildNameChangedByDeedPollPage(Index(0)))
            .success
            .value
            .remove(AllChildPreviousNames(Index(0)))
            .success
            .value

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList must contain theSameElementsAs Seq(
          ChildNameChangedByDeedPollPage(Index(0)),
          AllChildPreviousNames(Index(0))
        )
      }

      "when the birth is registered in England or Wales, but whether there is a system number is missing" in {

        val answers =
          minimalChildAnswers
            .remove(BirthCertificateHasSystemNumberPage(Index(0)))
            .success
            .value

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` BirthCertificateHasSystemNumberPage(Index(0))
      }

      "when the user said there was a system number but it is missing" in {

        val answers =
          minimalChildAnswers
            .set(BirthCertificateHasSystemNumberPage(Index(0)), true)
            .success
            .value

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` ChildBirthCertificateSystemNumberPage(Index(0))
      }

      "when the birth is registered in Scotland, but whether there is a registration number is missing" in {

        val answers =
          minimalChildAnswers
            .set(ChildBirthRegistrationCountryPage(Index(0)), Scotland)
            .success
            .value

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` ScottishBirthCertificateHasNumbersPage(Index(0))
      }

      "when the user said there was a Scottish registration number but it is missing" in {

        val answers =
          minimalChildAnswers
            .set(ChildBirthRegistrationCountryPage(Index(0)), Scotland)
            .success
            .value
            .set(ScottishBirthCertificateHasNumbersPage(Index(0)), true)
            .success
            .value

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` ChildScottishBirthCertificateDetailsPage(Index(0))
      }

      "when the birth is registered in Northern Ireland, but whether there is a registration number is missing" in {

        val answers =
          minimalChildAnswers
            .set(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland)
            .success
            .value

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` BirthCertificateHasNorthernIrishNumberPage(Index(0))
      }

      "when the user said there was a NI registration number but it is missing" in {

        val answers =
          minimalChildAnswers
            .set(ChildBirthRegistrationCountryPage(Index(0)), NorthernIreland)
            .success
            .value
            .set(BirthCertificateHasNorthernIrishNumberPage(Index(0)), true)
            .success
            .value

        val (errors, data) = Child.buildChildren(answers).pad

        data `must` not `be` defined
        errors.value.toChain.toList `must` contain `only` ChildNorthernIrishBirthCertificateNumberPage(Index(0))
      }
    }
  }
}
