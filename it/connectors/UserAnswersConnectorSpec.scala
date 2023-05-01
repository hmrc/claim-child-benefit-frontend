package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.ClaimChildBenefitConnector._
import generators.ModelGenerators
import models.domain._
import models.{AdultName, CheckLimitResponse, DesignatoryDetails, Done, NPSAddress, RelationshipDetails, SupplementaryMetadata, UserAnswers}
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

class UserAnswersConnectorSpec
  extends AnyFreeSpec
    with WireMockHelper
    with ScalaFutures
    with Matchers
    with IntegrationPatience
    with EitherValues
    with OptionValues
    with MockitoSugar
    with ModelGenerators {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.claim-child-benefit.port" -> server.port,
      )
      .build()

  private lazy val connector = app.injector.instanceOf[UserAnswersConnector]

  private val answers = UserAnswers("id")


  ".get" - {

    "must return user answers when the server returns them" in {

      server.stubFor(
        get(urlEqualTo("/claim-child-benefit/user-data"))
          .willReturn(ok(Json.toJson(answers).toString))
      )

      val result = connector.get().futureValue

      result.value mustEqual answers
    }

    "must return None when the server returns NOT_FOUND" in {

      server.stubFor(
        get(urlEqualTo("/claim-child-benefit/user-data"))
          .willReturn(notFound())
      )

      val result = connector.get().futureValue

      result must not be defined
    }

    "must return a failed future when the server returns an error" in {

      server.stubFor(
        get(urlEqualTo("/claim-child-benefit/user-data"))
          .willReturn(serverError())
      )

      connector.get().failed.futureValue
    }
  }

  ".set" - {

    "must post user answers to the server" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/user-data"))
          .withRequestBody(equalTo(Json.toJson(answers).toString))
          .willReturn(noContent())
      )

      connector.set(answers).futureValue
    }

    "must return a failed future when the server returns an error" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/user-data"))
          .withRequestBody(equalTo(Json.toJson(answers).toString))
          .willReturn(serverError())
      )

      connector.set(answers).failed.futureValue
    }
  }

  ".keepAlive" - {

    "must post to the server" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/user-data/keep-alive"))
          .willReturn(noContent())
      )

      connector.keepAlive().futureValue
    }

    "must return a failed future when the server returns an error" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/user-data/keep-alive"))
          .willReturn(serverError())
      )

      connector.keepAlive().failed.futureValue
    }
  }

  ".clear" - {

    "must send a delete request to the server" in {

      server.stubFor(
        delete(urlEqualTo("/claim-child-benefit/user-data"))
          .willReturn(noContent())
      )

      connector.clear().futureValue
    }

    "must return a failed future when the server returns an error" in {

      server.stubFor(
        delete(urlEqualTo("/claim-child-benefit/user-data"))
          .willReturn(serverError())
      )

      connector.clear().failed.futureValue
    }
  }
}
