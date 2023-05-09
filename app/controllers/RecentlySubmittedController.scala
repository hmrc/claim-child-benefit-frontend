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

import connectors.ClaimChildBenefitConnector
import controllers.actions._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RecentlySubmittedView

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RecentlySubmittedController @Inject()(
                                          override val messagesApi: MessagesApi,
                                          identify: IdentifierAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: RecentlySubmittedView,
                                          connector: ClaimChildBenefitConnector
                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy").withZone(ZoneId.systemDefault())

  def onPageLoad(): Action[AnyContent] = identify.async {
    implicit request =>
      connector
        .getRecentClaim()
        .map(_.map(claim => Ok(view(dateFormatter.format(claim.created))))
          .getOrElse(Redirect(routes.JourneyRecoveryController.onPageLoad())))

  }
}

