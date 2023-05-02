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
import cats.data.NonEmptyList
import config.FeatureFlags
import connectors.ImmigrationStatusConnector
import generators.Generators
import models._
import models.immigration.ImmigrationStatus.{eus, ilr}
import models.immigration.{ImmigrationStatus, StatusCheckResult}
import models.journey
import models.journey.JourneyModel
import org.mockito.ArgumentMatchers.any
import org.scalacheck.Arbitrary.arbitrary
import org.mockito.{Mockito, MockitoSugar}
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ImmigrationStatusServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach with Generators with GuiceOneAppPerSuite {

  private val mockFeatureFlags = mock[FeatureFlags]
  private val mockConnector = mock[ImmigrationStatusConnector]

  private val configuration: Configuration = app.injector.instanceOf[Configuration]

  override def beforeEach(): Unit = {
    Mockito.reset(mockFeatureFlags)
    Mockito.reset(mockConnector)
    super.beforeEach()
  }

  private val service = new ImmigrationStatusService(mockFeatureFlags, mockConnector, configuration, clockAtFixedInstant)

  private implicit val hc: HeaderCarrier = new HeaderCarrier()
  private val correlationId = UUID.randomUUID()
  private val hmfAbroad = arbitrary[Boolean].sample.value
  private val nino = arbitrary[Nino].sample.value.nino

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

  "hasSettledStatus" - {

    "when the check immigration status feature is off" - {

      "must return None" in {

        val nationality = Nationality.allNationalities.filter(_.group == NationalityGroup.Eea).head
        val model = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(nationalities = NonEmptyList(nationality, Nil)))

        when(mockFeatureFlags.checkImmigrationStatus).thenReturn(false)

        val result = service.settledStatusStartDate(nino, model, correlationId)(hc).futureValue

        result must not be defined
        verify(mockConnector, never).checkStatus(any(), any())(any())
      }
    }

    "when the check immigration status feature is on" - {

      "and the applicant is an EU national" - {

        "must return the result of the call to check immigration status" in {

          val nationality = Nationality.allNationalities.filter(_.group == NationalityGroup.Eea).head
          val model = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(nationalities = NonEmptyList(nationality, Nil)))
          val settledStatusDate = LocalDate.now

          val immigrationStatusWithSettledStatus = ImmigrationStatus(
            statusStartDate = settledStatusDate,
            statusEndDate = None,
            productType = eus,
            immigrationStatus = ilr,
            noRecourseToPublicFunds = false
          )

          val response = StatusCheckResult(
            fullName = "name",
            dateOfBirth = LocalDate.now,
            nationality = nationality.name,
            statuses = List(immigrationStatusWithSettledStatus)
          )

          when(mockFeatureFlags.checkImmigrationStatus).thenReturn(true)
          when(mockConnector.checkStatus(any(), any())(any())).thenReturn(Future.successful(response))

          val result = service.settledStatusStartDate(nino, model, correlationId)(hc).futureValue

          result.value mustEqual settledStatusDate
          verify(mockConnector, times(1)).checkStatus(any(), any())(any())
        }
      }

      "and the applicant is not an EU national" - {

        "must return None" in {

          val nationality = Nationality.allNationalities.filter(_.group == NationalityGroup.NonEea).head
          val model = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(nationalities = NonEmptyList(nationality, Nil)))

          when(mockFeatureFlags.checkImmigrationStatus).thenReturn(true)

          val result = service.settledStatusStartDate(nino, model, correlationId)(hc).futureValue

          result must not be defined
          verify(mockConnector, never).checkStatus(any(), any())(any())
        }
      }

      "and the connector call fails" - {

        "must return None" in {

          val nationality = Nationality.allNationalities.filter(_.group == NationalityGroup.Eea).head
          val model = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(nationalities = NonEmptyList(nationality, Nil)))

          when(mockFeatureFlags.checkImmigrationStatus).thenReturn(true)
          when(mockConnector.checkStatus(any(), any())(any())).thenReturn(Future.failed(new RuntimeException("foo")))

          val result = service.settledStatusStartDate(nino, model, correlationId)(hc).futureValue

          result must not be defined
        }
      }
    }
  }
}
