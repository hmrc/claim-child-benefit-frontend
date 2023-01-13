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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.EmptyWaypoints
import pages.applicant.CheckApplicantDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.ApplicantNameSummary
import views.html.applicant.CheckApplicantDetailsView
import viewmodels.checkAnswers.applicant._
import viewmodels.govuk.summarylist._

import javax.inject.Inject

class CheckApplicantDetailsController  @Inject()(
                                                  override val messagesApi: MessagesApi,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: CheckApplicantDetailsView
                                                ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val thisPage = CheckApplicantDetailsPage
      val waypoints = EmptyWaypoints

      val applicantDetails = SummaryListViewModel(
        rows = Seq(
          ApplicantNinoKnownSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantNinoSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantNameSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantHasPreviousFamilyNameSummary.row(request.userAnswers, waypoints, thisPage),
          AddApplicantPreviousFamilyNameSummary.checkAnswersRow(request.userAnswers, waypoints, thisPage),
          ApplicantDateOfBirthSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantPhoneNumberSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantNationalitySummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantCurrentAddressInUkSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantCurrentUkAddressSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantCurrentInternationalAddressSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantLivedAtCurrentAddressOneYearSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantPreviousAddressInUkSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantPreviousUkAddressSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantPreviousInternationalAddressSummary.row(request.userAnswers, waypoints, thisPage)
        ).flatten
      )
      Ok(view(applicantDetails))
  }

  def onSubmit: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(CheckApplicantDetailsPage.navigate(EmptyWaypoints, request.userAnswers, request.userAnswers).route)
  }
}
