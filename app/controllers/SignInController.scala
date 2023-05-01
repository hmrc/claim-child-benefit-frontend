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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import controllers.auth.{routes => authRoutes}
import forms.SignInFormProvider
import pages.{TaskListPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SignInView

import javax.inject.Inject

class SignInController @Inject()(
                                    override val messagesApi: MessagesApi,
                                    identify: IdentifierAction,
                                    checkRecentClaims: CheckRecentClaimsAction,
                                    getData: DataRetrievalAction,
                                    formProvider: SignInFormProvider,
                                    val controllerComponents: MessagesControllerComponents,
                                    view: SignInView,
                                    config: FrontendAppConfig
                                  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData) {
    implicit request =>
      Ok(view(form, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData) {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          BadRequest(view(formWithErrors, waypoints)),

        value =>
          if (value) {
            Redirect(authRoutes.AuthController.redirectToLogin(config.loginContinueUrl + config.signedInUrl))
          } else {
            Redirect(TaskListPage.route(waypoints).url)
          }
      )
  }
}
