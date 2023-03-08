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

package services

import cats.data.NonEmptyList
import connectors.ClaimChildBenefitConnector
import generators.Generators
import models._
import models.domain.Claim
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class SubmissionLimiterSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with ScalaFutures
    with Generators
    with OptionValues {

  private val mockConnector = mock[ClaimChildBenefitConnector]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector)
    super.beforeEach()
  }

  private val nino = arbitrary[Nino].sample.value.nino

  private val basicJourneyModel = JourneyModel(
    applicant = JourneyModel.Applicant(
      name = arbitrary[AdultName].sample.value,
      previousFamilyNames = Nil,
      dateOfBirth = LocalDate.now,
      nationalInsuranceNumber = Some(nino),
      currentAddress = arbitrary[models.UkAddress].sample.value,
      previousAddress = None, telephoneNumber = "0777777777",
      nationalities = NonEmptyList(genUkCtaNationality.sample.value, Gen.listOf(arbitrary[models.Nationality]).sample.value),
      residency = JourneyModel.Residency.AlwaysLivedInUk,
      memberOfHMForcesOrCivilServantAbroad = false,
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

  private val claim = Claim.build(nino, basicJourneyModel)

  "SubmissionsLimitedByAllowList" - {

    val limiter = new SubmissionsLimitedByAllowList(mockConnector)

    ".allowedToSubmit" - {

      "must return true when the connector's checkAllowlist returns true" in {

        when(mockConnector.checkAllowlist()(any())) thenReturn Future.successful(true)

        limiter.allowedToSubmit(hc).futureValue mustEqual true
      }

      "must return false when the connector's checkAllowlist returns false" in {

        when(mockConnector.checkAllowlist()(any())) thenReturn Future.successful(false)

        limiter.allowedToSubmit(hc).futureValue mustEqual false
      }
    }

    "recordSubmission" - {

      "must return Done" in {

        limiter.recordSubmission(claim)(hc).futureValue mustEqual Done
      }
    }
  }

  "SubmissionsNotLimited" - {

    val limiter = new SubmissionsNotLimited

    ".allowedToSubmit" - {

      "must return true" in {

        limiter.allowedToSubmit(hc).futureValue mustEqual true
      }
    }

    ".recordSubmission" - {

      "must return Done" in {

        limiter.recordSubmission(claim)(hc).futureValue mustEqual Done
      }
    }
  }

  "SubmissionsLimitedByThrottle" - {

    val limiter = new SubmissionsLimitedByThrottle(mockConnector)

    ".allowedToSubmit" - {

      "must return true when the connector's checkThrottleLimit returns a response that the limit has not been reached" in {

        val response = CheckLimitResponse(limitReached = false)
        when(mockConnector.checkThrottleLimit()(any())) thenReturn Future.successful(response)

        limiter.allowedToSubmit(hc).futureValue mustEqual true
      }

      "must return false when the connector's checkThrottleLimit returns a response that the limit has been reached" in {

        val response = CheckLimitResponse(limitReached = true)
        when(mockConnector.checkThrottleLimit()(any())) thenReturn Future.successful(response)

        limiter.allowedToSubmit(hc).futureValue mustEqual false
      }
    }

    ".recordSubmission" - {

      "must increment the throttle counter and return Done" in {

        when(mockConnector.incrementThrottleCount()(any())) thenReturn Future.successful(Done)

        limiter.recordSubmission(claim)(hc).futureValue
        verify(mockConnector, times(1)).incrementThrottleCount()(any())
      }
    }
  }
}
