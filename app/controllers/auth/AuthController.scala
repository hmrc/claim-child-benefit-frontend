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

package controllers.auth

import config.FrontendAppConfig
import connectors.ClaimChildBenefitConnector
import controllers.actions.{CheckRecentClaimsAction, DataRetrievalAction, IdentifierAction}
import controllers.{routes => baseRoutes}
import models.UserAnswers
import models.requests.AuthenticatedIdentifierRequest
import pages.{EmptyWaypoints, TaskListPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.auth._

import java.time.{Clock, Instant}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                userDataService: UserDataService,
                                identify: IdentifierAction,
                                checkRecentClaims: CheckRecentClaimsAction,
                                getData: DataRetrievalAction,
                                config: FrontendAppConfig,
                                unsupportedAffinityGroupAgentView: UnsupportedAffinityGroupAgentView,
                                unsupportedAffinityGroupOrganisationView: UnsupportedAffinityGroupOrganisationView,
                                clock: Clock,
                                connector: ClaimChildBenefitConnector
                              )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def redirectToRegister(continueUrl: String): Action[AnyContent] = Action { _ =>
    Redirect(
      config.registerUrl,
      Map(
        "origin" -> Seq(config.origin),
        "continueUrl" -> Seq(continueUrl),
        "accountType" -> Seq("Individual"))
    )
  }

  def redirectToLogin(continueUrl: String): Action[AnyContent] = Action { _ =>
    Redirect(
      config.loginUrl,
      Map(
        "origin" -> Seq(config.origin),
        "continue" -> Seq(continueUrl)
      )
    )
  }

  def signOut(): Action[AnyContent] = identify.async {
    implicit request =>
      request match {
        case _: AuthenticatedIdentifierRequest[_] =>
          connector
            .getRecentClaim()
            .map {
              _.map(_ => Redirect(config.signOutUrl, Map("continue" -> Seq(config.host + routes.SignedOutAfterSubmissionController.onPageLoad.url))))
                .getOrElse(Redirect(config.signOutUrl, Map("continue" -> Seq(config.host + routes.SignedOutController.onPageLoad.url))))
            }

        case _ =>
          userDataService
            .clear()
            .map { _ =>
              Redirect(routes.ApplicationResetController.onPageLoad)
            }
      }
  }

  def signOutAndApplyUnauthenticated(): Action[AnyContent] = Action { _ =>
      Redirect(
        config.signOutUrl,
        Map("continue" -> Seq(config.host + baseRoutes.RecentlyClaimedController.onPageLoad().url)))
  }

  def unsupportedAffinityGroupAgent(continueUrl: String): Action[AnyContent] = Action { implicit request =>
    Ok(unsupportedAffinityGroupAgentView(continueUrl))
  }

  def unsupportedAffinityGroupOrganisation(continueUrl: String): Action[AnyContent] = Action { implicit request =>
    Ok(unsupportedAffinityGroupOrganisationView(continueUrl))
  }

  def signedIn(): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData).async {
    implicit request =>
      request.userAnswers
        .map(_ => Future.successful(Redirect(TaskListPage.route(EmptyWaypoints).url)))
        .getOrElse {
          userDataService
            .set(UserAnswers(request.userId, lastUpdated = Instant.now(clock)))
            .map(_ => Redirect(TaskListPage.route(EmptyWaypoints).url))
        }
  }
}
