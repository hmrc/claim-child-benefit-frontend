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
import models.requests.DataRequest
import models.{JourneyModel, JourneyModelProvider}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.RequestOps._

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ClaimSubmissionService @Inject()(
                                        featureFlags: FeatureFlags,
                                        connector: ClaimChildBenefitConnector,
                                        journeyModelProvider: JourneyModelProvider,
                                        clock: Clock
                                      ) {

  def canSubmit(request: DataRequest[_])(implicit ec: ExecutionContext): Future[Boolean] = {

    val childAgeLimit = LocalDate.now(clock).minusMonths(6)

    def dataAllowsSubmission(journeyModel: JourneyModel): Boolean = {

      val anyReasonNotToSubmit =
        journeyModel.children.exists(_.dateOfBirth.isBefore(childAgeLimit)) ||
          journeyModel.anyDocumentsRequired ||
          journeyModel.applicant.changedDesignatoryDetails.contains(true) ||
          (journeyModel.relationship.partner.exists(_.eldestChild.nonEmpty) && journeyModel.relationship.partner.exists(_.nationalInsuranceNumber.isEmpty)) ||
          journeyModel.additionalInformation.nonEmpty

      !anyReasonNotToSubmit
    }

    if (featureFlags.allowSubmissionToCbs) {
      if (request.signedIn) {

          val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

          connector.checkAllowlist()(hc).flatMap {
            case true =>
              journeyModelProvider.buildFromUserAnswers(request.userAnswers)(hc).flatMap {
                result =>
                  result.right.map { journeyModel =>
                    Future.successful(dataAllowsSubmission(journeyModel))
                  }.getOrElse(Future.successful(false))
              }
            case false =>
              Future.successful(false)
          }
      } else {
        Future.successful(false)
      }
    } else {
      Future.successful(false)
    }
  }
}
