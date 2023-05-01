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

import com.google.inject.Inject
import controllers.AnswerExtractor
import controllers.actions.{CheckRecentClaimsAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Index
import pages.Waypoints
import pages.child.{CheckChildDetailsPage, ChildNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.child._
import viewmodels.govuk.summarylist._
import views.html.child.CheckChildDetailsView

class CheckChildDetailsController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            checkRecentClaims: CheckRecentClaimsAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckChildDetailsView
                                          ) extends FrontendBaseController with I18nSupport with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswer(ChildNamePage(index)) {
        childName =>

          val list = SummaryListViewModel(
            rows = Seq(
              ChildNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildHasPreviousNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildNameChangedByDeedPollSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              AddChildPreviousNameSummary.checkAnswersRow(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildBiologicalSexSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildDateOfBirthSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildBirthRegistrationCountrySummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              BirthCertificateHasSystemNumberSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildBirthCertificateSystemNumberSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ScottishBirthCertificateHasNumbersSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildScottishBirthCertificateDetailsSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              BirthCertificateHasNorthernIrishNumberSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildNorthernIrishBirthCertificateNumberSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              AdoptingThroughLocalAuthoritySummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ApplicantRelationshipToChildSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              AnyoneClaimedForChildBeforeSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousClaimantNameKnownSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousClaimantNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousClaimantAddressKnownSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousClaimantAddressInUkSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousClaimantUkAddressSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousClaimantInternationalAddressSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildLivesWithApplicantSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              GuardianNameKnownSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              GuardianNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              GuardianAddressKnownSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              GuardianAddressInUkSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              GuardianUkAddressSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              GuardianInternationalAddressSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              ChildLivedWithAnyoneElseSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianNameKnownSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianAddressKnownSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianAddressInUkSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianUkAddressSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianInternationalAddressSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianPhoneNumberKnownSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              PreviousGuardianPhoneNumberSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
              DateChildStartedLivingWithApplicantSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
            ).flatten
          )

          Ok(view(list, waypoints, index, childName))
      }
  }
  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      Redirect(CheckChildDetailsPage(index).navigate(waypoints, request.userAnswers, request.userAnswers).route)
  }
}
