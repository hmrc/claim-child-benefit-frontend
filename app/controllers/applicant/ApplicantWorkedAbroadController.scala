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

package controllers.applicant

import controllers.actions._
import forms.applicant.ApplicantWorkedAbroadFormProvider
import pages.Waypoints
import pages.applicant.ApplicantWorkedAbroadPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.applicant.ApplicantWorkedAbroadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicantWorkedAbroadController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              userDataService: UserDataService,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: ApplicantWorkedAbroadFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: ApplicantWorkedAbroadView
                                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(ApplicantWorkedAbroadPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(ApplicantWorkedAbroadPage, value))
            _              <- userDataService.set(updatedAnswers)
          } yield Redirect(ApplicantWorkedAbroadPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
      )
  }
}
