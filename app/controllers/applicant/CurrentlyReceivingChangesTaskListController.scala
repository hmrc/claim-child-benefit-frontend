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

package controllers.applicant

import controllers.actions._
import pages.Waypoints
import pages.applicant.CurrentlyReceivingChangesTaskListPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.applicant.CurrentlyReceivingChangesTaskListView

import javax.inject.Inject

class CurrentlyReceivingChangesTaskListController @Inject()(
                                                             override val messagesApi: MessagesApi,
                                                             identify: IdentifierAction,
                                                             checkRecentClaims: CheckRecentClaimsAction,
                                                             getData: DataRetrievalAction,
                                                             requireData: DataRequiredAction,
                                                             val controllerComponents: MessagesControllerComponents,
                                                             view: CurrentlyReceivingChangesTaskListView
                                                           ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      Ok(view(waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      Redirect(CurrentlyReceivingChangesTaskListPage.navigate(waypoints, request.userAnswers, request.userAnswers).route)
  }
}
