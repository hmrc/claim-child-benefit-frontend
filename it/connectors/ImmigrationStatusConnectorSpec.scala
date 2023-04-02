package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.ClaimChildBenefitConnector._
import generators.ModelGenerators
import models.domain._
import models.immigration.{ImmigrationStatus, NinoSearchRequest, StatusCheckRange, StatusCheckResult}
import models.{AdultName, CheckLimitResponse, DesignatoryDetails, Done, NPSAddress, RelationshipDetails, SupplementaryMetadata}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime, ZoneOffset}
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
        "microservice.services.home-office-immigration-status-proxy.port" -> server.port
      )
      .build()

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  private val correlationId = UUID.randomUUID()
  private val nino = arbitrary[Nino].sample.value

  private lazy val connector = app.injector.instanceOf[ImmigrationStatusConnector]

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
        post(urlEqualTo("/v1/status/public-funds/nino"))
          .withHeader("X-Correlation-Id", equalTo(correlationId.toString))
          .willReturn(ok(responseBody))
      )

      val ninoSearchRequest = NinoSearchRequest(nino.nino, "First", "Last", LocalDate.of(2001, 1, 31), StatusCheckRange())

      val result = connector.checkStatus(ninoSearchRequest, correlationId).futureValue

      result mustEqual expectedResponse
    }

    "must return a failed future when the server returns an error code" in {

      server.stubFor(
        post(urlEqualTo("/v1/status/public-funds/nino"))
          .withHeader("X-Correlation-Id", equalTo(correlationId.toString))
          .willReturn(serverError())
      )

      val ninoSearchRequest = NinoSearchRequest(nino.nino, "First", "Last", LocalDate.of(2001, 1, 31), StatusCheckRange())

      connector.checkStatus(ninoSearchRequest, correlationId).failed.futureValue
    }
  }
}
