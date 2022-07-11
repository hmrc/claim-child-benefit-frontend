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

package controllers.child

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.Index
import pages.Waypoints
import pages.child.CheckChildDetailsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.child._
import viewmodels.govuk.summarylist._
import views.html.child.CheckChildDetailsView

class CheckChildDetailsController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: CheckChildDetailsView
                                          ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val list = SummaryListViewModel(
        rows = Seq(
          ChildNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ChildHasPreviousNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ChildNameChangedByDeedPollSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          AddChildPreviousNameSummary.checkAnswersRow(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ChildBiologicalSexSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ChildDateOfBirthSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ChildBirthRegistrationCountrySummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ChildBirthCertificateSystemNumberSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ChildScottishBirthCertificateDetailsSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          ApplicantRelationshipToChildSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          AnyoneClaimedForChildBeforeSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          PreviousClaimantNameSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          PreviousClaimantAddressSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          AdoptingThroughLocalAuthoritySummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index)),
          IncludedDocumentsSummary.row(request.userAnswers, index, waypoints, CheckChildDetailsPage(index))
        ).flatten
      )

      Ok(view(list, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(CheckChildDetailsPage(index).navigate(waypoints, request.userAnswers, request.userAnswers).route)
  }
}
