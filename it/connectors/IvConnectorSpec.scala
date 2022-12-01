package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{get, notFound, ok, urlEqualTo}
import models.IvResult
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

class IvConnectorSpec
  extends AnyFreeSpec
    with WireMockHelper
    with ScalaFutures
    with Matchers
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar{

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    new GuiceApplicationBuilder()
      .configure("microservice.services.identity-verification.port" -> server.port)
      .build()

  private val journeyId = "journeyId"

  "getJourneyStatus" - {

    "must return an IvResult when the server responds with OK and a known status" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[IvConnector]

        val responseJson = Json.obj(
          "journeyId" -> journeyId,
          "progress" -> Json.obj(
            "result" -> "Success"
          )
        )

        server.stubFor(
          get(urlEqualTo(s"/identity-verification/journey/$journeyId"))
            .willReturn(ok(responseJson.toString))
        )

        val result = connector.getJourneyStatus(journeyId).futureValue

        result mustEqual IvResult.Success
      }
    }

    "must fail the future when the server responds with a status other than OK" in {

      val app = application

      running(app) {

        val connector = app.injector.instanceOf[IvConnector]

        server.stubFor(
          get(urlEqualTo(s"/identity-verification/journey/$journeyId"))
            .willReturn(notFound())
        )

        connector.getJourneyStatus(journeyId).failed.futureValue
      }
    }
  }
}
