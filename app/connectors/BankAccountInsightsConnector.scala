/*
 * Copyright 2024 HM Revenue & Customs
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

import audit.{AuditService, CheckBankAccountInsightsAuditEvent}
import config.Service
import connectors.BankAccountInsightsHttpParser.{BankAccountInsightsReads, BankAccountInsightsResponse}
import connectors.ConnectorFailureLogger._
import logging.Logging
import models.{BankAccountInsightsRequest, UnexpectedException}
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

class BankAccountInsightsConnector @Inject()(config: Configuration, httpClient: HttpClientV2, auditService: AuditService)
                                            (implicit ec: ExecutionContext) extends Logging {


  private val baseUrl = config.get[Service]("microservice.services.bank-account-insights")
  private val verifyUrl = url"$baseUrl/check/insights"
  private val internalAuthToken: String = config.get[String]("internal-auth.token")

  def check(request: BankAccountInsightsRequest)
           (implicit hc: HeaderCarrier): Future[BankAccountInsightsResponse] = {

    val json = Json.toJson(request)

    httpClient
      .post(verifyUrl)
      .setHeader("Authorization" -> internalAuthToken)
      .withBody(json)
      .execute
      .logFailureReason(connectorName = "BankAccountInsightsConnector on check")
      .map {
        case (connectorResponse, httpResponse) =>
          auditService.auditCheckBankAccountInsights(
            CheckBankAccountInsightsAuditEvent(
              request = request,
              response = getResponseJson(httpResponse)
            )
          )
          connectorResponse
      }.recover {
        case e =>
          logger.debug(s"Error calling bank account insights: ${e.getMessage}")
          logger.error(s"Error calling bank account insights.")
          Left(UnexpectedException)
      }
  }

  private def getResponseJson(response: HttpResponse): JsValue =
    Try(response.json).getOrElse(Json.obj())
}
