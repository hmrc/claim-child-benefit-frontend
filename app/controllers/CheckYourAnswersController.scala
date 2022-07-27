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

package controllers

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.{CheckYourAnswersPage, EmptyWaypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers._
import viewmodels.checkAnswers.applicant._
import viewmodels.checkAnswers.child._
import viewmodels.checkAnswers.income._
import viewmodels.checkAnswers.partner._
import viewmodels.checkAnswers.payments._
import viewmodels.govuk.summarylist._
import views.html.CheckYourAnswersView

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val thisPage  = CheckYourAnswersPage
      val waypoints = EmptyWaypoints

      val personalDetails = SummaryListViewModel(
        rows = Seq(
          ApplicantNameSummary.row(request.userAnswers, waypoints, thisPage),
          RelationshipStatusSummary.row(request.userAnswers, waypoints, thisPage),
          CohabitationDateSummary.row(request.userAnswers, waypoints, thisPage),
          SeparationDateSummary.row(request.userAnswers, waypoints, thisPage)
        ).flatten
      )

      val incomeDetails = SummaryListViewModel(
        rows = Seq(
          ApplicantIncomeOver50kSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantIncomeOver60kSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantBenefitsSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantOrPartnerIncomeOver50kSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantOrPartnerIncomeOver60kSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantOrPartnerBenefitsSummary.row(request.userAnswers, waypoints, thisPage)
        ).flatten
      )

      val paymentDetails = SummaryListViewModel(
        rows = Seq(
          CurrentlyReceivingChildBenefitSummary.row(request.userAnswers, waypoints, thisPage),
          EldestChildNameSummary.row(request.userAnswers, waypoints, thisPage),
          EldestChildDateOfBirthSummary.row(request.userAnswers, waypoints, thisPage),
          WantToBePaidToExistingAccountSummary.row(request.userAnswers, waypoints, thisPage),
          WantToBePaidSummary.row(request.userAnswers, waypoints, thisPage),
          PaymentFrequencySummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantHasSuitableAccountSummary.row(request.userAnswers, waypoints, thisPage),
          BankAccountDetailsSummary.row(request.userAnswers, waypoints, thisPage)
        ).flatten
      )

      val applicantDetails = SummaryListViewModel(
        rows = Seq(
          ApplicantHasPreviousFamilyNameSummary.row(request.userAnswers, waypoints, thisPage),
          AddApplicantPreviousFamilyNameSummary.checkAnswersRow(request.userAnswers, waypoints, thisPage),
          ApplicantNinoKnownSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantNinoSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantDateOfBirthSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantCurrentAddressSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantLivedAtCurrentAddressOneYearSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantPreviousAddressSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantPhoneNumberSummary.row(request.userAnswers, waypoints, thisPage),
          BestTimeToContactSummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantNationalitySummary.row(request.userAnswers, waypoints, thisPage),
          ApplicantEmploymentStatusSummary.row(request.userAnswers, waypoints, thisPage)
        ).flatten
      )

      val partnerDetails = SummaryListViewModel(
        rows = Seq(
          PartnerNameSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerNinoKnownSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerNinoSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerDateOfBirthSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerNationalitySummary.row(request.userAnswers, waypoints, thisPage),
          PartnerEmploymentStatusSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerEntitledToChildBenefitSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerWaitingForEntitlementDecisionSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerEldestChildNameSummary.row(request.userAnswers, waypoints, thisPage),
          PartnerEldestChildDateOfBirthSummary.row(request.userAnswers, waypoints, thisPage)
        ).flatten
      )

      val childDetails = SummaryListViewModel(
        rows = Seq(
          AddChildSummary.checkAnswersRow(request.userAnswers, waypoints, thisPage)
        ).flatten
      )

      Ok(view(personalDetails, incomeDetails, paymentDetails, applicantDetails, partnerDetails, childDetails))
  }
}
