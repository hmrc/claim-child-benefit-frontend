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
import forms.applicant.AddApplicantNationalityFormProvider
import pages.Waypoints
import pages.applicant.AddApplicantNationalityPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.applicant.AddApplicantNationalitySummary
import views.html.applicant.AddApplicantNationalityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddApplicantNationalityController @Inject()(
                                                          override val messagesApi: MessagesApi,
                                                          userDataService: UserDataService,
                                                          identify: IdentifierAction,
                                                          getData: DataRetrievalAction,
                                                          requireData: DataRequiredAction,
                                                          formProvider: AddApplicantNationalityFormProvider,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view: AddApplicantNationalityView
                                                        )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val otherNames = AddApplicantNationalitySummary.rows(request.userAnswers, waypoints, AddApplicantNationalityPage())

      Ok(view(form, waypoints, otherNames))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {
          val otherNames = AddApplicantNationalitySummary.rows(request.userAnswers, waypoints, AddApplicantNationalityPage())

          Future.successful(BadRequest(view(formWithErrors, waypoints, otherNames)))
        },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddApplicantNationalityPage(), value))
            _              <- userDataService.set(updatedAnswers)
          } yield Redirect(AddApplicantNationalityPage().navigate(waypoints, request.userAnswers, updatedAnswers).route)
      )
  }
}
