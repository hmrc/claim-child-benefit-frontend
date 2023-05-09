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

import controllers.actions._
import pages.{EmptyWaypoints, TaskListPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TaskListService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TaskListView

import javax.inject.Inject

class TaskListController@Inject()(
                                   override val messagesApi: MessagesApi,
                                   identify: IdentifierAction,
                                   checkRecentClaims: CheckRecentClaimsAction,
                                   getData: DataRetrievalAction,
                                   requireData: DataRequiredAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: TaskListView,
                                   taskListService: TaskListService
                                 ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>

      val sections = taskListService.sections(request.userAnswers)
      Ok(view(sections))
  }

  def onSubmit(): Action[AnyContent]=  (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>

      Redirect(TaskListPage.navigate(EmptyWaypoints, request.userAnswers, request.userAnswers).route)
  }
}
