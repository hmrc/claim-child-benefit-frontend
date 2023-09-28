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

import audit.{AuditService, VerifyBankDetailsAuditEvent}
import config.Service
import connectors.BarsHttpParser.{BarsReads, VerifyBankDetailsResponse}
import connectors.ConnectorFailureLogger._
import logging.Logging
import models.{UnexpectedException, VerifyBankDetailsRequest}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class BarsConnector @Inject()(config: Configuration, httpClient: HttpClientV2, auditService: AuditService)
                             (implicit ec: ExecutionContext) extends Logging {

  private val baseUrl   = config.get[Service]("microservice.services.bank-account-reputation")
  private val verifyUrl = url"$baseUrl/verify/personal"

  def verify(barsRequest: VerifyBankDetailsRequest)
            (implicit hc: HeaderCarrier): Future[VerifyBankDetailsResponse] = {

    val json = Json.toJson(barsRequest)

    httpClient.post(verifyUrl).withBody(json).execute.map {
      case (connectorResponse, httpResponse) =>
        auditService.auditVerifyBankDetails(
          VerifyBankDetailsAuditEvent(
            request = barsRequest,
            response = getResponseJson(httpResponse)
          )
        )

        connectorResponse
    }
      .logFailureReason(connectorName = "BarsConnector on verify")
      .recover {
        case e =>
          logger.error(s"Error calling verify personal: ${e.getMessage}")
          Left(UnexpectedException)
      }
  }

  private def getResponseJson(response: HttpResponse): JsValue =
    Try(response.json).getOrElse(Json.obj())
}
