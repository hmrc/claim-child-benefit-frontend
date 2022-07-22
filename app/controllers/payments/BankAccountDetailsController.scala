/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers.payments

import controllers.actions._
import forms.payments.BankAccountDetailsFormProvider
import models.{ReputationResponseEnum, ValidateBankDetailsResponseModel}
import pages.Waypoints
import pages.payments.BankAccountDetailsPage
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.BarsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.payments.BankAccountDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BankAccountDetailsController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      sessionRepository: SessionRepository,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: BankAccountDetailsFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: BankAccountDetailsView,
                                      barsService: BarsService
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(BankAccountDetailsPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, waypoints))
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, waypoints))),

        value =>
          barsService.validateBankDetails(value).flatMap {
            getBarsError(_).map { error =>
              Future.successful(BadRequest(view(form.fill(value).withError(error), waypoints)))
            }.getOrElse {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(BankAccountDetailsPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(BankAccountDetailsPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
            }
          }
      )
  }

  private def getBarsError(validationResult: Option[ValidateBankDetailsResponseModel]): Option[FormError] =
    validationResult.flatMap { result =>
      if (result.sortCodeIsPresentOnEISCD == ReputationResponseEnum.No) {
        Some(FormError("sortCode", "bankAccountDetails.error.sortCode.doesNotExist"))
      } else if (result.accountNumberIsWellFormatted == ReputationResponseEnum.No) {
        Some(FormError("sortCode", "bankAccountDetails.error.sortCode.failedModulusCheck"))
      } else {
        None
      }
    }
}
