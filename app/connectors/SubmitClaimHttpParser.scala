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

        case BAD_REQUEST => {
          logger.warn(s"Received BadRequestException from CBS")
          Left(new BadRequestException)
        }
        case UNPROCESSABLE_ENTITY =>
          response.json.validate[SubmitClaimFailureResponse] match {
            case JsSuccess(response, _) =>
              if (response.failures.exists(_.code == SubmitClaimFailure.InvalidClaimStateCode)) {
                logger.warn(s"Received InvalidClaimStateCode from CBS")
                Left(new InvalidClaimStateException)
              } else if (response.failures.exists(_.code == SubmitClaimFailure.InvalidAccountStateCode)) {
                logger.warn(s"Received InvalidAccountStateException from CBS")
                Left(new InvalidAccountStateException)
              } else if (response.failures.exists(_.code == SubmitClaimFailure.PaymentPresentAfterFirstPaymentCode)) {
                logger.warn(s"Received InvalidAccountStateException from CBS")
                Left(new AlreadyInPaymentException)
              } else {
                logger.warn(s"Received UnprocessableEntityException from CBS")
                Left(new UnprocessableEntityException)
              }

            case _ =>
              logger.warn("Unable to parse the content of an UNPROCESSABLE_ENTITY response from CBS")
              Left(new UnprocessableEntityException)
          }

        case INTERNAL_SERVER_ERROR => {
          logger.warn(s"Received INTERNAL_SERVER_ERROR from CBS")
          Left(new ServerErrorException)
        }

        case SERVICE_UNAVAILABLE => {
          logger.warn(s"Received SERVICE_UNAVAILABLE from CBS")
          Left(new ServiceUnavailableException)
        }
        case code => {
          logger.warn(s"Received error status code $code from CBS")
          Left(new UnrecognisedResponseException(code))
        }
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
