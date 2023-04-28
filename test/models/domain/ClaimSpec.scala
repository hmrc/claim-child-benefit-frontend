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
import generators.Generators
import models._
import models.journey
import models.journey.JourneyModel
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ClaimSpec extends AnyFreeSpec with Matchers with Generators with OptionValues {

  ".build" - {

    val hmfAbroad = arbitrary[Boolean].sample.value
    val nino = arbitrary[Nino].sample.value.nino

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
      children = NonEmptyList(
        journey.Child(
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
      paymentPreference = journey.PaymentPreference.DoNotPay(None),
      additionalInformation = None,
      userAuthenticated = true,
      reasonsNotToSubmit = Nil,
      otherEligibilityFailureReasons = Nil
    )

    "must set `other eligibility fail` to false when the journey model has no other eligibility fail reasons" in {

      val claim = Claim.build(nino, basicJourneyModel, hasClaimedChildBenefit = false)

      claim.otherEligibilityFailure mustBe false
    }

    "must set `other eligibility fail` to true when the journey model has any other eligibility fail reasons" in {

      val model = basicJourneyModel.copy(otherEligibilityFailureReasons = Seq(OtherEligibilityFailReason.ApplicantWorkedAbroad))

      val claim = Claim.build(nino, model, hasClaimedChildBenefit = false)

      claim.otherEligibilityFailure mustBe true
    }
  }
}
