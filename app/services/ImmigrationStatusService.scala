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

package services

import config.FeatureFlags
import connectors.ImmigrationStatusConnector
import models.NationalityGroup
import models.immigration.{NinoSearchRequest, StatusCheckRange}
import models.journey.JourneyModel
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImmigrationStatusService @Inject()(
                                          featureFlags: FeatureFlags,
                                          connector: ImmigrationStatusConnector
                                        )
                                        (implicit ec: ExecutionContext) {

  def hasSettledStatus(nino: String, model: JourneyModel, correlationId: UUID)(hc: HeaderCarrier): Future[Option[Boolean]] =
    if (featureFlags.checkImmigrationStatus) {
      if (model.applicant.preferentialNationalityGroup == NationalityGroup.Eea) {

        val searchRequest = NinoSearchRequest(
          nino = nino,
          givenName = model.applicant.name.firstName,
          familyName = model.applicant.name.lastName,
          dateOfBirth = model.applicant.dateOfBirth,
          statusCheckRange = StatusCheckRange()
        )

        connector
          .checkStatus(searchRequest, correlationId)(hc)
          .map(result => Some(result.hasSettledStatus))
      } else {
        Future.successful(None)
      }
    } else {
      Future.successful(None)
    }
}
