/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.AnswerExtractor
import controllers.actions._
import forms.payments.WantToBePaidFormProvider
import models.Income._
import models.RelationshipStatus._
import models.requests.DataRequest
import pages.Waypoints
import pages.partner.RelationshipStatusPage
import pages.payments._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.payments._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//scalastyle:off
class WantToBePaidController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        identify: IdentifierAction,
                                        checkRecentClaims: CheckRecentClaimsAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        val controllerComponents: MessagesControllerComponents,
                                        coupleUnder60kUnder60k: WantToBePaidCoupleUnder60kUnder60kView,
                                        coupleUnder60kUnder80k: WantToBePaidCoupleUnder60kUnder80kView,
                                        coupleUnder60kOver80k: WantToBePaidCoupleUnder60kOver80kView,
                                        coupleUnder80kUnder60k: WantToBePaidCoupleUnder80kUnder60kView,
                                        coupleUnder80kUnder80k: WantToBePaidCoupleUnder80kUnder80kView,
                                        coupleUnder80kOver80k: WantToBePaidCoupleUnder80kOver80kView,
                                        coupleOver80kUnder60k: WantToBePaidCoupleOver80kUnder60kView,
                                        coupleOver80kUnder80k: WantToBePaidCoupleOver80kUnder80kView,
                                        coupleOver80kOver80k: WantToBePaidCoupleOver80kOver80kView,
                                        singleUnder60k: WantToBePaidSingleUnder60kView,
                                        singleUnder80k: WantToBePaidSingleUnder80kView,
                                        singleOver80k: WantToBePaidSingleOver80kView,
                                        formProvider: WantToBePaidFormProvider,
                                        userDataService: UserDataService
                                      )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  private val form = formProvider()
  
  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(WantToBePaidPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      showView(preparedForm, waypoints, Ok)
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(showView(formWithErrors, waypoints, BadRequest)),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WantToBePaidPage, value))
            _              <- userDataService.set(updatedAnswers)
          } yield Redirect(WantToBePaidPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
      )
  }

  private def showView(preparedForm: Form[Boolean], waypoints: Waypoints, status: Status)
                      (implicit request: DataRequest[AnyContent]): Result =
    getAnswer(RelationshipStatusPage) {
      case Married | Cohabiting =>
        getAnswers(ApplicantIncomePage, PartnerIncomePage) {
          case (BelowLowerThreshold, BelowLowerThreshold) =>
            status(coupleUnder60kUnder60k(waypoints))

          case (BelowLowerThreshold, BetweenThresholds) =>
            status(coupleUnder60kUnder80k(preparedForm, waypoints))

          case (BelowLowerThreshold, AboveUpperThreshold) =>
            status(coupleUnder60kOver80k(preparedForm, waypoints))

          case (BetweenThresholds, BelowLowerThreshold) =>
            status(coupleUnder80kUnder60k(preparedForm, waypoints))

          case (BetweenThresholds, BetweenThresholds) =>
            status(coupleUnder80kUnder80k(preparedForm, waypoints))

          case (BetweenThresholds, AboveUpperThreshold) =>
            status(coupleUnder80kOver80k(preparedForm, waypoints))

          case (AboveUpperThreshold, BelowLowerThreshold) =>
            status(coupleOver80kUnder60k(preparedForm, waypoints))

          case (AboveUpperThreshold, BetweenThresholds) =>
            status(coupleOver80kUnder80k(preparedForm, waypoints))

          case (AboveUpperThreshold, AboveUpperThreshold) =>
            status(coupleOver80kOver80k(preparedForm, waypoints))
        }

      case Single | Separated | Widowed | Divorced =>
        getAnswer(ApplicantIncomePage) {
          case BelowLowerThreshold =>
            status(singleUnder60k(waypoints))

          case BetweenThresholds =>
            status(singleUnder80k(preparedForm, waypoints))

          case AboveUpperThreshold =>
            status(singleOver80k(preparedForm, waypoints))
        }
    }
}
