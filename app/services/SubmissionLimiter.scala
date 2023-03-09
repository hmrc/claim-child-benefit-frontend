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
import connectors.ClaimChildBenefitConnector
import models.{Done, JourneyModel}
import models.domain.Claim
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait SubmissionLimiter {

  def allowedToSubmit(hc: HeaderCarrier): Future[Boolean]

  def recordSubmission(model: JourneyModel, claim: Claim, correlationId: UUID)(hc: HeaderCarrier): Future[Done]
}

class SubmissionsLimitedByAllowList @Inject()(connector: ClaimChildBenefitConnector, auditService: AuditService)
                                             (implicit ec: ExecutionContext) extends SubmissionLimiter {

  override def allowedToSubmit(hc: HeaderCarrier): Future[Boolean] =
    connector.checkAllowlist()(hc)

  override def recordSubmission(model: JourneyModel, claim: Claim, correlationId: UUID)(hc: HeaderCarrier): Future[Done] = {
    auditService.auditSubmissionToCbs(model, claim, correlationId)(hc)
    Future.successful(Done)
  }
}

class SubmissionsLimitedByThrottle @Inject()(connector: ClaimChildBenefitConnector, auditService: AuditService)
                                            (implicit ec: ExecutionContext)extends SubmissionLimiter {

  override def allowedToSubmit(hc: HeaderCarrier): Future[Boolean] =
    connector
      .checkThrottleLimit()(hc)
      .map(response => !response.limitReached)

  override def recordSubmission(model: JourneyModel, claim: Claim, correlationId: UUID)(hc: HeaderCarrier): Future[Done] =
    connector
      .incrementThrottleCount()(hc)
      .map { _ =>
        auditService.auditSubmissionToCbs(model, claim, correlationId)(hc)
        Done
      }
}

class SubmissionsNotLimited @Inject()(auditService: AuditService) extends SubmissionLimiter {

  override def allowedToSubmit(hc: HeaderCarrier): Future[Boolean] =
    Future.successful(true)

  override def recordSubmission(model: JourneyModel, claim: Claim, correlationId: UUID)(hc: HeaderCarrier): Future[Done] = {
    auditService.auditSubmissionToCbs(model, claim, correlationId)(hc)
    Future.successful(Done)
  }
}
