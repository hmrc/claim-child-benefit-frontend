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

import controllers.AnswerExtractor
import controllers.actions._
import forms.applicant.RemoveApplicantPreviousFamilyNameFormProvider
import models.Index
import pages.{Waypoints, applicant}
import pages.applicant.{ApplicantPreviousFamilyNamePage, RemoveApplicantPreviousFamilyNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.applicant.RemoveApplicantPreviousFamilyNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveApplicantPreviousFamilyNameController @Inject()(
                                         override val messagesApi: MessagesApi,
                                         userDataService: UserDataService,
                                         identify: IdentifierAction,
                                         getData: DataRetrievalAction,
                                         requireData: DataRequiredAction,
                                         formProvider: RemoveApplicantPreviousFamilyNameFormProvider,
                                         val controllerComponents: MessagesControllerComponents,
                                         view: RemoveApplicantPreviousFamilyNameView
                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(ApplicantPreviousFamilyNamePage(index)) {
        otherName =>

          val safeName = HtmlFormat.escape(otherName).toString
          val form = formProvider(safeName)

          Ok(view(form, waypoints, index, safeName))
      }
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(applicant.ApplicantPreviousFamilyNamePage(index)) {
        otherName =>

          val safeName = HtmlFormat.escape(otherName).toString
          val form = formProvider(safeName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, index, safeName))),

            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(ApplicantPreviousFamilyNamePage(index)))
                  _ <- userDataService.set(updatedAnswers)
                } yield Redirect(RemoveApplicantPreviousFamilyNamePage(index).navigate(waypoints, request.userAnswers, updatedAnswers).route)
              } else {
                Future.successful(Redirect(RemoveApplicantPreviousFamilyNamePage(index).navigate(waypoints, request.userAnswers, request.userAnswers).route))
              }
          )
      }
  }
}