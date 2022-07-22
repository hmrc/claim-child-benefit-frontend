package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{Account, InvalidJson, ReputationResponseEnum, UnexpectedResponseStatus, ValidateBankDetailsRequest, ValidateBankDetailsResponseModel}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
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

      "must return a Right" in {

        val app = application

        running(app) {

          val connector = app.injector.instanceOf[BarsConnector]

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
        }
      }
    }

    "when invalid json is returned" - {

      "must return a Left(InvalidJson)" in {

        val app = application

        running(app) {

          val connector = app.injector.instanceOf[BarsConnector]

          val invalidJson = """{"foo": "bar"}"""

          server.stubFor(
            post(urlEqualTo("/validate/bank-details"))
              .willReturn(ok(invalidJson))
          )

          val request = ValidateBankDetailsRequest(Account("123456", "12345678"))

          val result = connector.validate(request).futureValue

          result.left.value mustEqual InvalidJson
        }
      }
    }

    "when an error is returned" - {

      "must return a Left(UnexpectedResponseStatus)" in {

        val app = application

        running(app) {

          val connector = app.injector.instanceOf[BarsConnector]

          server.stubFor(
            post(urlEqualTo("/validate/bank-details"))
              .willReturn(serverError)
          )

          val request = ValidateBankDetailsRequest(Account("123456", "12345678"))

          val result = connector.validate(request).futureValue

          result.left.value mustEqual UnexpectedResponseStatus(500)
        }
      }
    }
  }
}
