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

package controllers.child

import controllers.AnswerExtractor
import controllers.actions._
import forms.child.RemoveChildPreviousNameFormProvider
import models.Index
import pages.Waypoints
import pages.child.{ChildPreviousNamePage, RemoveChildPreviousNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.child.RemoveChildPreviousNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveChildPreviousNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: RemoveChildPreviousNameFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemoveChildPreviousNameView
                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints, childIndex: Index, nameIndex: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(ChildPreviousNamePage(childIndex, nameIndex)) {
        previousName =>

          val safeName = HtmlFormat.escape(previousName.fullName).toString
          val form = formProvider(safeName)

          val preparedForm = request.userAnswers.get(RemoveChildPreviousNamePage(childIndex, nameIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, childIndex, nameIndex, safeName))
      }
  }

  def onSubmit(waypoints: Waypoints, childIndex: Index, nameIndex: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(ChildPreviousNamePage(childIndex, nameIndex)) {
        previousName =>

          val safeName = HtmlFormat.escape(previousName.fullName).toString
          val form = formProvider(safeName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, childIndex, nameIndex, safeName))),

            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(ChildPreviousNamePage(childIndex, nameIndex)))
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(RemoveChildPreviousNamePage(childIndex, nameIndex).navigate(waypoints, updatedAnswers))
              } else {
                Future.successful(Redirect(RemoveChildPreviousNamePage(childIndex, nameIndex).navigate(waypoints, request.userAnswers)))
              }
          )
      }
  }
}
