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

import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserDataService
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, ConfidenceLevel, NoActiveSession}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.IndexView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IndexController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identify: IdentifierAction,
                                 view: IndexView,
                                 userDataService: UserDataService,
                                 val authConnector: AuthConnector
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with AuthorisedFunctions {

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(view())
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised().retrieve(
      Retrievals.affinityGroup and
        Retrievals.confidenceLevel
    ) {
      case Some(Individual) ~ confidenceLevel if confidenceLevel < ConfidenceLevel.L250 =>
        Future.successful(Redirect(routes.NeedToUpliftIvController.onPageLoad()))

      case _ =>
        goToNextPage
    }.recoverWith {
      case _: NoActiveSession =>
        goToNextPage
    }
  }

  private def goToNextPage: Future[Result] =
    Future.successful(Redirect(routes.RecentlyClaimedController.onPageLoad()))

  def startAgain: Action[AnyContent] = identify.async { implicit request =>
    userDataService.clear(request.userId).map {
      _ =>
        Redirect(routes.IndexController.onPageLoad)
    }
  }
}
