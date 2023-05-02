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

import base.SpecBase
import config.FeatureFlags
import connectors.ClaimChildBenefitConnector
import generators.ModelGenerators
import models.PartnerClaimingChildBenefit.{GettingPayments, NotClaiming, NotGettingPayments, WaitingToHear}
import models._
import models.requests.{AuthenticatedIdentifierRequest, DataRequest, UnauthenticatedIdentifierRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages.applicant._
import pages.child._
import pages.partner._
import pages.payments._
import play.api.test.FakeRequest
import services.ClaimSubmissionService._
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ClaimSubmissionServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach with ModelGenerators {

  private val mockFeatureFlags = mock[FeatureFlags]
  private val mockConnector = mock[ClaimChildBenefitConnector]
  private val mockSubmissionLimiter = mock[SubmissionLimiter]
  private val mockSupplementaryDataService = mock[SupplementaryDataService]
  private val mockImmigrationStatusService = mock[ImmigrationStatusService]
  private val journeyModelService = new JourneyModelService(mockFeatureFlags)

  override def beforeEach(): Unit = {
    Mockito.reset(mockFeatureFlags)
    Mockito.reset(mockConnector)
    Mockito.reset(mockSubmissionLimiter)
    Mockito.reset(mockSupplementaryDataService)
    Mockito.reset(mockImmigrationStatusService)
    super.beforeEach()
  }

  private val submissionService = new ClaimSubmissionService(
    mockFeatureFlags,
    mockConnector,
    mockSubmissionLimiter,
    mockSupplementaryDataService,
    mockImmigrationStatusService,
    journeyModelService
  )

  private val nino = arbitrary[Nino].sample.value
  private val userId = "user id"
  private val baseRequest = FakeRequest("", "")

  private val now = LocalDate.now
  private val adultName = AdultName(None, "first", None, "last")
  private val currentUkAddress = UkAddress("line 1", None, "town", None, "AA11 1AA")
  private val phoneNumber = "07777 777777"
  private val nationality = Gen.oneOf(Nationality.allNationalities).sample.value

  private val childName = ChildName("first", None, "last")
  private val biologicalSex = ChildBiologicalSex.Female
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = BirthCertificateSystemNumber("000000000")

  private val designatoryDetails = DesignatoryDetails(
    Some(adultName),
    None,
    Some(NPSAddress("1", None, None, None, None, None, None)),
    None,
    LocalDate.now
  )
  private val relationshipDetails = RelationshipDetails(hasClaimedChildBenefit = false)

  private val basicUserAnswers =
    UserAnswers(
      "id",
      nino = Some(nino.nino),
      designatoryDetails = Some(designatoryDetails),
      relationshipDetails = Some(relationshipDetails)
    )
      .set(ApplicantNinoKnownPage, false).success.value
      .set(ApplicantNamePage, adultName).success.value
      .set(ApplicantHasPreviousFamilyNamePage, false).success.value
      .set(ApplicantDateOfBirthPage, now).success.value
      .set(ApplicantPhoneNumberPage, phoneNumber).success.value
      .set(ApplicantNationalityPage(Index(0)), nationality).success.value
      .set(ApplicantIsHmfOrCivilServantPage, false).success.value
      .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk).success.value
      .set(ApplicantCurrentUkAddressPage, currentUkAddress).success.value
      .set(ApplicantLivedAtCurrentAddressOneYearPage, true).success.value
      .set(ApplicantIsHmfOrCivilServantPage, false).success.value
      .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming).success.value
      .set(RelationshipStatusPage, RelationshipStatus.Single).success.value
      .set(ChildNamePage(Index(0)), childName).success.value
      .set(ChildHasPreviousNamePage(Index(0)), false).success.value
      .set(ChildBiologicalSexPage(Index(0)), biologicalSex).success.value
      .set(ChildDateOfBirthPage(Index(0)), now).success.value
      .set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.England).success.value
      .set(BirthCertificateHasSystemNumberPage(Index(0)), true).success.value
      .set(ChildBirthCertificateSystemNumberPage(Index(0)), systemNumber).success.value
      .set(ApplicantRelationshipToChildPage(Index(0)), relationshipToChild).success.value
      .set(AdoptingThroughLocalAuthorityPage(Index(0)), false).success.value
      .set(AnyoneClaimedForChildBeforePage(Index(0)), false).success.value
      .set(ChildLivesWithApplicantPage(Index(0)), true).success.value
      .set(ChildLivedWithAnyoneElsePage(Index(0)), false).success.value
      .set(ApplicantIncomePage, Income.BelowLowerThreshold).success.value
      .set(WantToBePaidPage, false).success.value
      .set(IncludeAdditionalInformationPage, false).success.value

  ".canSubmit" - {

    "when the submission feature is disabled" - {

      "must be false" in {

        val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
        val request = DataRequest(identifierRequest, userId, basicUserAnswers)

        when(mockFeatureFlags.allowSubmissionToCbs) thenReturn false
        when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false

        submissionService.canSubmit(request).futureValue mustEqual false
      }
    }

    "when the submission feature is enabled" - {

      "and the user is unauthenticated" - {

        "must be false" in {

          val identifierRequest = UnauthenticatedIdentifierRequest(baseRequest, userId)
          val request = DataRequest(identifierRequest, userId, basicUserAnswers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false

          submissionService.canSubmit(request).futureValue mustEqual false
        }
      }

      "and the user is authenticated" - {

        "must be false when the submission limiter returns false" in {

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, basicUserAnswers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(false)

          submissionService.canSubmit(request).futureValue mustEqual false

          verify(mockSubmissionLimiter, times(1)).allowedToSubmit(eqTo(nino.nino))(any())
        }

        "must be false if the user's answers are incomplete and cannot be built into a journey model" in {

          val answers = basicUserAnswers.remove(ApplicantPhoneNumberPage).success.value
          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be false when a child in the claim is over 6 months old" in {

          val dob = LocalDate.now(clockAtFixedInstant).minusMonths(6).minusDays(1)
          val answers = basicUserAnswers.set(ChildDateOfBirthPage(Index(0)), dob).success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be false when any documents need to be posted for a child" in {

          val answers = basicUserAnswers.set(ChildBirthRegistrationCountryPage(Index(0)), ChildBirthRegistrationCountry.NorthernIreland).success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be false when the user's designatory details are not correct (name)" in {

          val answers = basicUserAnswers.set(DesignatoryNamePage, adultName).success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be false when the user's designatory details are not correct (residential address)" in {

          val answers = basicUserAnswers.set(DesignatoryAddressInUkPage, true).success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be false when the user's designatory details are not correct (correspondence address)" in {

          val answers = basicUserAnswers.set(CorrespondenceAddressInUkPage, true).success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be false when the user's partner is claiming Child Benefit but no NINO is provided for them" in {

          val claiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value
          val answers =
            basicUserAnswers
              .set(RelationshipStatusPage, RelationshipStatus.Married).success.value
              .set(PartnerNamePage, adultName).success.value
              .set(PartnerNinoKnownPage, false).success.value
              .set(PartnerDateOfBirthPage, now).success.value
              .set(PartnerNationalityPage(Index(0)), nationality).success.value
              .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
              .set(PartnerIsHmfOrCivilServantPage, false).success.value
              .set(PartnerWorkedAbroadPage, false).success.value
              .set(PartnerReceivedBenefitsAbroadPage, false).success.value
              .set(PartnerClaimingChildBenefitPage, claiming).success.value
              .set(PartnerEldestChildNamePage, childName).success.value
              .set(PartnerEldestChildDateOfBirthPage, LocalDate.now).success.value
              .set(WantToBePaidPage, false).success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be false when the user gives any additional information" in {

          val answers =
            basicUserAnswers
              .set(IncludeAdditionalInformationPage, true).success.value
              .set(AdditionalInformationPage, "foo").success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
          when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
          when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

          submissionService.canSubmit(request).futureValue mustEqual false
        }

        "must be true when the submission limiter allows submission, they have not changed designatory details or added information, no children are over 6 or need to send documents" - {

          "and the user does not have a partner" in {

            val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
            val request = DataRequest(identifierRequest, userId, basicUserAnswers)

            when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
            when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
            when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

            submissionService.canSubmit(request).futureValue mustEqual true
          }

          "and the user has a partner" - {

            "who is not claiming Child Benefit" in {

              val answers =
                basicUserAnswers
                  .set(RelationshipStatusPage, RelationshipStatus.Married).success.value
                  .set(PartnerNamePage, adultName).success.value
                  .set(PartnerNinoKnownPage, false).success.value
                  .set(PartnerDateOfBirthPage, now).success.value
                  .set(PartnerNationalityPage(Index(0)), nationality).success.value
                  .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
                  .set(PartnerIsHmfOrCivilServantPage, false).success.value
                  .set(PartnerWorkedAbroadPage, false).success.value
                  .set(PartnerReceivedBenefitsAbroadPage, false).success.value
                  .set(PartnerClaimingChildBenefitPage, NotClaiming).success.value
                  .set(WantToBePaidPage, false).success.value

              val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
              val request = DataRequest(identifierRequest, userId, answers)

              when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
              when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
              when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

              submissionService.canSubmit(request).futureValue mustEqual true
            }

            "who is claiming Child Benefit, and the user has supplied their NINO" in {

              val claiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value
              val answers =
                basicUserAnswers
                  .set(RelationshipStatusPage, RelationshipStatus.Married).success.value
                  .set(PartnerNamePage, adultName).success.value
                  .set(PartnerNinoKnownPage, true).success.value
                  .set(PartnerNinoPage, nino).success.value
                  .set(PartnerDateOfBirthPage, now).success.value
                  .set(PartnerNationalityPage(Index(0)), nationality).success.value
                  .set(PartnerEmploymentStatusPage, EmploymentStatus.activeStatuses).success.value
                  .set(PartnerIsHmfOrCivilServantPage, false).success.value
                  .set(PartnerWorkedAbroadPage, false).success.value
                  .set(PartnerReceivedBenefitsAbroadPage, false).success.value
                  .set(PartnerClaimingChildBenefitPage, claiming).success.value
                  .set(PartnerEldestChildNamePage, childName).success.value
                  .set(PartnerEldestChildDateOfBirthPage, LocalDate.now).success.value
                  .set(WantToBePaidPage, false).success.value

              val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
              val request = DataRequest(identifierRequest, userId, answers)

              when(mockFeatureFlags.allowSubmissionToCbs) thenReturn true
              when(mockFeatureFlags.submitOlderChildrenToCbs) thenReturn false
              when(mockSubmissionLimiter.allowedToSubmit(any())(any())) thenReturn Future.successful(true)

              submissionService.canSubmit(request).futureValue mustEqual true
            }
          }
        }
      }
    }
  }

  ".submit" - {

    "when the user is authenticated" - {

      "and their answers can produce a valid Journey Model" - {

        "must submit a claim and record the submission" in {

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, basicUserAnswers)

          when(mockConnector.submitClaim(any(), any())(any())) thenReturn Future.successful(Done)
          when(mockSubmissionLimiter.recordSubmission(any(), any(), any())(any())) thenReturn Future.successful(Done)
          when(mockSupplementaryDataService.submit(any(), any(), any(), any())(any())) thenReturn Future.successful(Done)
          when(mockImmigrationStatusService.settledStatusStartDate(any(), any(), any())(any())) thenReturn Future.successful(None)

          submissionService.submit(request).futureValue

          verify(mockConnector, times(1)).submitClaim(any(), any())(any())
          verify(mockSubmissionLimiter, times(1)).recordSubmission(any(), any(), any())(any())
          verify(mockSupplementaryDataService, times(1)).submit(eqTo(nino.nino), any(), any(), any())(any())
        }

        "must submit a claim and return Done when recording the submission fails" in {

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, basicUserAnswers)

          when(mockConnector.submitClaim(any(), any())(any())) thenReturn Future.successful(Done)
          when(mockSubmissionLimiter.recordSubmission(any(), any(), any())(any())) thenReturn Future.failed(new Exception("foo"))
          when(mockSupplementaryDataService.submit(any(), any(), any(), any())(any())) thenReturn Future.successful(Done)
          when(mockImmigrationStatusService.settledStatusStartDate(any(), any(), any())(any())) thenReturn Future.successful(None)

          submissionService.submit(request).futureValue

          verify(mockConnector, times(1)).submitClaim(any(), any())(any())
          verify(mockSupplementaryDataService, times(1)).submit(eqTo(nino.nino), any(), any(), any())(any())
        }

        "must submit a claim and return Done when the supplementary data service fails" in {

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, basicUserAnswers)

          when(mockConnector.submitClaim(any(), any())(any())) thenReturn Future.successful(Done)
          when(mockSubmissionLimiter.recordSubmission(any(), any(), any())(any())) thenReturn Future.successful(Done)
          when(mockSupplementaryDataService.submit(any(), any(), any(), any())(any())) thenReturn Future.failed(new RuntimeException())
          when(mockImmigrationStatusService.settledStatusStartDate(any(), any(), any())(any())) thenReturn Future.successful(None)

          submissionService.submit(request).futureValue

          verify(mockConnector, times(1)).submitClaim(any(), any())(any())
          verify(mockSubmissionLimiter, times(1)).recordSubmission(any(), any(), any())(any())
          verify(mockSupplementaryDataService, times(1)).submit(eqTo(nino.nino), any(), any(), any())(any())
        }
      }

      "and their answers cannot produce a valid Journey Model" - {

        "must return a failed future (Cannot Build Journey Model)" in {

          val answers = basicUserAnswers.remove(ApplicantPhoneNumberPage).success.value

          val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino.nino)
          val request = DataRequest(identifierRequest, userId, answers)

          when(mockConnector.submitClaim(any(), any())(any())) thenReturn Future.successful(Done)

          val result = submissionService.submit(request).failed.futureValue
          result.getMessage mustEqual CannotBuildJourneyModelException.getMessage
        }
      }
    }

    "when the user is unauthenticated" - {

      "must return a failed future (Not Authenticated)" in {

        val answers = basicUserAnswers.copy(nino = None, designatoryDetails = None)
        val identifierRequest = UnauthenticatedIdentifierRequest(baseRequest, userId)
        val request = DataRequest(identifierRequest, userId, answers)

        when(mockConnector.submitClaim(any(), any())(any())) thenReturn Future.successful(Done)

        val result = submissionService.submit(request).failed.futureValue
        result.getMessage mustEqual NotAuthenticatedException.getMessage
      }
    }
  }
}
