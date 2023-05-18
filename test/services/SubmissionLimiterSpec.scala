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

import java.time.{Clock, Instant, LocalDate, ZoneId}
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

  private val now = Instant.now
  private val stubClock = Clock.fixed(now, ZoneId.systemDefault())
  private val recentClaim = RecentClaim(nino, now)

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
    userAuthenticated = true,
    reasonsNotToSubmit = Nil,
    otherEligibilityFailureReasons = Nil
  )

  private val claim = Claim.build(nino, basicJourneyModel, hasClaimedChildBenefit = false, settledStatusStartDate = None)

  "SubmissionsLimitedByAllowList" - {

    val limiter = new SubmissionsLimitedByAllowList(
      configuration,
      mockUserAllowListConnector,
      mockAuditService,
      mockClaimChildBenefitConnector,
      stubClock
    )

    "recordSubmission" - {

      "must audit the submission, record the recent claim and return Done" in {

        when(mockClaimChildBenefitConnector.recordRecentClaim(any())(any()))thenReturn Future.successful(Done)

        limiter.recordSubmission(nino, basicJourneyModel, claim, correlationId).futureValue mustEqual Done

        verify(mockAuditService, times(1)).auditSubmissionToCbs(eqTo(basicJourneyModel), eqTo(claim), any())(any())
        verify(mockClaimChildBenefitConnector, times(1)).recordRecentClaim(eqTo(recentClaim))(any())
      }
    }
  }

  "SubmissionsNotLimited" - {

    val limiter = new SubmissionsNotLimited(mockAuditService, stubClock, mockClaimChildBenefitConnector)


    ".recordSubmission" - {

      "must audit the submission, record the recent claim and return Done" in {

        when(mockClaimChildBenefitConnector.recordRecentClaim(any())(any()))thenReturn Future.successful(Done)

        limiter.recordSubmission(nino, basicJourneyModel, claim, correlationId).futureValue mustEqual Done

        verify(mockAuditService, times(1)).auditSubmissionToCbs(eqTo(basicJourneyModel), eqTo(claim), any())(any())
        verify(mockClaimChildBenefitConnector, times(1)).recordRecentClaim(eqTo(recentClaim))(any())
      }
    }
  }

  "SubmissionsLimitedByThrottle" - {

    val limiter = new SubmissionsLimitedByThrottle(
      configuration,
      mockClaimChildBenefitConnector,
      mockUserAllowListConnector,
      mockAuditService,
      stubClock
    )

    ".recordSubmission" - {

      "must increment the throttle counter, audit the submission, record the recent claim and return Done" in {

        when(mockClaimChildBenefitConnector.incrementThrottleCount()(any())) thenReturn Future.successful(Done)
        when(mockClaimChildBenefitConnector.recordRecentClaim(any())(any()))thenReturn Future.successful(Done)

        limiter.recordSubmission(nino, basicJourneyModel, claim, correlationId)(hc).futureValue

        verify(mockClaimChildBenefitConnector, times(1)).incrementThrottleCount()(any())
        verify(mockAuditService, times(1)).auditSubmissionToCbs(eqTo(basicJourneyModel), eqTo(claim), any())(any())
        verify(mockClaimChildBenefitConnector, times(1)).recordRecentClaim(eqTo(recentClaim))(any())
      }
    }
  }
}
