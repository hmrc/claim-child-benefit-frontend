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

import audit.AuditService
import connectors.{ClaimChildBenefitConnector, UserAllowListConnector}
import models.{Done, RecentClaim}
import models.domain.Claim
import models.journey.JourneyModel
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionLimiter {

  def recordSubmission(nino: String, model: JourneyModel, claim: Claim, correlationId: UUID)(implicit hc: HeaderCarrier): Future[Done]
}

class SubmissionsLimitedByAllowList @Inject()(
                                               configuration: Configuration,
                                               userAllowListConnector: UserAllowListConnector,
                                               auditService: AuditService,
                                               claimChildBenefitConnector: ClaimChildBenefitConnector,
                                               clock: Clock
                                             )(implicit ec: ExecutionContext) extends SubmissionLimiter {

  private val submissionFeature: String = configuration.get[String]("allow-list-features.submission")

  override def recordSubmission(nino: String, model: JourneyModel, claim: Claim, correlationId: UUID)(implicit hc: HeaderCarrier): Future[Done] = {
    auditService.auditSubmissionToCbs(model, claim, correlationId)
    val recentClaim = RecentClaim(nino, Instant.now(clock))

    claimChildBenefitConnector
      .recordRecentClaim(recentClaim)
      .map(_ => Done)
  }
}

class SubmissionsLimitedByThrottle @Inject()(
                                              configuration: Configuration,
                                              connector: ClaimChildBenefitConnector,
                                              userAllowListConnector: UserAllowListConnector,
                                              auditService: AuditService,
                                              clock: Clock
                                            )(implicit ec: ExecutionContext) extends SubmissionLimiter {

  private val submissionFeature: String = configuration.get[String]("allow-list-features.submission")

  override def recordSubmission(nino: String, model: JourneyModel, claim: Claim, correlationId: UUID)(implicit hc: HeaderCarrier): Future[Done] =
    connector
      .incrementThrottleCount()
      .flatMap { _ =>
        auditService.auditSubmissionToCbs(model, claim, correlationId)
        val recentClaim = RecentClaim(nino, Instant.now(clock))

        connector
          .recordRecentClaim(recentClaim)
          .map(_ => Done)
      }
}

class SubmissionsNotLimited @Inject()(
                                       auditService: AuditService,
                                       clock: Clock,
                                       claimChildBenefitConnector: ClaimChildBenefitConnector
                                     )(implicit ec: ExecutionContext) extends SubmissionLimiter {

  override def recordSubmission(nino: String, model: JourneyModel, claim: Claim, correlationId: UUID)(implicit hc: HeaderCarrier): Future[Done] = {
    auditService.auditSubmissionToCbs(model, claim, correlationId)
    val recentClaim = RecentClaim(nino, Instant.now(clock))

    claimChildBenefitConnector
      .recordRecentClaim(recentClaim)
      .map(_ => Done)
  }
}
