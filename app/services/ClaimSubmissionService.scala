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
import connectors.ClaimChildBenefitConnector
import logging.Logging
import models.{AdditionalArchiveDetails, Done}
import models.domain.Claim
import models.requests.{AuthenticatedIdentifierRequest, DataRequest}
import services.ClaimSubmissionService.{CannotBuildJourneyModelException, NotAuthenticatedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.RequestOps._

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimSubmissionService @Inject()(
                                        featureFlags: FeatureFlags,
                                        connector: ClaimChildBenefitConnector,
                                        submissionLimiter: SubmissionLimiter,
                                        supplementaryDataService: SupplementaryDataService,
                                        immigrationStatusService: ImmigrationStatusService,
                                        journeyModelService: JourneyModelService
                                      ) extends Logging {

  def canSubmit(request: DataRequest[_])(implicit ec: ExecutionContext): Future[Boolean] =
    if (featureFlags.allowSubmissionToCbs) {
      if (request.signedIn) {

        val nino = request.request.asInstanceOf[AuthenticatedIdentifierRequest[_]].nino // TODO tidy this
        val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        submissionLimiter.allowedToSubmit(nino)(hc).flatMap {
          case true =>
            journeyModelService.build(request.userAnswers).right.map {
              journeyModel =>
                Future.successful(journeyModel.reasonsNotToSubmit.isEmpty)
            }.getOrElse(Future.successful(false))

          case false =>
            Future.successful(false)
        }
      } else {
        Future.successful(false)
      }
    } else {
      Future.successful(false)
    }

  def submit(request: DataRequest[_])(implicit ec: ExecutionContext): Future[Done] = {
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
                submissionLimiter
                  .recordSubmission(model, claim, correlationId)(hc)
                  .recover {
                    case e: Exception =>
                      logger.error("Failed to record submission: " + e.getMessage)
                      Done
                  }
              }.flatMap { _ =>
              val additionalDetails = AdditionalArchiveDetails(settledStatusStartDate)
              supplementaryDataService.submit(nino, model, correlationId, additionalDetails)(request).recover {
                case e: Exception =>
                  logger.error("Failed to submit supplementary data", e)
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
