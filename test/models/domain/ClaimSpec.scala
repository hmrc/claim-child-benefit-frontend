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

package models.domain

import cats.data.NonEmptyList
import generators.Generators
import models._
import models.journey
import models.journey.JourneyModel
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ClaimSpec extends AnyFreeSpec with Matchers with Generators with OptionValues with ScalaCheckPropertyChecks {

  ".build" - {

    val hmfAbroad = arbitrary[Boolean].sample.value
    val nino = arbitrary[Nino].sample.value.nino

    val basicChild = journey.Child(
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
    )

    val basicJourneyModel = JourneyModel(
      applicant = journey.Applicant(
        name = arbitrary[AdultName].sample.value,
        previousFamilyNames = Nil,
        dateOfBirth = LocalDate.now,
        nationalInsuranceNumber = Some(nino),
        currentAddress = arbitrary[models.UkAddress].sample.value,
        previousAddress = None, telephoneNumber = "0777777777",
        nationalities = NonEmptyList(genUkCtaNationality.sample.value, Gen.listOf(arbitrary[models.Nationality]).sample.value),
        residency = journey.Residency.AlwaysLivedInUk,
        memberOfHMForcesOrCivilServantAbroad = hmfAbroad,
        currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
        changedDesignatoryDetails = Some(false),
        correspondenceAddress = None
      ),
      relationship = journey.Relationship(RelationshipStatus.Single, None, None),
      children = NonEmptyList(basicChild, Nil),
      benefits = None,
      paymentPreference = journey.PaymentPreference.DoNotPay(None),
      userAuthenticated = true,
      reasonsNotToSubmit = Nil,
      otherEligibilityFailureReasons = Nil
    )

    "must set `other eligibility fail` to false when the journey model has no other eligibility fail reasons" in {

      val claim = Claim.build(nino, basicJourneyModel, hasClaimedChildBenefit = false, settledStatusStartDate = None)

      claim.otherEligibilityFailure mustBe false
    }

    "must set `other eligibility fail` to true when the journey model has any other eligibility fail reasons" in {

      val model = basicJourneyModel.copy(otherEligibilityFailureReasons = Seq(OtherEligibilityFailReason.ApplicantWorkedAbroad))

      val claim = Claim.build(nino, model, hasClaimedChildBenefit = false, settledStatusStartDate = None)

      claim.otherEligibilityFailure mustBe true
    }

    "must set settled status on the Claimant to `true` when the settled status date is 3 months or more in the past" in {

      forAll(datesBetween(LocalDate.now.minusYears(10), LocalDate.now.minusMonths(3))) {
        settledStatusDate =>

          val nationality = models.Nationality.allNationalities.filter(_.group == models.NationalityGroup.Eea).head
          val model =
            basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(nationalities = NonEmptyList(nationality, Nil)),
              children = NonEmptyList(basicChild.copy(dateOfBirth = LocalDate.now.minusYears(1)), Nil)
            )

          val claim = Claim.build(nino, model, hasClaimedChildBenefit = false, settledStatusStartDate = Some(settledStatusDate))

          claim.claimant.asInstanceOf[NonUkCtaClaimantAlwaysResident].rightToReside mustEqual true
      }
    }

    "must set settled status on the Claimant to `true` when the settled status date is on or before the oldest child's birth date" in {

      val gen = for {
        settledStatusDate <- datesBetween(LocalDate.now.minusMonths(3), LocalDate.now.minusDays(1))
        childBirthDate    <- datesBetween(settledStatusDate, LocalDate.now)
      } yield (settledStatusDate, childBirthDate)

      forAll(gen) {
        case (settledStatusDate, childBirthDate) =>

          val nationality = models.Nationality.allNationalities.filter(_.group == models.NationalityGroup.Eea).head
          val model =
            basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(nationalities = NonEmptyList(nationality, Nil)),
              children = NonEmptyList(basicChild.copy(dateOfBirth = childBirthDate), Nil)
            )

          val claim = Claim.build(nino, model, hasClaimedChildBenefit = false, settledStatusStartDate = Some(settledStatusDate))

          claim.claimant.asInstanceOf[NonUkCtaClaimantAlwaysResident].rightToReside mustEqual true
      }
    }

    "must set settled status on the Claimant to `false` when a settled status date is after the oldest child's birth date and less than 3 months ago" - {

      val gen = for {
        childBirthDate <- datesBetween(LocalDate.now.minusMonths(3), LocalDate.now.minusDays(2))
        settledStatusDate <- datesBetween(childBirthDate.plusDays(1), LocalDate.now)
      } yield (settledStatusDate, childBirthDate)

      forAll(gen) {
        case (settledStatusDate, childBirthDate) =>

          val nationality = models.Nationality.allNationalities.filter(_.group == models.NationalityGroup.Eea).head
          val model =
            basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(nationalities = NonEmptyList(nationality, Nil)),
              children = NonEmptyList(basicChild.copy(dateOfBirth = childBirthDate), Nil)
            )

          val claim = Claim.build(nino, model, hasClaimedChildBenefit = false, settledStatusStartDate = Some(settledStatusDate))

          claim.claimant.asInstanceOf[NonUkCtaClaimantAlwaysResident].rightToReside mustEqual false
      }
    }
  }
}
