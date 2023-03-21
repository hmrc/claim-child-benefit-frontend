package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import connectors.ClaimChildBenefitConnector._
import generators.ModelGenerators
import models.domain._
import models.{AdultName, CheckLimitResponse, DesignatoryDetails, Done, NPSAddress, SupplementaryMetadata}
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

class ClaimChildBenefitConnectorSpec
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
        "internal-auth.token" -> "authKey"
      )
      .build()

  private val happyDesignatoryDetailsJson ="""{
      |"realName": {"title": "Mrs", "firstName": "foo", "middleNames": "baz", "lastName": "bar"},
      |"residentialAddress": {"line1": "123", "postcode": "NE98 1ZZ"},
      |"dateOfBirth": "2000-02-01"
      |}""".stripMargin
  private val happyDesignatoryDetailsModel = DesignatoryDetails(
    realName = Some(AdultName(Some("Mrs"), "foo", Some("baz"), "bar")),
    knownAsName = None,
    residentialAddress = Some(NPSAddress("123", None, None, None, None, Some("NE98 1ZZ"), None)),
    correspondenceAddress = None,
    dateOfBirth = LocalDate.of(2000, 2, 1)
  )

  private lazy val connector: ClaimChildBenefitConnector = app.injector.instanceOf[ClaimChildBenefitConnector]

  ".designatoryDetails" - {

    "when valid json is returned" - {

      "must return a designatory details model" in {

        server.stubFor(
          get(urlEqualTo("/claim-child-benefit/designatory-details"))
            .willReturn(ok(happyDesignatoryDetailsJson))
        )

        val result = connector.designatoryDetails().futureValue

        result mustEqual happyDesignatoryDetailsModel
      }
    }

    "when an error is returned" - {

      "must return a failed future" in {

        server.stubFor(
          get(urlEqualTo("/claim-child-benefit/designatory-details"))
            .willReturn(aResponse().withStatus(500))
        )

        val result = connector.designatoryDetails()

        result.failed.futureValue mustBe an[Exception]
      }
    }
  }

  ".submitClaim" - {

    val correlationId = UUID.randomUUID()

    val nino = arbitrary[Nino].sample.value.nino
    val claim = Claim(
      dateOfClaim = LocalDate.now,
      claimant = UkCtaClaimantAlwaysResident(nino = nino, hmfAbroad = false, hicbcOptOut = true),
      partner = None,
      payment = None,
      children = List(Child(
        name = ChildName("first", None, "last"),
        gender = BiologicalSex.Female,
        dateOfBirth = LocalDate.now,
        birthRegistrationNumber = None,
        crn = None,
        countryOfRegistration = CountryOfRegistration.EnglandWales,
        dateOfBirthVerified = None,
        livingWithClaimant = true,
        claimantIsParent = true,
        adoptionStatus = false
      )),
      otherEligibilityFailure = false
    )

    "must return Done when the server returns CREATED" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .withHeader("CorrelationId", equalTo(correlationId.toString))
          .willReturn(created())
      )

      val result = connector.submitClaim(claim, correlationId).futureValue

      result mustEqual Done
    }

    "must return a failed future (Bad Request Exception) when the server returns 400" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(aResponse().withStatus(BAD_REQUEST))
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe a[BadRequestException]
    }

    "must return a failed future (Invalid Claim State Exception) when the server returns 422 with a code of INVALID_CLAIM_STATE" in {

      val responseJson = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> "INVALID_CLAIM_STATE",
            "reason" -> "The remote endpoint has indicated that this account can not currently accept new claim requests due to a previous claim is still being processed on the account."
          )
        )
      )

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody(responseJson.toString)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe an[InvalidClaimStateException]
    }

    "must return a failed future (Invalid Account State Exception) when the server returns 422 with a code of INVALID_ACCOUNT_STATE" in {

      val responseJson = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> "INVALID_ACCOUNT_STATE",
            "reason" -> "The remote endpoint has indicated that - Invalid customer data held."
          )
        )
      )

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody(responseJson.toString)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe an[InvalidAccountStateException]
    }

    "must return a failed future (Already In Payment Exception) when the server returns 422 with a code of PAYMENT_PRESENT_AFTER_FIRST_PAYMENT_INSTRUCTION" in {

      val responseJson = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> "PAYMENT_PRESENT_AFTER_FIRST_PAYMENT_INSTRUCTION",
            "reason" -> "The remote endpoint has indicated that payment object should not be present if the first payment instruction has been sent."
          )
        )
      )

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody(responseJson.toString)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe an[AlreadyInPaymentException]
    }

    "must return a failed future (Unprocessable Entity Exception) when the server returns 422 with no recognised code" in {

      val responseJson = Json.obj(
        "failures" -> Json.arr(
          Json.obj(
            "code" -> "Foo",
            "reason" -> "Bar"
          )
        )
      )

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody(responseJson.toString)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe an[UnprocessableEntityException]
    }

    "must return a failed future (Unrecognised Response Exception) when the server returns 422 but we cannon parse the response body" in {

      val responseJson = Json.obj(
        "foo" -> "bar"
      )

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(UNPROCESSABLE_ENTITY)
              .withBody(responseJson.toString)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe an[UnprocessableEntityException]
    }

    "must return a failed future (Server Error) when the server returns 500" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe a[ServerErrorException]
    }

    "must return a failed future (Service Unavailable) when the server returns 503" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe a[ServiceUnavailableException]
    }

    "must return a failed future (Unrecognised Response) when the server returns an unexpected code" in {

      val status = Gen.oneOf(UNAUTHORIZED, FORBIDDEN, REQUEST_TIMEOUT, CONFLICT, BAD_GATEWAY).sample.value

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/submit"))
          .willReturn(
            aResponse()
              .withStatus(status)
          )
      )

      val result = connector.submitClaim(claim, correlationId).failed.futureValue

      result mustBe an[UnrecognisedResponseException]
    }
  }

  ".submitSupplementaryData" - {

    val submissionDate = LocalDateTime.of(2022, 3, 2, 12, 30, 45, 123456)
    val expectedSubmissionDate = DateTimeFormatter.ISO_DATE_TIME.format(submissionDate.truncatedTo(ChronoUnit.SECONDS))
    val nino = arbitrary[Nino].sample.value
    val correlationId = "correlationId"
    val hc = HeaderCarrier()
    val pdf = "asdf".getBytes("UTF-8")
    val metadata = SupplementaryMetadata(
      nino = nino.value,
      correlationId = correlationId,
      submissionDate = submissionDate.toInstant(ZoneOffset.UTC)
    )

    "must return Done when the server returns ACCEPTED" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/supplementary-data"))
          .withHeader(AUTHORIZATION, equalTo("authKey"))
          .withHeader(USER_AGENT, equalTo("claim-child-benefit-frontend"))
          .withMultipartRequestBody(aMultipart().withName("metadata.nino").withBody(equalTo(nino.value)))
          .withMultipartRequestBody(aMultipart().withName("metadata.submissionDate").withBody(equalTo(expectedSubmissionDate)))
          .withMultipartRequestBody(aMultipart().withName("metadata.correlationId").withBody(equalTo(correlationId)))
          .withMultipartRequestBody(aMultipart().withName("file").withBody(binaryEqualTo(pdf)))
          .willReturn(
            aResponse()
              .withStatus(ACCEPTED)
              .withBody(Json.stringify(Json.obj("id" -> "foobar")))
          )
      )

      connector.submitSupplementaryData(pdf, metadata)(hc).futureValue
    }

    "must fail when the server returns another status" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/supplementary-data"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      connector.submitSupplementaryData(pdf, metadata)(hc).failed.futureValue
    }
  }

  ".checkThrottleLimit" - {

    "must return a result when the server returns one" in {

      val response = CheckLimitResponse(limitReached = true)

      server.stubFor(
        get(urlEqualTo("/claim-child-benefit/throttle/check"))
          .willReturn(aResponse().withStatus(OK).withBody(Json.toJson(response).toString))
      )

      connector.checkThrottleLimit().futureValue mustEqual response
    }

    "must fail when the server returns an error" in {

      server.stubFor(
        get(urlEqualTo("/claim-child-benefit/throttle/check"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      connector.checkThrottleLimit().failed.futureValue
    }
  }

  ".incrementThrottleCount" - {

    "must return Done when the server responds with OK" in {

      server.stubFor(
        post(urlEqualTo("/claim-child-benefit/throttle/increment"))
          .willReturn(aResponse().withStatus(OK))
      )

      connector.incrementThrottleCount().futureValue mustEqual Done
    }

    "must fail when the server returns an error" in {

      server.stubFor(
        get(urlEqualTo("/claim-child-benefit/throttle/incremnt"))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      connector.incrementThrottleCount().failed.futureValue
    }
  }
}
