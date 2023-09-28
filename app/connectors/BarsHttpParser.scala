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

import logging.Logging
import models.{ErrorResponse, InvalidJson, UnexpectedResponseStatus, VerifyBankDetailsResponseModel}
import play.api.http.Status.OK
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object BarsHttpParser extends Logging {

  type VerifyBankDetailsResponse = Either[ErrorResponse, VerifyBankDetailsResponseModel]

  implicit object BarsReads extends HttpReads[(VerifyBankDetailsResponse, HttpResponse)] {

    override def read(method: String, url: String, response: HttpResponse): (VerifyBankDetailsResponse, HttpResponse) = {
      (response.status match {
        case OK =>
          response.json.validate[VerifyBankDetailsResponseModel] match {
            case JsSuccess(model, _) =>
              Right(model)

            case _ => {
              logger.warn("Unable to parse the content of a response from BARS")
              Left(InvalidJson)
            }
          }
        case status => {
          logger.warn(s"Received an error status $status from BARS")
          Left(UnexpectedResponseStatus(status))
        }
      }) -> response
    }
  }
}
