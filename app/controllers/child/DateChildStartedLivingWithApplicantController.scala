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

package controllers.child

import controllers.AnswerExtractor
import controllers.actions._
import forms.child.DateChildStartedLivingWithApplicantFormProvider
import models.Index
import pages.{ApplicantNamePage, Waypoints}
import pages.child.{ChildNamePage, DateChildStartedLivingWithApplicantPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.child.DateChildStartedLivingWithApplicantView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateChildStartedLivingWithApplicantController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            userDataService: UserDataService,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: DateChildStartedLivingWithApplicantFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: DateChildStartedLivingWithApplicantView
                                          )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswers(ChildNamePage(index), ApplicantNamePage) {
        case (childName, applicantName) =>

          val form = formProvider(childName)

          val preparedForm = request.userAnswers.get(DateChildStartedLivingWithApplicantPage(index)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, index, childName, applicantName))
      }
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswersAsync(ChildNamePage(index), ApplicantNamePage) {
        case (childName, applicantName) =>

          val form = formProvider(childName)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, index, childName, applicantName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(DateChildStartedLivingWithApplicantPage(index), value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(DateChildStartedLivingWithApplicantPage(index).navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}