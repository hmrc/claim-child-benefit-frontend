package connectors

import audit.{AuditService, ValidateBankDetailsAuditEvent}
import com.github.tomakehurst.wiremock.client.WireMock.{ok, post, serverError, urlEqualTo}
import models.{Account, InvalidJson, ReputationResponseEnum, UnexpectedResponseStatus, ValidateBankDetailsRequest, ValidateBankDetailsResponseModel}
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
       |  "iban": "GB59 HBUK 1234 5678"
       |}
       |""".stripMargin

  ".validateBankDetails" - {

    "when valid json is returned" - {

      "must return a Right and audit the response" in {

        val app = application

        running(app) {

          val connector    = app.injector.instanceOf[BarsConnector]
          val auditService = app.injector.instanceOf[AuditService]

          server.stubFor(
            post(urlEqualTo("/validate/bank-details"))
              .willReturn(ok(happyResponseJson))
          )

          val request = ValidateBankDetailsRequest(Account("123456", "12345678"))

          val result = connector.validate(request).futureValue
          
          result.value mustEqual ValidateBankDetailsResponseModel(
            accountNumberIsWellFormatted = ReputationResponseEnum.Yes,
            nonStandardAccountDetailsRequiredForBacs = ReputationResponseEnum.No,
            sortCodeIsPresentOnEISCD = ReputationResponseEnum.Yes
          )

          verify(auditService, times(1)).auditValidateBankDetails(
            eqTo(ValidateBankDetailsAuditEvent(
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
            post(urlEqualTo("/validate/bank-details"))
              .willReturn(ok(invalidJson))
          )

          val request = ValidateBankDetailsRequest(Account("123456", "12345678"))

          val result = connector.validate(request).futureValue

          result.left.value mustEqual InvalidJson

          verify(auditService, times(1)).auditValidateBankDetails(
            eqTo(ValidateBankDetailsAuditEvent(
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
            post(urlEqualTo("/validate/bank-details"))
              .willReturn(serverError)
          )

          val request = ValidateBankDetailsRequest(Account("123456", "12345678"))

          val result = connector.validate(request).futureValue

          result.left.value mustEqual UnexpectedResponseStatus(500)

          verify(auditService, times(1)).auditValidateBankDetails(
            eqTo(ValidateBankDetailsAuditEvent(
              request  = request,
              response = Json.obj()
            ))
          )(any())
        }
      }
    }
  }
}
