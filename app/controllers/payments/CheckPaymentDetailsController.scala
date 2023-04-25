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

package controllers.payments

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.EmptyWaypoints
import pages.payments.CheckPaymentDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.payments._
import viewmodels.govuk.summarylist._
import views.html.payments.CheckPaymentDetailsView

import javax.inject.Inject

class CheckPaymentDetailsController  @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: CheckPaymentDetailsView
                                              ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val thisPage = CheckPaymentDetailsPage
      val waypoints = EmptyWaypoints

      val paymentsDetails = SummaryListViewModel(
        rows = Seq(
          ApplicantIncomeSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerIncomeSummary.row(request.userAnswers, waypoints, thisPage),
          WantToBePaidSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantBenefitsSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantOrPartnerBenefitsSummary.row(request.userAnswers, waypoints, thisPage),
          PaymentFrequencySummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantHasSuitableAccountSummary.row(request.userAnswers, waypoints, thisPage),
          BankAccountHolderSummary.row(request.userAnswers, waypoints, thisPage),
          AccountTypeSummary.row(request.userAnswers, waypoints, thisPage),
          BankAccountDetailsSummary.row(request.userAnswers, waypoints, thisPage),
          BuildingSocietyDetailsSummary.row(request.userAnswers, waypoints, thisPage),
        ).flatten
      )
      Ok(view(paymentsDetails))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(CheckPaymentDetailsPage.navigate(EmptyWaypoints, request.userAnswers, request.userAnswers).route)
  }
}
