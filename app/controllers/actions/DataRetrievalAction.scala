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

package controllers.actions

import connectors.ClaimChildBenefitConnector
import logging.Logging
import models.requests.{AuthenticatedIdentifierRequest, IdentifierRequest, OptionalDataRequest, UnauthenticatedIdentifierRequest}
import play.api.mvc.ActionTransformer
import services.UserDataService
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.FutureOps._

class DataRetrievalActionImpl @Inject()(
                                         val userDataService: UserDataService,
                                         val connector: ClaimChildBenefitConnector
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction with Logging {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {
    val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request match {
      case a: AuthenticatedIdentifierRequest[_] =>
        for {
          maybeUserAnswers <- userDataService.get()(hc).logFailure(s"UserDataService failed on get on ${request.path}")
          designatoryDetails <- connector.designatoryDetails()(hc).logFailure(s"DesignatoryDetails failed on ${request.path}.")
          relationshipDetails <- connector.relationshipDetails()(hc).logFailure(s"RelationshipDetails failed on ${request.path}.")
        } yield OptionalDataRequest(
          request,
          request.userId,
          maybeUserAnswers.map(_.copy(
            nino = Some(a.nino),
            designatoryDetails = Some(designatoryDetails),
            relationshipDetails = Some(relationshipDetails)
          ))
        )


      case _: UnauthenticatedIdentifierRequest[_] =>
        userDataService.get()(hc).map { maybeAnswers =>
          OptionalDataRequest(request, request.userId, maybeAnswers)
        }.logFailure(s"UserDataService failed on get on ${request.path}.")
    }
  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
