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

import audit.{AuditService, VerifyBankDetailsAuditEvent}
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import models.{Account, InvalidJson, ReputationResponseEnum, Subject, UnexpectedException, UnexpectedResponseStatus, VerifyBankDetailsRequest, VerifyBankDetailsResponseModel}
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
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

class BarsConnectorSpec
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
      .configure("microservice.services.bank-account-reputation.port" -> server.port)
      .bindings(bind[AuditService].toInstance(mock[AuditService]))
      .build()

  private val happyResponseJson =
    """|{
       |  "accountNumberIsWellFormatted": "yes",
       |  "nonStandardAccountDetailsRequiredForBacs": "no",
       |  "sortCodeIsPresentOnEISCD":"yes",
       |  "sortCodeBankName": "Lloyds",
       |  "sortCodeSupportsDirectDebit": "yes",
       |  "sortCodeSupportsDirectCredit": "yes",
       |  "iban": "GB59 HBUK 1234 5678",
       |  "accountExists": "yes",
       |  "nameMatches": "partial"
       |}
       |""".stripMargin

  ".verifyBankDetails" - {

    "when valid json is returned" - {

      "must return a Right and audit the response" in {

        val app = application

        running(app) {

          val connector    = app.injector.instanceOf[BarsConnector]
          val auditService = app.injector.instanceOf[AuditService]

          server.stubFor(
            post(urlEqualTo("/verify/personal"))
              .willReturn(ok(happyResponseJson))
          )

          val request = VerifyBankDetailsRequest(Account("123456", "12345678"), Subject("first", "last"))

          val result = connector.verify(request).futureValue
          
          result.value `mustEqual` VerifyBankDetailsResponseModel(
            accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
            nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
            sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes,
            sortCodeSupportsDirectCredit = ReputationResponseEnum.Yes,
            accountExists = ReputationResponseEnum.Yes,
            nameMatches = ReputationResponseEnum.Partial
          )

          verify(auditService, times(1)).auditVerifyBankDetails(
            eqTo(VerifyBankDetailsAuditEvent(
              request  = request,
              response = Json.parse(happyResponseJson)
            ))
          )(any())
        }
      }
    }

    "when invalid json is returned" - {

      "must return a Left(InvalidJson)" in {

        val app = application

        running(app) {

          val connector    = app.injector.instanceOf[BarsConnector]
          val auditService = app.injector.instanceOf[AuditService]

          val invalidJson = """{"foo": "bar"}"""

          server.stubFor(
            post(urlEqualTo("/verify/personal"))
              .willReturn(ok(invalidJson))
          )

          val request = VerifyBankDetailsRequest(Account("123456", "12345678"), Subject("first", "last"))

          val result = connector.verify(request).futureValue

          result.left.value `mustEqual` InvalidJson

          verify(auditService, times(1)).auditVerifyBankDetails(
            eqTo(VerifyBankDetailsAuditEvent(
              request  = request,
              response = Json.parse(invalidJson)
            ))
          )(any())
        }
      }
    }

    "when an error is returned" - {

      "must return a Left(UnexpectedResponseStatus)" in {

        val app = application

        running(app) {

          val connector    = app.injector.instanceOf[BarsConnector]
          val auditService = app.injector.instanceOf[AuditService]

          server.stubFor(
            post(urlEqualTo("/verify/personal"))
              .willReturn(serverError)
          )

          val request = VerifyBankDetailsRequest(Account("123456", "12345678"), Subject("first", "last"))

          val result = connector.verify(request).futureValue

          result.left.value `mustEqual` UnexpectedResponseStatus(500)

          verify(auditService, times(1)).auditVerifyBankDetails(
            eqTo(VerifyBankDetailsAuditEvent(
              request  = request,
              response = Json.obj()
            ))
          )(any())
        }
      }
    }

    "when the server responds with a fault" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[BarsConnector]

        server.stubFor(
          post(urlEqualTo("/verify/personal"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )

        val request = VerifyBankDetailsRequest(Account("123456", "12345678"), Subject("first", "last"))

        val result = connector.verify(request).futureValue

        result.left.value `mustEqual` UnexpectedException
      }
    }
  }
}
