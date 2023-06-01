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

import connectors.ClaimChildBenefitConnector.{AlreadyInPaymentException, InvalidClaimStateException}
import controllers.actions.{CheckRecentClaimsAction, DataRequiredAction, DataRetrievalAction, IdentifierAction}
import logging.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.ClaimSubmissionService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DeclarationController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       checkRecentClaims: CheckRecentClaimsAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DeclarationView,
                                       claimSubmissionService: ClaimSubmissionService
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>

      Ok(view())
  }

  def onSubmit: Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>

      claimSubmissionService.canSubmit(request).flatMap {
        case true =>
          claimSubmissionService.submit(request).map { _ =>
            Redirect(routes.SubmittedController.onPageLoad)
          }.recover {
            case _: InvalidClaimStateException =>
              logger.warn("Submission for existing claim")
              Redirect(routes.SubmissionFailedExistingClaimController.onPageLoad())
            case _: AlreadyInPaymentException =>
              logger.warn("User already has a claim in payment")
              Redirect(routes.SubmissionFailedAlreadyInPaymentController.onPageLoad())
            case _: Exception =>
              logger.warn("Submission to CBS failed")
              Redirect(routes.PrintController.onPageLoad)
          }

        case false =>
          Future.successful(Redirect(routes.PrintController.onPageLoad))
      }
  }
}
