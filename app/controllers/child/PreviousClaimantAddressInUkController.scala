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
import forms.child.PreviousClaimantAddressInUkFormProvider
import models.Index
import pages.Waypoints
import pages.child.{PreviousClaimantAddressInUkPage, PreviousClaimantNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.child.PreviousClaimantAddressInUkView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PreviousClaimantAddressInUkController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     sessionRepository: SessionRepository,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: PreviousClaimantAddressInUkFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: PreviousClaimantAddressInUkView
                                                   )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(PreviousClaimantNamePage(index)) {
        previousClaimantName =>

          val form = formProvider(previousClaimantName)

          val preparedForm = request.userAnswers.get(PreviousClaimantAddressInUkPage(index)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, index, previousClaimantName))
      }
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(PreviousClaimantNamePage(index)) {
        previousClaimantName =>

          val form = formProvider(previousClaimantName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, index, previousClaimantName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviousClaimantAddressInUkPage(index), value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(PreviousClaimantAddressInUkPage(index).navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}