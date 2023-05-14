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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.auth.core.ConfidenceLevel
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.NeedToUpliftIvView

import javax.inject.Inject

class NeedToUpliftIvController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: NeedToUpliftIvView,
                                          config: FrontendAppConfig
                                        ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>
      Ok(view())
  }

  def onSubmit(): Action[AnyContent] = Action {
    implicit request =>
      Redirect(
        config.upliftIvUrl,
        Map(
          "origin" -> Seq(config.origin),
          "confidenceLevel" -> Seq(ConfidenceLevel.L250.toString),
          "completionURL" -> Seq(config.loginContinueUrl + routes.RecentlyClaimedController.onPageLoad().url),
          "failureURL" -> Seq(config.loginContinueUrl + controllers.auth.routes.IvController.handleIvFailure(request.path, None).url)
        )
      )
  }
}
