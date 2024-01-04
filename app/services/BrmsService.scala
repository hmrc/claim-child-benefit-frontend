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

package services

import config.FeatureFlags
import connectors.BrmsConnector
import logging.Logging
import metrics.{BrmsMonitor, MetricsService}
import models.BirthRegistrationMatchingResult._
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResult}
import repositories.BrmsCacheRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BrmsService @Inject()(
                             brmsConnector: BrmsConnector,
                             brmsCacheRepository: BrmsCacheRepository,
                             featureFlags: FeatureFlags,
                             metricsService: MetricsService
                           ) extends Logging {

  def matchChild(maybeRequest: Option[BirthRegistrationMatchingRequest])
                (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[BirthRegistrationMatchingResult] = {
    if (featureFlags.matchBirthRegistrationDetails) {
      maybeRequest.map { request =>
        brmsCacheRepository.getResult(request).flatMap {
          _.map(Future.successful)
            .getOrElse {
              brmsConnector.matchChild(request).flatMap { response =>
                val result = if (response.matched) Matched else NotMatched
                metricsService.count(BrmsMonitor.getCounter(result))

                brmsCacheRepository.set(request, result)
                  .map(_ => result)
                  .recover {
                    case e: Exception =>
                      logger.debug("Error caching BRMS response", e.getMessage)
                      logger.warn("Error caching BRMS response")
                      result
                  }
              }.recover {
                case e: Exception =>
                  logger.debug("Error calling BRMS", e.getMessage)
                  logger.warn("Error calling BRMS")
                  metricsService.count(BrmsMonitor.getCounter(MatchingAttemptFailed))
                  MatchingAttemptFailed
              }
            }
        }
      }.getOrElse {
        metricsService.count(BrmsMonitor.getCounter(NotAttempted))
        Future.successful(NotAttempted)
      }
    } else {
      Future.successful(NotAttempted)
    }
  }
}
