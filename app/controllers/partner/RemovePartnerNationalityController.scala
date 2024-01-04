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

package controllers.partner

import controllers.AnswerExtractor
import controllers.actions._
import forms.partner.RemovePartnerNationalityFormProvider
import models.Index
import pages.Waypoints
import pages.partner.{PartnerNamePage, PartnerNationalityPage, RemovePartnerNationalityPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.partner.RemovePartnerNationalityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemovePartnerNationalityController @Inject()(
                                                      override val messagesApi: MessagesApi,
                                                      userDataService: UserDataService,
                                                      identify: IdentifierAction,
                                                      checkRecentClaims: CheckRecentClaimsAction,
                                                      getData: DataRetrievalAction,
                                                      requireData: DataRequiredAction,
                                                      formProvider: RemovePartnerNationalityFormProvider,
                                                      val controllerComponents: MessagesControllerComponents,
                                                      view: RemovePartnerNationalityView
                                                    )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswers(PartnerNamePage, PartnerNationalityPage(index)) {
        case (partnerName, nationality) =>

          val form = formProvider(partnerName.firstName, nationality.message)

          Ok(view(form, waypoints, index, partnerName.firstName, nationality.message))
      }
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getAnswersAsync(PartnerNamePage, PartnerNationalityPage(index)) {
        case(partnerName, nationality) =>

          val form = formProvider(partnerName.firstName, nationality.message)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, index, partnerName.firstName, nationality.message))),

            value =>
              if (value) {
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.remove(PartnerNationalityPage(index)))
                  _ <- userDataService.set(updatedAnswers)
                } yield Redirect(RemovePartnerNationalityPage(index).navigate(waypoints, request.userAnswers, updatedAnswers).route)
              } else {
                Future.successful(Redirect(RemovePartnerNationalityPage(index).navigate(waypoints, request.userAnswers, request.userAnswers).route))
              }
          )
      }
  }
}