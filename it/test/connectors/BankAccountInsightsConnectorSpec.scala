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

import audit.{AuditService, CheckBankAccountInsightsAuditEvent}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import models.{BankAccountInsightsRequest, BankAccountInsightsResponseModel, InvalidJson, UnexpectedException, UnexpectedResponseStatus}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers.{AUTHORIZATION, INTERNAL_SERVER_ERROR, running}
import uk.gov.hmrc.http.HeaderCarrier

class BankAccountInsightsConnectorSpec
  extends AnyFreeSpec
    with WireMockHelper
    with ScalaFutures
    with Matchers
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.bank-account-insights.port" -> server.port,
        "internal-auth.token" -> "token"
      )
      .bindings(bind[AuditService].toInstance(mock[AuditService]))
      .build()

  ".check" - {

    "must return Right when the server response with OK and valid json" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[BankAccountInsightsConnector]
        val auditService = app.injector.instanceOf[AuditService]

        val jsonResponse =
          """{
            |    "bankAccountInsightsCorrelationId": "ab8514f3-0f3c-4823-aba6-58f2222c33f1",
            |    "riskScore": 0,
            |    "reason": "foo"
            |}""".stripMargin

        server.stubFor(
          post(urlEqualTo("/check/insights"))
            .withHeader(AUTHORIZATION, equalTo("token"))
            .willReturn(ok(jsonResponse))
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.value mustEqual BankAccountInsightsResponseModel("ab8514f3-0f3c-4823-aba6-58f2222c33f1", 0, "foo")

        verify(auditService, times(1)).auditCheckBankAccountInsights(
          eqTo(CheckBankAccountInsightsAuditEvent(
            request = request,
            response = Json.parse(jsonResponse)
          ))
        )(any())
      }
    }

    "must return Left(InvalidJson) when the server response with OK but we cannot read the response json" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[BankAccountInsightsConnector]
        val auditService = app.injector.instanceOf[AuditService]

        val jsonResponse =
          """{"foo": "bar"}""".stripMargin

        server.stubFor(
          post(urlEqualTo("/check/insights"))
            .withHeader(AUTHORIZATION, equalTo("token"))
            .willReturn(ok(jsonResponse))
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.left.value mustEqual InvalidJson

        verify(auditService, times(1)).auditCheckBankAccountInsights(
          eqTo(CheckBankAccountInsightsAuditEvent(
            request = request,
            response = Json.parse(jsonResponse)
          ))
        )(any())
      }
    }

    "must return a Left(UnexpectedResponseStatus) when an error is returned" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[BankAccountInsightsConnector]
        val auditService = app.injector.instanceOf[AuditService]

        server.stubFor(
          post(urlEqualTo("/check/insights"))
            .withHeader(AUTHORIZATION, equalTo("token"))
            .willReturn(serverError)
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.left.value mustEqual UnexpectedResponseStatus(INTERNAL_SERVER_ERROR)

        verify(auditService, times(1)).auditCheckBankAccountInsights(
          eqTo(CheckBankAccountInsightsAuditEvent(
            request = request,
            response = Json.obj()
          ))
        )(any())
      }

    }

    "must return Left(UnexpectedException) when the server response with a fault" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[BankAccountInsightsConnector]

        server.stubFor(
          post(urlEqualTo("/check/insights"))
            .withHeader(AUTHORIZATION, equalTo("token"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.left.value mustEqual UnexpectedException
      }
    }
  }
}
