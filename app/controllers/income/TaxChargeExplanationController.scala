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

package controllers.income

import controllers.AnswerExtractor
import controllers.actions._
import models.Income._
import models.RelationshipStatus._
import pages.{RelationshipStatusPage, Waypoints}
import pages.income._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.income._

import javax.inject.Inject

class TaxChargeExplanationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       coupleUnder50kView: TaxChargeExplanationCoupleUnder50kView,
                                       coupleUnder60kView: TaxChargeExplanationCoupleUnder60kView,
                                       coupleOver60kView: TaxChargeExplanationCoupleOver60kView,
                                       singleUnder50kView: TaxChargeExplanationSingleUnder50kView,
                                       singleUnder60kView: TaxChargeExplanationSingleUnder60kView,
                                       singleOver60kView: TaxChargeExplanationSingleOver60kView
                                     ) extends FrontendBaseController with I18nSupport with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(RelationshipStatusPage) {
        case Married | Cohabiting =>
          getAnswer(ApplicantOrPartnerIncomePage) {
            case BelowLowerThreshold => Ok(coupleUnder50kView(waypoints))
            case BetweenThresholds   => Ok(coupleUnder60kView(waypoints))
            case AboveUpperThreshold => Ok(coupleOver60kView(waypoints))
          }

        case Single | Divorced | Separated | Widowed =>
          getAnswer(ApplicantIncomePage) {
            case BelowLowerThreshold => Ok(singleUnder50kView(waypoints))
            case BetweenThresholds   => Ok(singleUnder60kView(waypoints))
            case AboveUpperThreshold => Ok(singleOver60kView(waypoints))
          }
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(TaxChargeExplanationPage.navigate(waypoints, request.userAnswers, request.userAnswers).route)
  }
}
