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

package controllers.applicant

import controllers.actions._
import forms.applicant.CountryApplicantReceivedBenefitsFormProvider
import models.Index
import pages.Waypoints
import pages.applicant.CountryApplicantReceivedBenefitsPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.AllCountriesApplicantReceivedBenefits
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.applicant.CountryApplicantReceivedBenefitsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryApplicantReceivedBenefitsController @Inject()(
                                                            override val messagesApi: MessagesApi,
                                                            userDataService: UserDataService,
                                                            identify: IdentifierAction,
                                                            checkRecentClaims: CheckRecentClaimsAction,
                                                            getData: DataRetrievalAction,
                                                            requireData: DataRequiredAction,
                                                            formProvider: CountryApplicantReceivedBenefitsFormProvider,
                                                            val controllerComponents: MessagesControllerComponents,
                                                            view: CountryApplicantReceivedBenefitsView
                                                          )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  def onPageLoad(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>

      val countries = request.userAnswers.get(AllCountriesApplicantReceivedBenefits).getOrElse(Nil)

      val form = formProvider(index, countries)

      val preparedForm = request.userAnswers.get(CountryApplicantReceivedBenefitsPage(index)) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints, index))
  }

  def onSubmit(waypoints: Waypoints, index: Index): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>

      val countries = request.userAnswers.get(AllCountriesApplicantReceivedBenefits).getOrElse(Nil)

      val form = formProvider(index, countries)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints, index))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CountryApplicantReceivedBenefitsPage(index), value))
            _ <- userDataService.set(updatedAnswers)
          } yield Redirect(CountryApplicantReceivedBenefitsPage(index).navigate(waypoints, request.userAnswers, updatedAnswers).route)
      )
  }
}
