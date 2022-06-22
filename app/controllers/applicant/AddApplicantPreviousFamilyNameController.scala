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

package controllers.applicant

import controllers.actions._
import forms.applicant.AddApplicantPreviousFamilyNameFormProvider
import pages.Waypoints
import pages.applicant.AddApplicantPreviousFamilyNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.applicant.AddApplicantPreviousFamilyNameSummary
import views.html.applicant.AddApplicantPreviousFamilyNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddApplicantPreviousFamilyNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         sessionRepository: SessionRepository,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: AddApplicantPreviousFamilyNameFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: AddApplicantPreviousFamilyNameView
                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val otherNames = AddApplicantPreviousFamilyNameSummary.rows(request.userAnswers, waypoints, AddApplicantPreviousFamilyNamePage)

      Ok(view(form, waypoints, otherNames))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {
          val otherNames = AddApplicantPreviousFamilyNameSummary.rows(request.userAnswers, waypoints, AddApplicantPreviousFamilyNamePage)

          Future.successful(BadRequest(view(formWithErrors, waypoints, otherNames)))
        },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddApplicantPreviousFamilyNamePage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(AddApplicantPreviousFamilyNamePage.navigate(waypoints, updatedAnswers))
      )
  }
}
