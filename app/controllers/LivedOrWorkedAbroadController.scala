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

package controllers

import controllers.actions._
import forms.LivedOrWorkedAbroadFormProvider
import models.RelationshipStatus._
import models.UserAnswers

import javax.inject.Inject
import pages.{LivedOrWorkedAbroadPage, RelationshipStatusPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.LivedOrWorkedAbroadView

import scala.concurrent.{ExecutionContext, Future}

class LivedOrWorkedAbroadController @Inject()(
                                               override val messagesApi: MessagesApi,
                                               sessionRepository: SessionRepository,
                                               identify: IdentifierAction,
                                               getData: DataRetrievalAction,
                                               requireData: DataRequiredAction,
                                               formProvider: LivedOrWorkedAbroadFormProvider,
                                               val controllerComponents: MessagesControllerComponents,
                                               view: LivedOrWorkedAbroadView
                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(RelationshipStatusPage) {
        relationshipStatus =>

          val singleOrCouple = relationshipStatus match {
            case Married | Cohabiting                    => "couple"
            case Single | Separated | Widowed | Divorced => "single"
          }

          val form = formProvider(singleOrCouple)

          val preparedForm = request.userAnswers.get(LivedOrWorkedAbroadPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, singleOrCouple))
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(RelationshipStatusPage) {
        relationshipStatus =>

          val singleOrCouple = relationshipStatus match {
            case Married | Cohabiting                    => "couple"
            case Single | Separated | Widowed | Divorced => "single"
          }

          val form = formProvider(singleOrCouple)
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, singleOrCouple))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(LivedOrWorkedAbroadPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(LivedOrWorkedAbroadPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)

          )
      }
  }
}
