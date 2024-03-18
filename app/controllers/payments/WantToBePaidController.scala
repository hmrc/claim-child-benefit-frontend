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

import java.time.Clock
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
                                        coupleUnderLowerThresholdUnderLowerThreshold: WantToBePaidCoupleUnderLowerThresholdUnderLowerThresholdView,
                                        coupleUnderLowerThresholdUnderUpperThreshold: WantToBePaidCoupleUnderLowerThresholdUnderUpperThresholdView,
                                        coupleUnderLowerThresholdOverUpperThreshold: WantToBePaidCoupleUnderLowerThresholdOverUpperThresholdView,
                                        coupleUnderUpperThresholdUnderLowerThreshold: WantToBePaidCoupleUnderUpperThresholdUnderLowerThresholdView,
                                        coupleUnderUpperThresholdUnderUpperThreshold: WantToBePaidCoupleUnderUpperThresholdUnderUpperThresholdView,
                                        coupleUnderUpperThresholdOverUpperThreshold: WantToBePaidCoupleUnderUpperThresholdOverUpperThresholdView,
                                        coupleOverUpperThresholdUnderLowerThreshold: WantToBePaidCoupleOverUpperThresholdUnderLowerThresholdView,
                                        coupleOverUpperThresholdUnderUpperThreshold: WantToBePaidCoupleOverUpperThresholdUnderUpperThresholdView,
                                        coupleOverUpperThresholdOverUpperThreshold: WantToBePaidCoupleOverUpperThresholdOverUpperThresholdView,
                                        singleUnderLowerThreshold: WantToBePaidSingleUnderLowerThresholdView,
                                        singleUnderUpperThreshold: WantToBePaidSingleUnderUpperThresholdView,
                                        singleOverUpperThreshold: WantToBePaidSingleOverUpperThresholdView,
                                        formProvider: WantToBePaidFormProvider,
                                        userDataService: UserDataService
                                      )(implicit ec: ExecutionContext, clock: Clock)
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
            status(coupleUnderLowerThresholdUnderLowerThreshold(waypoints))

          case (BelowLowerThreshold, BetweenThresholds) =>
            status(coupleUnderLowerThresholdUnderUpperThreshold(preparedForm, waypoints))

          case (BelowLowerThreshold, AboveUpperThreshold) =>
            status(coupleUnderLowerThresholdOverUpperThreshold(preparedForm, waypoints))

          case (BetweenThresholds, BelowLowerThreshold) =>
            status(coupleUnderUpperThresholdUnderLowerThreshold(preparedForm, waypoints))

          case (BetweenThresholds, BetweenThresholds) =>
            status(coupleUnderUpperThresholdUnderUpperThreshold(preparedForm, waypoints))

          case (BetweenThresholds, AboveUpperThreshold) =>
            status(coupleUnderUpperThresholdOverUpperThreshold(preparedForm, waypoints))

          case (AboveUpperThreshold, BelowLowerThreshold) =>
            status(coupleOverUpperThresholdUnderLowerThreshold(preparedForm, waypoints))

          case (AboveUpperThreshold, BetweenThresholds) =>
            status(coupleOverUpperThresholdUnderUpperThreshold(preparedForm, waypoints))

          case (AboveUpperThreshold, AboveUpperThreshold) =>
            status(coupleOverUpperThresholdOverUpperThreshold(preparedForm, waypoints))
        }

      case Single | Separated | Widowed | Divorced =>
        getAnswer(ApplicantIncomePage) {
          case BelowLowerThreshold =>
            status(singleUnderLowerThreshold(waypoints))

          case BetweenThresholds =>
            status(singleUnderUpperThreshold(preparedForm, waypoints))

          case AboveUpperThreshold =>
            status(singleOverUpperThreshold(preparedForm, waypoints))
        }
    }
}
