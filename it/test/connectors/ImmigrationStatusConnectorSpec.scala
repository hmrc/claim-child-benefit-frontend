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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import generators.ModelGenerators
import models.immigration.{ImmigrationStatus, NinoSearchRequest, StatusCheckRange, StatusCheckResult}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import java.util.UUID

class ImmigrationStatusConnectorSpec
  extends AnyFreeSpec
    with WireMockHelper
    with ScalaFutures
    with Matchers
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar
    with ModelGenerators {

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.home-office-immigration-status-proxy.port" -> server.port,
        "internal-auth.token" -> "authKey"
      )
      .build()

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  private val correlationId = UUID.randomUUID()
  private val nino = arbitrary[Nino].sample.value
  private val service = "claim-child-benefit-frontend"

  private lazy val connector = app.injector.instanceOf[ImmigrationStatusConnector]

  private val searchRange = StatusCheckRange(
    endDate = Some(LocalDate.of(2023, 4, 26)),
    startDate = Some(LocalDate.of(2022, 11, 26))
  )

  ".checkStatus" - {

    "must return a status check result when valid json is returned" in {

      val responseBody: String =
        s"""{
           |  "correlationId": "$correlationId",
           |  "result": {
           |    "dateOfBirth": "2001-01-31",
           |    "nationality": "IRL",
           |    "fullName": "First Last",
           |    "statuses": [
           |      {
           |        "productType": "EUS",
           |        "immigrationStatus": "ILR",
           |        "noRecourseToPublicFunds": true,
           |        "statusStartDate": "2018-01-31",
           |        "statusEndDate": "2018-12-12"
           |      }
           |    ]
           |  }
           |}""".stripMargin

      val expectedResponse = StatusCheckResult(
        fullName = "First Last",
        dateOfBirth = LocalDate.of(2001, 1, 31),
        nationality = "IRL",
        statuses = List(ImmigrationStatus(
          statusStartDate = LocalDate.of(2018, 1, 31),
          statusEndDate = Some(LocalDate.of(2018, 12, 12)),
          productType = "EUS",
          immigrationStatus = "ILR",
          noRecourseToPublicFunds = true
        ))
      )

      server.stubFor(
        post(urlEqualTo(s"/v1/status/public-funds/nino/$service"))
          .withHeader("X-Correlation-Id", equalTo(correlationId.toString))
          .withHeader("Authorization", equalTo("authKey"))
          .willReturn(ok(responseBody))
      )

      val ninoSearchRequest = NinoSearchRequest(nino.nino, "First", "Last", LocalDate.of(2001, 1, 31), searchRange)

      val result = connector.checkStatus(ninoSearchRequest, correlationId).futureValue

      result mustEqual expectedResponse
    }

    "must return a failed future when the server returns an error code" in {

      server.stubFor(
        post(urlEqualTo(s"/v1/status/public-funds/nino/$service"))
          .withHeader("X-Correlation-Id", equalTo(correlationId.toString))
          .withHeader("Authorization", equalTo("authKey"))
          .willReturn(serverError())
      )

      val ninoSearchRequest = NinoSearchRequest(nino.nino, "First", "Last", LocalDate.of(2001, 1, 31), searchRange)

      connector.checkStatus(ninoSearchRequest, correlationId).failed.futureValue
    }
  }
}
