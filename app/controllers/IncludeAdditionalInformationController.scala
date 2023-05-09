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
import forms.IncludeAdditionalInformationFormProvider
import models.UserAnswers

import javax.inject.Inject
import pages.{IncludeAdditionalInformationPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.IncludeAdditionalInformationView

import scala.concurrent.{ExecutionContext, Future}

class IncludeAdditionalInformationController @Inject()(
                                           override val messagesApi: MessagesApi,
                                           userDataService: UserDataService,
                                           identify: IdentifierAction,
                                           checkRecentClaims: CheckRecentClaimsAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: IncludeAdditionalInformationFormProvider,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: IncludeAdditionalInformationView
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(IncludeAdditionalInformationPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints))),

        value => {

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(IncludeAdditionalInformationPage, value))
            _              <- userDataService.set(updatedAnswers)
          } yield Redirect(IncludeAdditionalInformationPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
        }
      )
  }
}
