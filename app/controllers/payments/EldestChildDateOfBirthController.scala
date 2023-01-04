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

package controllers.payments

import controllers.AnswerExtractor
import controllers.actions._
import forms.payments.EldestChildDateOfBirthFormProvider
import pages.Waypoints
import pages.payments.{EldestChildDateOfBirthPage, EldestChildNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.payments.EldestChildDateOfBirthView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EldestChildDateOfBirthController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        userDataService: UserDataService,
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: EldestChildDateOfBirthFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: EldestChildDateOfBirthView
                                      )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(EldestChildNamePage) {
        eldestChildName =>

          val firstName = HtmlFormat.escape(eldestChildName.firstName).toString
          val form = formProvider(firstName)

          val preparedForm = request.userAnswers.get(EldestChildDateOfBirthPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, firstName))
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(EldestChildNamePage) {
        eldestChildName =>

          val firstName = HtmlFormat.escape(eldestChildName.firstName).toString
          val form = formProvider(firstName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, firstName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(EldestChildDateOfBirthPage, value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(EldestChildDateOfBirthPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
        }
  }
}