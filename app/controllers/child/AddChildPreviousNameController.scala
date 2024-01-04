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
import forms.child.AddChildPreviousNameFormProvider
import models.Index
import pages.Waypoints
import pages.child.{AddChildPreviousNamePage, ChildNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.child.AddChildPreviousNameSummary
import views.html.child.AddChildPreviousNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddChildPreviousNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         userDataService: UserDataService,
                                         identify: IdentifierAction,
                                         checkRecentClaims: CheckRecentClaimsAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddChildPreviousNameFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AddChildPreviousNameView
                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswer(ChildNamePage(index)) {
        childName =>

          val previousNames = AddChildPreviousNameSummary.rows(request.userAnswers, index, waypoints, AddChildPreviousNamePage(index))

          Ok(view(form, waypoints, index, childName.firstName, previousNames))
      }
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(ChildNamePage(index)) {
        childName =>

          form.bindFromRequest().fold(
            formWithErrors => {
              val previousNames = AddChildPreviousNameSummary.rows(request.userAnswers, index, waypoints, AddChildPreviousNamePage(index))

              Future.successful(BadRequest(view(formWithErrors, waypoints, index, childName.firstName, previousNames)))
            },

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddChildPreviousNamePage(index), value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(AddChildPreviousNamePage(index).navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}