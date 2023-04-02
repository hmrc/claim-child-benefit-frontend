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

import audit.AuditService
import cats.data.NonEmptyList
import connectors.{ClaimChildBenefitConnector, UserAllowListConnector}
import generators.Generators
import models._
import models.domain.Claim
import models.journey
import models.journey.JourneyModel
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.{Mockito, MockitoSugar}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.Configuration
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubmissionLimiterSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with ScalaFutures
    with Generators
    with OptionValues {

  private val mockAuditService = mock[AuditService]
  private val mockClaimChildBenefitConnector = mock[ClaimChildBenefitConnector]
  private val mockUserAllowListConnector = mock[UserAllowListConnector]
  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val configuration = Configuration(
    "allow-list-features.submission" -> "Submission"
  )

  override def beforeEach(): Unit = {
    Mockito.reset[Any](mockClaimChildBenefitConnector, mockUserAllowListConnector, mockAuditService)
    super.beforeEach()
  }

  private val nino = arbitrary[Nino].sample.value.nino
  private val correlationId = UUID.randomUUID()

  private val basicJourneyModel = JourneyModel(
    applicant = journey.Applicant(
      name = arbitrary[AdultName].sample.value,
      previousFamilyNames = Nil,
      dateOfBirth = LocalDate.now,
      nationalInsuranceNumber = Some(nino),
      currentAddress = arbitrary[models.UkAddress].sample.value,
      previousAddress = None, telephoneNumber = "0777777777",
      nationalities = NonEmptyList(genUkCtaNationality.sample.value, Gen.listOf(arbitrary[models.Nationality]).sample.value),
      residency = journey.Residency.AlwaysLivedInUk,
      memberOfHMForcesOrCivilServantAbroad = false,
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
    userAuthenticated = true
  )

  private val claim = Claim.build(nino, basicJourneyModel, hasClaimedChildBenefit = false, hasSettledStatus = None)

  "SubmissionsLimitedByAllowList" - {

    val limiter = new SubmissionsLimitedByAllowList(configuration, mockUserAllowListConnector, mockAuditService)

    ".allowedToSubmit" - {

      "must return true when the connector's checkAllowlist returns true" in {

        when(mockUserAllowListConnector.check(any(), any())(any())) thenReturn Future.successful(true)

        limiter.allowedToSubmit(nino).futureValue mustEqual true

        verify(mockUserAllowListConnector, times(1)).check(eqTo("Submission"), eqTo(nino))(any())
      }

      "must return false when the connector's checkAllowlist returns false" in {

        when(mockUserAllowListConnector.check(any(), any())(any())) thenReturn Future.successful(false)

        limiter.allowedToSubmit(nino).futureValue mustEqual false
      }
    }

    "recordSubmission" - {

      "must audit the submission and return Done" in {

        limiter.recordSubmission(basicJourneyModel, claim, correlationId).futureValue mustEqual Done
        verify(mockAuditService, times(1)).auditSubmissionToCbs(eqTo(basicJourneyModel), eqTo(claim), any())(any())
      }
    }
  }

  "SubmissionsNotLimited" - {

    val limiter = new SubmissionsNotLimited(mockAuditService)

    ".allowedToSubmit" - {

      "must return true" in {

        limiter.allowedToSubmit(nino).futureValue mustEqual true
      }
    }

    ".recordSubmission" - {

      "must audit the submission and return Done" in {

        limiter.recordSubmission(basicJourneyModel, claim, correlationId).futureValue mustEqual Done
        verify(mockAuditService, times(1)).auditSubmissionToCbs(eqTo(basicJourneyModel), eqTo(claim), any())(any())
      }
    }
  }

  "SubmissionsLimitedByThrottle" - {

    val limiter = new SubmissionsLimitedByThrottle(mockClaimChildBenefitConnector, mockAuditService)

    ".allowedToSubmit" - {

      "must return true when the connector's checkThrottleLimit returns a response that the limit has not been reached" in {

        val response = CheckLimitResponse(limitReached = false)
        when(mockClaimChildBenefitConnector.checkThrottleLimit()(any())) thenReturn Future.successful(response)

        limiter.allowedToSubmit(nino).futureValue mustEqual true
      }

      "must return false when the connector's checkThrottleLimit returns a response that the limit has been reached" in {

        val response = CheckLimitResponse(limitReached = true)
        when(mockClaimChildBenefitConnector.checkThrottleLimit()(any())) thenReturn Future.successful(response)

        limiter.allowedToSubmit(nino).futureValue mustEqual false
      }
    }

    ".recordSubmission" - {

      "must increment the throttle counter, audit the submission and  and return Done" in {

        when(mockClaimChildBenefitConnector.incrementThrottleCount()(any())) thenReturn Future.successful(Done)

        limiter.recordSubmission(basicJourneyModel, claim, correlationId)(hc).futureValue
        verify(mockClaimChildBenefitConnector, times(1)).incrementThrottleCount()(any())
        verify(mockAuditService, times(1)).auditSubmissionToCbs(eqTo(basicJourneyModel), eqTo(claim), any())(any())
      }
    }
  }
}
