package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import models.{AdultName, DesignatoryDetails, NPSAddress}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

class ClaimChildBenefitConnectorSpec
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
      .configure("microservice.services.claim-child-benefit.port" -> server.port)
      .build()

  private val happyResponseJson ="""{
      |"realName": {"title": "Mrs", "firstName": "foo", "middleNames": "baz", "lastName": "bar"},
      |"residentialAddress": {"line1": "123", "postcode": "NE98 1ZZ"}}""".stripMargin
  private val happyResponseModel = DesignatoryDetails(
    realName = Some(AdultName(Some("Mrs"), "foo", Some("baz"), "bar")),
    knownAsName = None,
    residentialAddress = Some(NPSAddress("123", None, None, None, None, Some("NE98 1ZZ"), None)),
    correspondenceAddress = None
  )

  ".designatoryDetails" - {

    "when valid json is returned" - {

      "must return a designatory details model" in {

        val app = application

        running(app) {

          val connector = app.injector.instanceOf[ClaimChildBenefitConnector]

          server.stubFor(
            get(urlEqualTo("/designatory-details"))
              .willReturn(ok(happyResponseJson))
          )

          val result = connector.designatoryDetails().futureValue

          result mustEqual happyResponseModel
        }
      }
    }

    "when an error is returned" - {

      "must return a failed future" in {

        val app = application

        running(app) {

          val connector = app.injector.instanceOf[ClaimChildBenefitConnector]

          server.stubFor(
            get(urlEqualTo("/designatory-details"))
              .willReturn(aResponse().withStatus(500))
          )

          val result = connector.designatoryDetails()

          result.failed.futureValue mustBe an[Exception]
        }
      }
    }
  }
}
