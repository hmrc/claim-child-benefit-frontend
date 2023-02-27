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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ClaimSpec extends AnyFreeSpec with Matchers with Generators with OptionValues {

  ".build" - {

    val hmfAbroad = arbitrary[Boolean].sample.value
    val nino = arbitrary[Nino].sample.value.nino

    def paymentPreference: JourneyModel.PaymentPreference = {
      Gen.oneOf(
        Gen.const(JourneyModel.PaymentPreference.Weekly(None, None)),
        Gen.const(JourneyModel.PaymentPreference.EveryFourWeeks(None, None)),
        Gen.const(JourneyModel.PaymentPreference.ExistingAccount(
          JourneyModel.EldestChild(
            arbitrary[models.ChildName].sample.value,
            LocalDate.now
          ),
          Gen.oneOf(models.PaymentFrequency.values).sample.value
        ))
      )
    }.sample.value

    def alwaysAbroad: JourneyModel.Residency.AlwaysLivedAbroad = {
      for {
        country <- arbitrary[Country]
        employment <- Gen.listOf(arbitrary[EmploymentStatus])
        countriesWorked <- Gen.listOf(arbitrary[Country])
        countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
      } yield JourneyModel.Residency.AlwaysLivedAbroad(country, employment.toSet, countriesWorked, countriesReceivedBenefits)
    }.sample.value

    val basicJourneyModel = JourneyModel(
      applicant = JourneyModel.Applicant(
        name = arbitrary[AdultName].sample.value,
        previousFamilyNames = Nil,
        dateOfBirth = LocalDate.now,
        nationalInsuranceNumber = Some(nino),
        currentAddress = arbitrary[models.UkAddress].sample.value,
        previousAddress = None, telephoneNumber = "0777777777",
        nationalities = NonEmptyList(genUkCtaNationality.sample.value, Gen.listOf(arbitrary[models.Nationality]).sample.value),
        residency = JourneyModel.Residency.AlwaysLivedInUk,
        memberOfHMForcesOrCivilServantAbroad = hmfAbroad,
        currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
        changedDesignatoryDetails = Some(false),
        correspondenceAddress = None
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
      additionalInformation = None,
      userAuthenticated = true
    )

    "must set `other eligibility fail` to false when there is no additional information" in {

      val claim = Claim.build(nino, basicJourneyModel)

      claim.otherEligibilityFailure mustBe false
    }

    "must set `other eligibility fail` to true when there is any additional information" in {

      val model = basicJourneyModel.copy(additionalInformation = Some("info"))

      val claim = Claim.build(nino, model)

      claim.otherEligibilityFailure mustBe true
    }
  }
}
