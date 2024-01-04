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
import forms.partner.AddCountryPartnerWorkedFormProvider
import pages.Waypoints
import pages.partner.{AddCountryPartnerWorkedPage, PartnerNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.partner.AddCountryPartnerWorkedSummary
import views.html.partner.AddCountryPartnerWorkedView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddCountryPartnerWorkedController @Inject()(
                                                 override val messagesApi: MessagesApi,
                                                 userDataService: UserDataService,
                                                 identify: IdentifierAction,
                                                 checkRecentClaims: CheckRecentClaimsAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: AddCountryPartnerWorkedFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: AddCountryPartnerWorkedView
                                               )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswer(PartnerNamePage) {
        partnerName =>

          val form = formProvider(partnerName.firstName)
          val nationalities = AddCountryPartnerWorkedSummary.rows(request.userAnswers, waypoints, AddCountryPartnerWorkedPage())

          Ok(view(form, waypoints, nationalities, partnerName.firstName))
      }
  }
  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(PartnerNamePage) {
        partnerName =>

          val form = formProvider(partnerName.firstName)

          form.bindFromRequest().fold(
            formWithErrors => {
              val nationalities = AddCountryPartnerWorkedSummary.rows(request.userAnswers, waypoints, AddCountryPartnerWorkedPage())

              Future.successful(BadRequest(view(formWithErrors, waypoints, nationalities, partnerName.firstName)))
            },

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(AddCountryPartnerWorkedPage(), value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(AddCountryPartnerWorkedPage().navigate(waypoints, request.userAnswers, updatedAnswers).route)
          )
      }
  }
}
