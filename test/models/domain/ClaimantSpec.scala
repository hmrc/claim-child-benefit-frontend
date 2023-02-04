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

import cats.data.NonEmptyList
import generators.ModelGenerators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ClaimantSpec extends AnyFreeSpec with Matchers with ModelGenerators with OptionValues {

  ".build" - {

    val hmfAbroad = arbitrary[Boolean].sample.value
    val nino = arbitrary[Nino].sample.value.nino

    val basicJourneyModel = JourneyModel(
      applicant = JourneyModel.Applicant(
        name = arbitrary[AdultName].sample.value,
        previousFamilyNames = Nil,
        dateOfBirth = LocalDate.now,
        nationalInsuranceNumber = None,
        currentAddress = arbitrary[models.UkAddress].sample.value,
        previousAddress = None, telephoneNumber = "0777777777",
        nationalities = NonEmptyList(models.Nationality.allNationalities.find(_.name == "British").get, Gen.listOf(Gen.oneOf(models.Nationality.allNationalities)).sample.value),
        residency = JourneyModel.Residency.AlwaysLivedInUk,
        memberOfHMForcesOrCivilServantAbroad = hmfAbroad,
        currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming
      ),
      relationship = JourneyModel.Relationship(RelationshipStatus.Single, None, None),
      children = NonEmptyList(
        JourneyModel.Child(
          name = arbitrary[models.ChildName].sample.value,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = Gen.oneOf(models.ChildBiologicalSex.values).sample.value,
          dateOfBirth = LocalDate.now,
          countryOfRegistration = Gen.oneOf(models.ChildBirthRegistrationCountry.values).sample.value,
          birthCertificateNumber = None,
          birthCertificateDetailsMatched = models.BirthRegistrationMatchingResult.NotAttempted,
          relationshipToApplicant = arbitrary[models.ApplicantRelationshipToChild].sample.value,
          adoptingThroughLocalAuthority = false,
          previousClaimant = None,
          guardian = None,
          previousGuardian = None,
          dateChildStartedLivingWithApplicant = None
        ),
        Nil
      ),
      benefits = None,
      paymentPreference = JourneyModel.PaymentPreference.DoNotPay(None),
      additionalInformation = models.AdditionalInformation.NoInformation
    )

    "must return a UK/CTA claimant who has always been resident in the UK" - {

      "when the claimant opts out of HICBC" in {

        val result = Claimant.build(nino, basicJourneyModel)

        result mustEqual UkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = hmfAbroad,
          hicbcOptOut = true
        )
      }

      "when the claimant does not opt out of HICBC" in {

        val paymentPreference = Gen.oneOf(
          Gen.const(JourneyModel.PaymentPreference.Weekly(None, None)),
          Gen.const(JourneyModel.PaymentPreference.EveryFourWeeks(None, None)),
          Gen.const(JourneyModel.PaymentPreference.ExistingAccount(
            JourneyModel.EldestChild(
              arbitrary[models.ChildName].sample.value,
              LocalDate.now
            ),
            Gen.oneOf(models.PaymentFrequency.values).sample.value
          ))
        ).sample.value

        val journeyModel = basicJourneyModel.copy(paymentPreference = paymentPreference)

        val result = Claimant.build(nino, journeyModel)

        result mustEqual UkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = hmfAbroad,
          hicbcOptOut = false
        )
      }
    }

    "must return a UK/CTA claimant who has not always been resident in the UK" - {


      "when the claimant opts out of HICBC" in {

        val result = Claimant.build(nino, basicJourneyModel)

        result mustEqual UkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = hmfAbroad,
          hicbcOptOut = true
        )
      }

    }

  }

  ".writes" - {

    "must write a UK/CTA claimant who has always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = UkCtaClaimantAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "UK_OR_CTA",
        "alwaysLivedInUK" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a UK/CTA claimant who has not always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = UkCtaClaimantNotAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        last3MonthsInUK = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "UK_OR_CTA",
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a non-UK/CTA claimant who has always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = NonUkCtaClaimantAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        nationality = Nationality.Eea,
        rightToReside = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "EEA",
        "alwaysLivedInUK" -> true,
        "rightToReside" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a non-UK/CTA claimant who has not always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = NonUkCtaClaimantNotAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        last3MonthsInUK = true,
        nationality = Nationality.Eea,
        rightToReside = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "EEA",
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> true,
        "rightToReside" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }
  }
}
