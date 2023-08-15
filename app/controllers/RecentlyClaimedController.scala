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
import forms.RecentlyClaimedFormProvider
import models.UserAnswers
import models.requests.AuthenticatedIdentifierRequest

import javax.inject.Inject
import pages.{RecentlyClaimedPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RecentlyClaimedView

import scala.concurrent.{ExecutionContext, Future}

class RecentlyClaimedController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   userDataService: UserDataService,
                                                   identify: IdentifierAction,
                                                   checkRecentClaims: CheckRecentClaimsAction,
                                                   getData: DataRetrievalAction,
                                                   formProvider: RecentlyClaimedFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: RecentlyClaimedView,
                                                   config: FrontendAppConfig
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()
  val recentlyClaimedPage = RecentlyClaimedPage(config)
  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData) {
    implicit request =>

      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(recentlyClaimedPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints))),

        value => {

          val originalAnswers = request.userAnswers.getOrElse {
            request.request match {
              case r: AuthenticatedIdentifierRequest[_] =>
                UserAnswers(r.userId, nino = Some(r.nino))

              case r =>
                UserAnswers(r.userId)
            }
          }

          for {
            updatedAnswers <- Future.fromTry(originalAnswers.set(recentlyClaimedPage, value))
            _              <- userDataService.set(updatedAnswers)
          } yield Redirect(recentlyClaimedPage.navigate(waypoints, originalAnswers, updatedAnswers).route)
        }
      )
  }
}
