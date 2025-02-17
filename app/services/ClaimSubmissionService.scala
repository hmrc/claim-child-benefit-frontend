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

import audit.AuditService
import connectors.ClaimChildBenefitConnector
import logging.Logging
import models.domain.Claim
import models.requests.DataRequest
import models.{AdditionalArchiveDetails, Done, RecentClaim}
import play.api.i18n.Messages
import services.ClaimSubmissionService.{CannotBuildJourneyModelException, NotAuthenticatedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.RequestOps._

import java.time.{Clock, Instant}
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimSubmissionService @Inject()(
                                        connector: ClaimChildBenefitConnector,
                                        supplementaryDataService: SupplementaryDataService,
                                        immigrationStatusService: ImmigrationStatusService,
                                        journeyModelService: JourneyModelService,
                                        auditService: AuditService,
                                        userDataService: UserDataService,
                                        clock: Clock
                                      ) extends Logging {

  def canSubmit(request: DataRequest[?])(implicit messages: Messages): Future[Boolean] =
    if (request.signedIn) {
      journeyModelService.build(request.userAnswers).right.map {
        journeyModel =>
          Future.successful(journeyModel.reasonsNotToSubmit.isEmpty)
      }.getOrElse(Future.successful(false))
    } else {
      Future.successful(false)
    }

  def submit(request: DataRequest[?])(implicit ec: ExecutionContext, messages: Messages): Future[Done] = {
    for {
      nino                <- request.userAnswers.nino
      relationshipDetails <- request.userAnswers.relationshipDetails
    } yield {

      val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
      val correlationId = UUID.randomUUID()

      journeyModelService.build(request.userAnswers).right.map { model =>
        immigrationStatusService.settledStatusStartDate(nino, model, correlationId)(hc).flatMap {
          settledStatusStartDate =>

            val claim = Claim.build(nino, model, relationshipDetails.hasClaimedChildBenefit, settledStatusStartDate)

            connector
              .submitClaim(claim, correlationId)(hc)
              .flatMap { _ =>

                auditService.auditSubmissionToCbs(model, claim, correlationId)(hc)
                val recentClaim = RecentClaim.build(nino, request.userAnswers, Instant.now(clock))

                recentClaim.map { rc =>
                  connector
                    .recordRecentClaim(rc)(hc)
                    .recover {
                      case e: Exception =>
                        logger.debug("Failed to record recent submission", e)
                        logger.error("Failed to record recent submission")
                        Done
                    }
                }.getOrElse {
                  logger.error("Unable to build a recent claim from user's answers")
                  Future.successful(Done)
                }
              }.flatMap { _ =>
                val additionalDetails = AdditionalArchiveDetails(settledStatusStartDate)
                supplementaryDataService.submit(nino, model, correlationId, additionalDetails)(request).recover {
                  case e: Exception =>
                    logger.debug("Failed to submit supplementary data", e)
                    logger.error("Failed to submit supplementary data")
                    Done
                }
              }.flatMap { _ =>
                userDataService
                  .clear()(hc)
                  .map(_ => Done)
                  .recover {
                    case e: Exception =>
                      logger.debug("Failed to delete user data", e)
                      logger.error("Failed to delete user data")
                      Done
                  }
              }
        }
      }.getOrElse(Future.failed(CannotBuildJourneyModelException))
    }
  }.getOrElse(Future.failed(NotAuthenticatedException))
}

object ClaimSubmissionService {

  case object NotAuthenticatedException extends Exception("User is not authenticated")

  case object CannotBuildJourneyModelException extends Exception("Cannot build journey model")
}
