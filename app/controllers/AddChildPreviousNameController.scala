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
import forms.AddChildPreviousNameFormProvider
import models.Index

import javax.inject.Inject
import pages.{AddChildPreviousNamePage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AddChildPreviousNameView

import scala.concurrent.{ExecutionContext, Future}

class AddChildPreviousNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddChildPreviousNameFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AddChildPreviousNameView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(AddChildPreviousNamePage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, index))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddChildPreviousNamePage(index), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(AddChildPreviousNamePage(index).navigate(waypoints, updatedAnswers))
      )
  }
}
