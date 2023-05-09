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

import controllers.actions.{CheckRecentClaimsAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.EmptyWaypoints
import pages.applicant.CheckDesignatoryDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.applicant._
import viewmodels.govuk.summarylist._
import views.html.applicant.CheckDesignatoryDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckDesignatoryDetailsController  @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  checkRecentClaims: CheckRecentClaimsAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckDesignatoryDetailsView,
                                                  userDataService: UserDataService
                                                )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>

      val waypoints = EmptyWaypoints

      val designatoryDetails = SummaryListViewModel(
        rows = Seq(
          DesignatoryNameSummary.row(request.userAnswers, waypoints),
          DesignatoryAddressSummary.row(request.userAnswers, waypoints),
          CorrespondenceAddressSummary.row(request.userAnswers, waypoints)
        ).flatten
      )
      Ok(view(designatoryDetails))
  }

  def onSubmit: Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>

      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(CheckDesignatoryDetailsPage, true))
        _              <- userDataService.set(updatedAnswers)
      } yield Redirect(CheckDesignatoryDetailsPage.navigate(EmptyWaypoints, request.userAnswers, request.userAnswers).route)
  }
}
