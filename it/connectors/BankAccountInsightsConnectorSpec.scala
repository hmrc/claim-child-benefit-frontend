package connectors

import audit.AuditService
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, post, serverError, urlEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import models.{BankAccountInsightsRequest, BankAccountInsightsResponseModel, InvalidJson, UnexpectedException, UnexpectedResponseStatus}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, running}
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
      .configure("microservice.services.bank-account-insights.port" -> server.port)
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
            .willReturn(ok(jsonResponse))
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.value mustEqual BankAccountInsightsResponseModel("ab8514f3-0f3c-4823-aba6-58f2222c33f1", 0, "foo")

        // TODO: Check audit event is emitted
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
            .willReturn(ok(jsonResponse))
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.left.value mustEqual InvalidJson

        // TODO: Check audit event is emitted
      }
    }

    "must return a Left(UnexpectedResponseStatus) when an error is returned" in {



      val app = application

      running(app) {

        val connector = app.injector.instanceOf[BankAccountInsightsConnector]
        val auditService = app.injector.instanceOf[AuditService]

        server.stubFor(
          post(urlEqualTo("/check/insights"))
            .willReturn(serverError)
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.left.value mustEqual UnexpectedResponseStatus(INTERNAL_SERVER_ERROR)

        // TODO: Check audit event is emitted
      }

    }


    "must return Left(UnexpectedException) when the server response with a fault" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[BankAccountInsightsConnector]

        server.stubFor(
          post(urlEqualTo("/check/insights"))
            .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK))
        )

        val request = BankAccountInsightsRequest("123456", "12345678")

        val result = connector.check(request).futureValue

        result.left.value mustEqual UnexpectedException
      }
    }
  }
}
