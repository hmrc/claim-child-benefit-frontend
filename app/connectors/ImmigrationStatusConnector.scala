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

import config.FrontendAppConfig
import connectors.ConnectorFailureLogger._
import models.immigration.{NinoSearchRequest, StatusCheckResponse, StatusCheckResult}
import play.api.http.HeaderNames
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImmigrationStatusConnector @Inject()(
                                            config: FrontendAppConfig,
                                            httpClient: HttpClientV2
                                          )(implicit ec: ExecutionContext) {

  private val baseUrl = config.homeOfficeImmigrationStatusUrl
  private val service = config.appName
  private val ninoCheckUrl = url"$baseUrl/v1/status/public-funds/nino/$service"

  def checkStatus(ninoSearchRequest: NinoSearchRequest, correlationId: UUID)(implicit hc: HeaderCarrier): Future[StatusCheckResult] =
    httpClient
      .post(ninoCheckUrl)
      .withBody(Json.toJson(ninoSearchRequest))
      .setHeader("X-Correlation-Id" -> correlationId.toString)
      .setHeader(HeaderNames.AUTHORIZATION -> config.internalAuthToken)
      .execute[StatusCheckResponse]
      .logFailureReason(connectorName = "ImmigrationStatusConnector on checkStatus")
      .map(_.result)
}
