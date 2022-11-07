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

package controllers.payments

import controllers.AnswerExtractor
import controllers.actions._
import forms.payments.WantToBePaidFormProvider
import models.Income._
import models.RelationshipStatus._
import pages.income.{ApplicantIncomePage, ApplicantOrPartnerIncomePage}
import pages.payments._
import pages.{RelationshipStatusPage, Waypoints}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.payments._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WantToBePaidController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                coupleUnder50kView: WantToBePaidCoupleUnder50kView,
                                                coupleUnder60kView: WantToBePaidCoupleUnder60kView,
                                                coupleOver60kView: WantToBePaidCoupleOver60kView,
                                                singleUnder50kView: WantToBePaidSingleUnder50kView,
                                                singleUnder60kView: WantToBePaidSingleUnder60kView,
                                                singleOver60kView: WantToBePaidSingleOver60kView,
                                                formProvider: WantToBePaidFormProvider,
                                                userDataService: UserDataService
                                              )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(WantToBePaidPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      getAnswer(RelationshipStatusPage) {
        case Married | Cohabiting =>
          getAnswer(ApplicantOrPartnerIncomePage) {
            case BelowLowerThreshold => Ok(coupleUnder50kView(waypoints))
            case BetweenThresholds   => Ok(coupleUnder60kView(preparedForm, waypoints))
            case AboveUpperThreshold => Ok(coupleOver60kView(preparedForm, waypoints))
          }

        case Single | Divorced | Separated | Widowed =>
          getAnswer(ApplicantIncomePage) {
            case BelowLowerThreshold => Ok(singleUnder50kView(waypoints))
            case BetweenThresholds   => Ok(singleUnder60kView(preparedForm, waypoints))
            case AboveUpperThreshold => Ok(singleOver60kView(preparedForm, waypoints))
          }
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors => {
          getAnswerAsync(RelationshipStatusPage) {
            case Married | Cohabiting =>
              getAnswerAsync(ApplicantOrPartnerIncomePage) {
                case BelowLowerThreshold => Future.successful(BadRequest(coupleUnder50kView(waypoints)))
                case BetweenThresholds   => Future.successful(BadRequest(coupleUnder60kView(formWithErrors, waypoints)))
                case AboveUpperThreshold => Future.successful(BadRequest(coupleOver60kView(formWithErrors, waypoints)))
              }

            case Single | Divorced | Separated | Widowed =>
              getAnswerAsync(ApplicantIncomePage) {
                case BelowLowerThreshold => Future.successful(BadRequest(singleUnder50kView(waypoints)))
                case BetweenThresholds   => Future.successful(BadRequest(singleUnder60kView(formWithErrors, waypoints)))
                case AboveUpperThreshold => Future.successful(BadRequest(singleOver60kView(formWithErrors, waypoints)))
              }
          }
        },

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(WantToBePaidPage, value))
            _              <- userDataService.set(updatedAnswers)
          } yield Redirect(WantToBePaidPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
      )
  }
}
