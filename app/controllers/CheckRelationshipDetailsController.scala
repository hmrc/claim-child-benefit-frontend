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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.EmptyWaypoints
import pages.CheckRelationshipDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.applicant.ApplicantIsHmfOrCivilServantSummary
import viewmodels.checkAnswers.{AlwaysLivedInUkSummary, CohabitationDateSummary, RelationshipStatusSummary, SeparationDateSummary}
import views.html.CheckRelationshipDetailsView
import viewmodels.checkAnswers.partner._
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CheckRelationshipDetailsController  @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: CheckRelationshipDetailsView
                                              ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val thisPage = CheckRelationshipDetailsPage
      val waypoints = EmptyWaypoints

      val partnerDetails = SummaryListViewModel(
        rows = Seq(
          RelationshipStatusSummary.row(request.userAnswers, waypoints, thisPage),
          CohabitationDateSummary.row(request.userAnswers, waypoints, thisPage),
          SeparationDateSummary.row(request.userAnswers, waypoints, thisPage),
          AlwaysLivedInUkSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantIsHmfOrCivilServantSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerIsHmfOrCivilServantSummary.row(request.userAnswers, waypoints, thisPage),
        ).flatten
      )
      Ok(view(partnerDetails))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(CheckRelationshipDetailsPage.navigate(EmptyWaypoints, request.userAnswers, request.userAnswers).route)
  }
}
