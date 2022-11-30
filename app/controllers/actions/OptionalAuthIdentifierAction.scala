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

package controllers.actions

import config.FrontendAppConfig
import controllers.auth.{routes => authRoutes}
import controllers.routes
import models.requests.{AuthenticatedIdentifierRequest, IdentifierRequest, UnauthenticatedIdentifierRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{BodyParsers, Call, Request, Result}
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, CredentialStrength, IncorrectCredentialStrength, NoActiveSession}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class OptionalAuthIdentifierAction(
                                    val authConnector: AuthConnector,
                                    val parser: BodyParsers.Default,
                                    config: FrontendAppConfig
                                  )(implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised()
      .retrieve(
        Retrievals.affinityGroup and
          Retrievals.credentialStrength and
          Retrievals.internalId and
          Retrievals.nino
      ) {
        case Some(Agent) ~ _ ~ _ ~ _ =>
          redirectTo(authRoutes.AuthController.unsupportedAffinityGroupAgent)

        case Some(Organisation) ~ _ ~ _ ~ _ =>
          redirectTo(authRoutes.AuthController.unsupportedAffinityGroupOrganisation(config.loginContinueUrl + request.path))

        case Some(Individual) ~ Some(CredentialStrength.weak) ~ _ ~ _ =>
          upliftMfa(request)

        case Some(Individual) ~ Some(CredentialStrength.strong) ~ Some(internalId) ~ Some(nino) =>
          block(AuthenticatedIdentifierRequest(request, internalId, nino))
    }.recoverWith {
      case _: NoActiveSession =>
        hc.sessionId match {
          case Some(sessionId) =>
            block(UnauthenticatedIdentifierRequest(request, sessionId.value))
          case None =>
            Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        }
    }
  }

  private def redirectTo(call: Call): Future[Result] =
    Future.successful(Redirect(call))

  private def upliftMfa(request: Request[_]): Future[Result] = {
    Future.successful(Redirect(
      config.upliftMfaUrl,
      Map(
        "origin" -> Seq(config.origin),
        "continueUrl" -> Seq(config.loginContinueUrl + request.path)
      )
    ))
  }
}
