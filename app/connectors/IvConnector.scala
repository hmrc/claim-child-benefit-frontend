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

import config.Service
import connectors.ConnectorFailureLogger._
import models.IvResult
import play.api.Configuration
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IvConnector @Inject()(config: Configuration, httpClient: HttpClientV2)
                           (implicit ec: ExecutionContext) {

  private val baseUrl = config.get[Service]("microservice.services.identity-verification")

  def getJourneyStatus(journeyId: String)(implicit hc: HeaderCarrier): Future[IvResult] =
    httpClient
      .get(url"$baseUrl/identity-verification/journey/$journeyId")
      .execute[IvResult]
      .logFailureReason(connectorName = "IvConnector on getJourneyStatus")
}
