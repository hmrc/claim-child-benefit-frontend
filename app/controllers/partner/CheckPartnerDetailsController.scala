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

package controllers.partner

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.EmptyWaypoints
import pages.partner.CheckPartnerDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.CheckPartnerDetailsView
import viewmodels.checkAnswers.partner._
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CheckPartnerDetailsController  @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckPartnerDetailsView
                                                ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val thisPage = CheckPartnerDetailsPage
      val waypoints = EmptyWaypoints

      val partnerDetails = SummaryListViewModel(
        rows = Seq(
          RelationshipStatusSummary.row(request.userAnswers, waypoints, thisPage),
          SeparationDateSummary.row(request.userAnswers, waypoints, thisPage),
          CohabitationDateSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerNameSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerNinoKnownSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerNinoSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerDateOfBirthSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerNationalitySummary.row(request.userAnswers, waypoints, thisPage),
          PartnerClaimingChildBenefitSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerEldestChildNameSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerEldestChildDateOfBirthSummary.row(request.userAnswers, waypoints, thisPage)
        ).flatten
      )
      Ok(view(partnerDetails))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(CheckPartnerDetailsPage.navigate(EmptyWaypoints, request.userAnswers, request.userAnswers).route)
  }
}
