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

package controllers.actions

import connectors.ClaimChildBenefitConnector
import controllers.routes
import models.requests.{AuthenticatedIdentifierRequest, IdentifierRequest, UnauthenticatedIdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import utils.FutureOps._

class CheckRecentClaimsAction @Inject() (connector: ClaimChildBenefitConnector)
                                        (implicit val executionContext: ExecutionContext) extends ActionFilter[IdentifierRequest] {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] =
    request match {
      case _: AuthenticatedIdentifierRequest[?] =>
        val hc = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        connector
          .getRecentClaim()(hc)
          .map(_.map(_ => Redirect(routes.RecentlySubmittedController.onPageLoad())))
          .logFailure(s"RecentClaim failed on ${request.path}.")

      case _: UnauthenticatedIdentifierRequest[?] =>
        Future.successful(None)
    }
}
