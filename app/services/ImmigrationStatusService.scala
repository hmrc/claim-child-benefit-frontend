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

import connectors.ImmigrationStatusConnector
import logging.Logging
import models.NationalityGroup
import models.immigration.{NinoSearchRequest, StatusCheckRange}
import models.journey.JourneyModel
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDate}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImmigrationStatusService @Inject()(
                                          connector: ImmigrationStatusConnector,
                                          config: Configuration,
                                          clock: Clock
                                        )
                                        (implicit ec: ExecutionContext) extends Logging {

  private val searchRangeInMonths: Long =
    config.get[Long]("microservice.services.home-office-immigration-status-proxy.searchRangeInMonths")

  def settledStatusStartDate(nino: String, model: JourneyModel, correlationId: UUID)
                            (hc: HeaderCarrier): Future[Option[LocalDate]] =
    if (model.applicant.nationalityGroupToUse == NationalityGroup.Eea) {

      val today = LocalDate.now(clock)

      val searchRequest = NinoSearchRequest(
        nino = nino,
        givenName = model.applicant.name.firstName,
        familyName = model.applicant.name.lastName,
        dateOfBirth = model.applicant.dateOfBirth,
        statusCheckRange = StatusCheckRange(
          startDate = Some(today.minusMonths(searchRangeInMonths)),
          endDate = Some(today)
        )
      )

      connector
        .checkStatus(searchRequest, correlationId)(hc)
        .map(result => result.settledStatusStartDate)
        .recover {
          case e: Throwable =>
            logger.debug("Call to home-office-immigration-status-proxy failed: " + e.getMessage)
            logger.warn("Call to home-office-immigration-status-proxy failed.")
            None
        }
    } else {
      Future.successful(None)
    }
}
