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

package controllers

import controllers.actions._
import models.UserAnswers
import models.requests.OptionalDataRequest
import pages.{EmptyWaypoints, TaskListPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{TaskListService, UserDataService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TaskListView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaskListController@Inject()(
                                   override val messagesApi: MessagesApi,
                                   identify: IdentifierAction,
                                   checkRecentClaims: CheckRecentClaimsAction,
                                   getData: DataRetrievalAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: TaskListView,
                                   taskListService: TaskListService,
                                   userDataService: UserDataService
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData).async {
    implicit request =>
      getUserAnswers.map { answers =>
        val sections = taskListService.sections(answers)
        Ok(view(sections))
      }
  }

  def onSubmit(): Action[AnyContent]=  (identify andThen checkRecentClaims andThen getData).async {
    implicit request =>
      getUserAnswers.map { answers =>
        Redirect(TaskListPage.navigate(EmptyWaypoints, answers, answers).route)
      }
  }

  private def getUserAnswers(implicit request: OptionalDataRequest[?]): Future[UserAnswers] =
    request
      .userAnswers
      .map(Future.successful)
      .getOrElse {
        val answers = UserAnswers(request.userId)
        userDataService
          .set(answers)
          .map(_ => answers)
      }
}
