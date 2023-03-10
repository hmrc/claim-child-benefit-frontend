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

package controllers.payments

import config.FeatureFlags
import controllers.AnswerExtractor
import controllers.actions._
import forms.payments.BankAccountDetailsFormProvider
import models.{BankAccountDetails, BankAccountHolder, ReputationResponseEnum, VerifyBankDetailsResponseModel}
import pages.Waypoints
import pages.payments.{BankAccountDetailsPage, BankAccountHolderPage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.UserDataService
import services.BarsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.payments.BankAccountDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BankAccountDetailsController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      userDataService: UserDataService,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      requireData: DataRequiredAction,
                                      formProvider: BankAccountDetailsFormProvider,
                                      val controllerComponents: MessagesControllerComponents,
                                      view: BankAccountDetailsView,
                                      barsService: BarsService,
                                      featureFlags: FeatureFlags
                                     )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(BankAccountHolderPage) {
        accountHolder =>

          val maybeGuidance = guidance(accountHolder)
          val preparedForm = request.userAnswers.get(BankAccountDetailsPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, maybeGuidance))
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(BankAccountHolderPage) {
        accountHolder =>

          val maybeGuidance = guidance(accountHolder)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, maybeGuidance))),

            value => {

              def saveAndRedirect: Future[Result] =
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(BankAccountDetailsPage, value))
                  _ <- userDataService.set(updatedAnswers)
                } yield Redirect(BankAccountDetailsPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)

              if (featureFlags.verifyBankDetails) {
                barsService.verifyBankDetails(value).flatMap {
                  getBarsError(value, _).map { error =>
                    Future.successful(BadRequest(view(form.fill(value).withError(error), waypoints, maybeGuidance)))
                  }.getOrElse(saveAndRedirect)
                }
              } else {
                saveAndRedirect
              }
            }
        )
      }
  }

  private def getBarsError(submittedDetails: BankAccountDetails, validationResult: Option[VerifyBankDetailsResponseModel]): Option[FormError] =
    validationResult.flatMap { result =>
      if (result.sortCodeIsPresentOnEISCD == ReputationResponseEnum.No) {
        Some(FormError("sortCode", "bankAccountDetails.error.sortCode.doesNotExist"))
      } else if (result.accountNumberIsWellFormatted == ReputationResponseEnum.No) {
        Some(FormError("sortCode", "bankAccountDetails.error.sortCode.failedModulusCheck"))
      } else if (result.nonStandardAccountDetailsRequiredForBacs == ReputationResponseEnum.Yes) {
        Some(FormError("sortCode", "bankAccountDetails.error.sortCode.nonStandardDetailsRequired"))
      } else if (result.sortCodeSupportsDirectCredit == ReputationResponseEnum.No) {
        Some(FormError("sortCode", "bankAccountDetails.error.sortCode.doesNotSupportDirectCredit"))
      } else {
        None
      }
    }

  private def guidance(accountHolder: BankAccountHolder)(implicit messages: Messages): Option[String] =
    accountHolder match {
      case BankAccountHolder.Applicant => None
      case BankAccountHolder.JointNames => Some(messages("bankAccountDetails.jointNames.p1"))
      case BankAccountHolder.SomeoneElse => Some(messages("bankAccountDetails.someoneElse.p1"))
    }
}
