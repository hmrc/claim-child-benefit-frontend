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
                                        coupleUnder50kUnder50k: WantToBePaidCoupleUnder50kUnder50kView,
                                        coupleUnder50kUnder60k: WantToBePaidCoupleUnder50kUnder60kView,
                                        coupleUnder50kOver60k: WantToBePaidCoupleUnder50kOver60kView,
                                        coupleUnder60kUnder50k: WantToBePaidCoupleUnder60kUnder50kView,
                                        coupleUnder60kUnder60k: WantToBePaidCoupleUnder60kUnder60kView,
                                        coupleUnder60kOver60k: WantToBePaidCoupleUnder60kOver60kView,
                                        coupleOver60kUnder50k: WantToBePaidCoupleOver60kUnder50kView,
                                        coupleOver60kUnder60k: WantToBePaidCoupleOver60kUnder60kView,
                                        coupleOver60kOver60k: WantToBePaidCoupleOver60kOver60kView,
                                        singleUnder50k: WantToBePaidSingleUnder50kView,
                                        singleUnder60k: WantToBePaidSingleUnder60kView,
                                        singleOver60k: WantToBePaidSingleOver60kView,
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
            status(coupleUnder50kUnder50k(waypoints))

          case (BelowLowerThreshold, BetweenThresholds) =>
            status(coupleUnder50kUnder60k(preparedForm, waypoints))

          case (BelowLowerThreshold, AboveUpperThreshold) =>
            status(coupleUnder50kOver60k(preparedForm, waypoints))

          case (BetweenThresholds, BelowLowerThreshold) =>
            status(coupleUnder60kUnder50k(preparedForm, waypoints))

          case (BetweenThresholds, BetweenThresholds) =>
            status(coupleUnder60kUnder60k(preparedForm, waypoints))

          case (BetweenThresholds, AboveUpperThreshold) =>
            status(coupleUnder60kOver60k(preparedForm, waypoints))

          case (AboveUpperThreshold, BelowLowerThreshold) =>
            status(coupleOver60kUnder50k(preparedForm, waypoints))

          case (AboveUpperThreshold, BetweenThresholds) =>
            status(coupleOver60kUnder60k(preparedForm, waypoints))

          case (AboveUpperThreshold, AboveUpperThreshold) =>
            status(coupleOver60kOver60k(preparedForm, waypoints))
        }

      case Single | Separated | Widowed | Divorced =>
        getAnswer(ApplicantIncomePage) {
          case BelowLowerThreshold =>
            status(singleUnder50k(waypoints))

          case BetweenThresholds =>
            status(singleUnder60k(preparedForm, waypoints))

          case AboveUpperThreshold =>
            status(singleOver60k(preparedForm, waypoints))
        }
    }
}
