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

package controllers.partner

import controllers.AnswerExtractor
import controllers.actions._
import forms.partner.PartnerEldestChildDateOfBirthFormProvider
import pages.Waypoints
import pages.partner.{PartnerEldestChildDateOfBirthPage, PartnerEldestChildNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.PartnerEldestChildDateOfBirthView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerEldestChildDateOfBirthController @Inject()(
                                        override val messagesApi: MessagesApi,
                                        userDataService: UserDataService,
                                        identify: IdentifierAction,
                                        checkRecentClaims: CheckRecentClaimsAction,
                                        getData: DataRetrievalAction,
                                        requireData: DataRequiredAction,
                                        formProvider: PartnerEldestChildDateOfBirthFormProvider,
                                        val controllerComponents: MessagesControllerComponents,
                                        view: PartnerEldestChildDateOfBirthView
                                      )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswer(PartnerEldestChildNamePage) {
        eldestChildName =>

          val form = formProvider(eldestChildName.firstName)

          val preparedForm = request.userAnswers.get(PartnerEldestChildDateOfBirthPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, eldestChildName.firstName))
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(PartnerEldestChildNamePage) {
        eldestChildName =>

          val form = formProvider(eldestChildName.firstName)

            form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, eldestChildName.firstName))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerEldestChildDateOfBirthPage, value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(PartnerEldestChildDateOfBirthPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}