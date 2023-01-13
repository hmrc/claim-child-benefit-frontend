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

package controllers

import controllers.actions._
import models.TaskListSectionChange._
import pages.Waypoints
import pages.RelationshipStatusChangesTaskListPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html._

import javax.inject.Inject

class RelationshipStatusChangesTaskListController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              paymentRemovedView: RelationshipStatusChangesTaskListPaymentRemovedView,
                                              partnerRemovedView: RelationshipStatusChangesTaskListPartnerRemovedView,
                                              partnerRequiredView: RelationshipStatusChangesTaskListPartnerRequiredView,
                                              paymentRemovedPartnerRemovedView: RelationshipStatusChangesTaskListPaymentRemovedPartnerRemovedView,
                                              paymentRemovedPartnerRequiredView: RelationshipStatusChangesTaskListPaymentRemovedPartnerRequiredView
                                            )
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(RelationshipStatusChangesTaskListPage) {
        sections =>

          val paymentRemoved = Set(PaymentDetailsRemoved)
          val partnerRemoved = Set(PartnerDetailsRemoved)
          val partnerRequired = Set(PartnerDetailsRequired)
          val paymentRemovedPartnerRemoved = Set(PaymentDetailsRemoved, PartnerDetailsRemoved)
          val paymentRemovedPartnerRequired = Set(PaymentDetailsRemoved, PartnerDetailsRequired)

          sections match {
            case x if x == paymentRemoved => Ok(paymentRemovedView(waypoints))
            case x if x == partnerRemoved => Ok(partnerRemovedView(waypoints))
            case x if x == partnerRequired => Ok(partnerRequiredView(waypoints))
            case x if x == paymentRemovedPartnerRemoved => Ok(paymentRemovedPartnerRemovedView(waypoints))
            case x if x == paymentRemovedPartnerRequired => Ok(paymentRemovedPartnerRequiredView(waypoints))
            case _ => Redirect(routes.JourneyRecoveryController.onPageLoad())
          }
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Redirect(RelationshipStatusChangesTaskListPage.navigate(waypoints, request.userAnswers, request.userAnswers).route)
  }
}
