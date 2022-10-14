/*
 * Copyright 2022 HM Revenue & Customs
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
import models.BirthRegistrationMatchingResult._
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResult}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BrmsService @Inject()(brmsConnector: BrmsConnector, featureFlags: FeatureFlags) extends Logging {

  def matchChild(request: BirthRegistrationMatchingRequest)
                (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[BirthRegistrationMatchingResult] = {
    if (featureFlags.matchBirthRegistrationDetails) {
      brmsConnector.matchChild(request).map { response =>
        if (response.matched) Matched
        else                  NotMatched
      }.recover {
        case e: Exception =>
          logger.warn("Error calling BRMS", e.getMessage)
          BirthRegistrationMatchingResult.MatchingAttemptFailed
      }
    } else {
      Future.successful(NotAttempted)
    }
  }
}
