/*
 * Copyright 2023 HM Revenue & Customs
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

import connectors.ClaimChildBenefitConnector._
import logging.Logging
import models.Done
import play.api.http.Status._
import play.api.libs.json.{JsSuccess, Json, OFormat}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object SubmitClaimHttpParser extends Logging {

  implicit object SubmitClaimReads extends HttpReads[Either[SubmitClaimException, Done]] {

    // scalastyle:off
    override def read(method: String, url: String, response: HttpResponse): Either[SubmitClaimException, Done] =
      response.status match {
        case CREATED =>
          Right(Done)

        case BAD_REQUEST =>
          Left(new BadRequestException)

        case UNPROCESSABLE_ENTITY =>
          response.json.validate[SubmitClaimFailureResponse] match {
            case JsSuccess(response, _) =>
              if (response.failures.exists(_.code == SubmitClaimFailure.InvalidClaimStateCode)) {
                Left(new InvalidClaimStateException)
              } else if (response.failures.exists(_.code == SubmitClaimFailure.InvalidAccountStateCode)) {
                Left(new InvalidAccountStateException)
              } else if (response.failures.exists(_.code == SubmitClaimFailure.PaymentPresentAfterFirstPaymentCode)) {
                Left(new AlreadyInPaymentException)
              } else {
                Left(new UnprocessableEntityException)
              }

            case _ =>
              logger.warn("Unable to parse the content of an UNPROCESSABLE_ENTITY response from CBS")
              Left(new UnprocessableEntityException)
          }

        case INTERNAL_SERVER_ERROR =>
          Left(new ServerErrorException)

        case SERVICE_UNAVAILABLE =>
          Left(new ServiceUnavailableException)

        case code =>
          Left(new UnrecognisedResponseException(code))
      }
  }
  // scalastyle: on
}

final case class SubmitClaimFailure(code: String, reason: String)

object SubmitClaimFailure {

  implicit lazy val format: OFormat[SubmitClaimFailure] = Json.format

  val InvalidClaimStateCode = "INVALID_CLAIM_STATE"
  val InvalidAccountStateCode = "INVALID_ACCOUNT_STATE"
  val PaymentPresentAfterFirstPaymentCode = "PAYMENT_PRESENT_AFTER_FIRST_PAYMENT_INSTRUCTION"
}

final case class SubmitClaimFailureResponse(failures: List[SubmitClaimFailure])

object SubmitClaimFailureResponse {

  implicit lazy val format: OFormat[SubmitClaimFailureResponse] = Json.format
}
