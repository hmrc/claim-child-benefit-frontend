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

package controllers.child

import controllers.AnswerExtractor
import controllers.actions._
import forms.child.ChildPreviousNameFormProvider
import models.Index
import pages.Waypoints
import pages.child.{ChildNamePage, ChildPreviousNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.child.ChildPreviousNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ChildPreviousNameController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      userDataService: UserDataService,
                                      identify: IdentifierAction,
                                      checkRecentClaims: CheckRecentClaimsAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: ChildPreviousNameFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: ChildPreviousNameView
                                     )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {



  def onPageLoad(waypoints: Waypoints, childIndex: Index, nameIndex: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswer(ChildNamePage(childIndex)) {
        childName =>

          val form = formProvider(childName)

          val preparedForm = request.userAnswers.get(ChildPreviousNamePage(childIndex, nameIndex)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, childIndex, nameIndex, childName))
      }
  }

  def onSubmit(waypoints: Waypoints, childIndex: Index, nameIndex: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(ChildNamePage(childIndex)) {
        childName =>

          val form = formProvider(childName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, childIndex, nameIndex, childName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ChildPreviousNamePage(childIndex, nameIndex), value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(ChildPreviousNamePage(childIndex, nameIndex).navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}