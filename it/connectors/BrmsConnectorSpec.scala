package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, ok, post, urlEqualTo}
import models.ChildBirthRegistrationCountry.England
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResponseModel, ChildName}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class BrmsConnectorSpec
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
      .configure("microservice.services.birth-registration-matching.port" -> server.port)
      .build()

  private val happyResponseJson = """{"matched": false}"""

  ".matchChild" - {

    "when valid json is returned" - {

      "must return a BRM response model" in {

        val app = application

        running(app) {

          val connector = app.injector.instanceOf[BrmsConnector]

          server.stubFor(
            post(urlEqualTo("/birth-registration-matching/match"))
              .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
              .withHeader("Audit-Source", equalTo("claim-child-benefit-frontend"))
              .withHeader("Content-Type", equalTo("application/json"))
              .willReturn(ok(happyResponseJson))
          )

          val request = BirthRegistrationMatchingRequest(None, ChildName("first", None, "last"), LocalDate.now, England).value

          val result = connector.matchChild(request).futureValue

          result mustEqual BirthRegistrationMatchingResponseModel(false)
        }
      }
    }

    "when an error is returned" - {

      "must return a failed future" in {

        val app = application

        running(app) {

          val connector = app.injector.instanceOf[BrmsConnector]

          server.stubFor(
            post(urlEqualTo("/birth-registration-matching/match"))
              .willReturn(aResponse().withStatus(500))
          )

          val request = BirthRegistrationMatchingRequest(None, ChildName("first", None, "last"), LocalDate.now, England).value

          val result = connector.matchChild(request)

          result.failed.futureValue mustBe an[Exception]
        }
      }
    }
  }
}
