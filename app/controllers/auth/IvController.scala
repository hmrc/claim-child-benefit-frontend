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

package controllers.auth

import connectors.IvConnector
import models.IvResult._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.auth._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IvController @Inject()(
                              val controllerComponents: MessagesControllerComponents,
                              ivConnector: IvConnector,
                              ivIncompleteView: IvIncompleteView,
                              ivFailedMatchingView: IvFailedMatchingView,
                              ivFailedIdentityVerificationView: IvFailedIdentityVerificationView,
                              ivInsufficientEvidenceView: IvInsufficientEvidenceView,
                              ivUserAbortedView: IvUserAbortedView,
                              ivLockedOutView: IvLockedOutView,
                              ivPreconditionFailedView: IvPreconditionFailedView,
                              ivTechnicalIssueView: IvTechnicalIssueView,
                              ivTimeoutView: IvTimeoutView,
                              ivErrorView: IvErrorView
                            )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def handleIvFailure(continueUrl: String, journeyId: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    journeyId.map { id =>
      ivConnector.getJourneyStatus(id).map {
        case Success                    => Redirect(continueUrl)
        case Incomplete                 => Redirect(routes.IvController.ivIncomplete)
        case FailedMatching             => Redirect(routes.IvController.ivFailedMatching(continueUrl))
        case FailedIdentityVerification => Redirect(routes.IvController.ivFailedIdentityVerification(continueUrl))
        case InsufficientEvidence       => Redirect(routes.IvController.ivInsufficientEvidence)
        case UserAborted                => Redirect(routes.IvController.ivUserAborted(continueUrl))
        case LockedOut                  => Redirect(routes.IvController.ivLockedOut)
        case IvPreconditionFailed       => Redirect(routes.IvController.ivPreconditionFailed)
        case TechnicalIssue             => Redirect(routes.IvController.ivTechnicalIssue)
        case Timeout                    => Redirect(routes.IvController.ivTimeout)
      }
    }.getOrElse(
      Future.successful(Redirect(routes.IvController.ivError))
    )
  }

  def ivIncomplete: Action[AnyContent] = Action { implicit request =>
    Ok(ivIncompleteView())
  }

  def ivFailedMatching(continueUrl: String): Action[AnyContent] = Action { implicit request =>
    Ok(ivFailedMatchingView(continueUrl))
  }

  def ivFailedIdentityVerification(continueUrl: String): Action[AnyContent] = Action { implicit request =>
    Ok(ivFailedIdentityVerificationView(continueUrl))
  }

  def ivInsufficientEvidence: Action[AnyContent] = Action { implicit request =>
    Ok(ivInsufficientEvidenceView())
  }

  def ivUserAborted(continueUrl: String): Action[AnyContent] = Action { implicit request =>
    Ok(ivUserAbortedView(continueUrl))
  }

  def ivLockedOut: Action[AnyContent] = Action { implicit request =>
    Ok(ivLockedOutView())
  }

  def ivPreconditionFailed: Action[AnyContent] = Action { implicit request =>
    Ok(ivPreconditionFailedView())
  }

  def ivTechnicalIssue: Action[AnyContent] = Action { implicit request =>
    Ok(ivTechnicalIssueView())
  }

  def ivTimeout: Action[AnyContent] = Action { implicit request =>
    Ok(ivTimeoutView())
  }

  def ivError: Action[AnyContent] = Action { implicit request =>
    Ok(ivErrorView())
  }
}
