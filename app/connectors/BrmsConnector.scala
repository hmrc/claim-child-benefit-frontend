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

import config.Service
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResponseModel}
import play.api.Configuration
import play.api.http.HeaderNames
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import ConnectorFailureLogger._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BrmsConnector @Inject()(config: Configuration, httpClient: HttpClientV2)
                             (implicit ec: ExecutionContext) {

  private val auditSource = "claim-child-benefit-frontend"
  private val baseUrl     = config.get[Service]("microservice.services.birth-registration-matching")
  private val matchUrl    = url"$baseUrl/birth-registration-matching/match"

  def matchChild(request: BirthRegistrationMatchingRequest)
                (implicit hc: HeaderCarrier): Future[BirthRegistrationMatchingResponseModel] = {

    val json = Json.toJson(request)

    httpClient
      .post(matchUrl)
      .withBody(json)
      .setHeader(
        "Audit-Source"     -> auditSource,
        HeaderNames.ACCEPT -> "application/vnd.hmrc.1.0+json"
      )
      .execute[BirthRegistrationMatchingResponseModel]
      .logFailureReason(connectorName = "BrmsConnector on matchChild")
  }
}
