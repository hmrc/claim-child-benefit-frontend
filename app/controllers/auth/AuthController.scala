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

package controllers.auth

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.auth._

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AuthController @Inject()(
                                val controllerComponents: MessagesControllerComponents,
                                userDataService: UserDataService,
                                identify: IdentifierAction,
                                config: FrontendAppConfig,
                                unsupportedAffinityGroupAgentView: UnsupportedAffinityGroupAgentView,
                                unsupportedAffinityGroupOrganisationView: UnsupportedAffinityGroupOrganisationView
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
      userDataService
        .clear(request.userId)
        .map {
          _ =>
            Redirect(routes.SignedOutController.onPageLoad)
      }
  }

  def unsupportedAffinityGroupAgent: Action[AnyContent] = Action { implicit request =>
    Ok(unsupportedAffinityGroupAgentView())
  }

  def unsupportedAffinityGroupOrganisation(continueUrl: String): Action[AnyContent] = Action { implicit request =>
    Ok(unsupportedAffinityGroupOrganisationView(continueUrl))
  }
}
