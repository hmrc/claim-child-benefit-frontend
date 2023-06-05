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
import forms.partner.PartnerEldestChildNameFormProvider
import pages.Waypoints
import pages.partner.{PartnerEldestChildNamePage, PartnerNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.PartnerEldestChildNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PartnerEldestChildNameController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      userDataService: UserDataService,
                                      identify: IdentifierAction,
                                      checkRecentClaims: CheckRecentClaimsAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: PartnerEldestChildNameFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: PartnerEldestChildNameView
                                     )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswer(PartnerNamePage) {
        partnerName =>

          val form = formProvider(partnerName.firstName)

          val preparedForm = request.userAnswers.get(PartnerEldestChildNamePage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, partnerName.firstName))
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(PartnerNamePage) {
        partnerName =>

          val form = formProvider(partnerName.firstName)

          form.bindFromRequest().fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, waypoints, partnerName.firstName)))
            },

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(PartnerEldestChildNamePage, value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(PartnerEldestChildNamePage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}