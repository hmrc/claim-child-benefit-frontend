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

import controllers.actions._
import controllers.{routes => baseRoutes}
import forms.child.DateChildStartedLivingWithApplicantFormProvider
import models.requests.DataRequest
import models.{AdultName, ChildName, Index}
import pages.Waypoints
import pages.applicant.{ApplicantNamePage, DesignatoryNamePage}
import pages.child.{ChildNamePage, DateChildStartedLivingWithApplicantPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.child.DateChildStartedLivingWithApplicantView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DateChildStartedLivingWithApplicantController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            userDataService: UserDataService,
                                            identify: IdentifierAction,
                                            checkRecentClaims: CheckRecentClaimsAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: DateChildStartedLivingWithApplicantFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: DateChildStartedLivingWithApplicantView
                                          )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getNames(index) {
        case (childName, applicantName) =>

          val form = formProvider(childName)

          val preparedForm = request.userAnswers.get(DateChildStartedLivingWithApplicantPage(index)) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Future.successful(Ok(view(preparedForm, waypoints, index, childName, applicantName)))
      }
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getNames(index) {
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

  private def getNames(index: Index)
                      (block: (ChildName, AdultName) => Future[Result])
                      (implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers
      .get(ChildNamePage(index))
      .flatMap {
        childName =>
          if (request.userAnswers.isAuthenticated) {
            request.userAnswers
              .get(DesignatoryNamePage)
              .orElse(request.userAnswers.designatoryDetails.flatMap(_.preferredName))
              .map(block(childName, _))
          } else {
            request.userAnswers
              .get(ApplicantNamePage)
              .map(block(childName, _))
          }
      }.getOrElse(Future.successful(Redirect(baseRoutes.JourneyRecoveryController.onPageLoad())))

}