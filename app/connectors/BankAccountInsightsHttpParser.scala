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
import models.{BankAccountInsightsResponseModel, ErrorResponse, InvalidJson, UnexpectedResponseStatus}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import play.api.http.Status.OK
import play.api.libs.json.JsSuccess

object BankAccountInsightsHttpParser extends Logging {

  type BankAccountInsightsResponse = Either[ErrorResponse, BankAccountInsightsResponseModel]

  implicit object BankAccountInsightsReads extends HttpReads[(BankAccountInsightsResponse, HttpResponse)] {

    override def read(method: String, url: String, response: HttpResponse): (BankAccountInsightsResponse, HttpResponse) = {
      (response.status match {
        case OK =>
          response.json.validate[BankAccountInsightsResponseModel] match {
            case JsSuccess(model, _) =>
              Right(model)

            case _ => {
              logger.warn(s"Unable to parse the content of a response from Bank Account Insights")
              Left(InvalidJson)
            }
          }

        case status => {
          logger.warn(s"Received error status code $status from Bank Account Insights")
          Left(UnexpectedResponseStatus(status))
        }
      }) -> response
    }
  }
}
