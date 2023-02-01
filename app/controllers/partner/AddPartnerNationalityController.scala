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
import forms.partner.AddPartnerNationalityFormProvider
import pages.Waypoints
import pages.partner.{AddPartnerNationalityPage, PartnerNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.partner.AddPartnerNationalitySummary
import views.html.partner.AddPartnerNationalityView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddPartnerNationalityController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   userDataService: UserDataService,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   formProvider: AddPartnerNationalityFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: AddPartnerNationalityView
                                                 )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(PartnerNamePage) {
        partnerName =>

          val form = formProvider(partnerName.firstName)
          val nationalities = AddPartnerNationalitySummary.rows(request.userAnswers, waypoints, AddPartnerNationalityPage())

          Ok(view(form, waypoints, nationalities, partnerName.firstName))
      }
  }
  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(PartnerNamePage) {
        partnerName =>

          val form = formProvider(partnerName.firstName)

          form.bindFromRequest().fold(
            formWithErrors => {
              val nationalities = AddPartnerNationalitySummary.rows(request.userAnswers, waypoints, AddPartnerNationalityPage())

              Future.successful(BadRequest(view(formWithErrors, waypoints, nationalities, partnerName.firstName)))
            },

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddPartnerNationalityPage(), value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(AddPartnerNationalityPage().navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}
