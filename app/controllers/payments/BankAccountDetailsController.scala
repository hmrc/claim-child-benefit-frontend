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
import forms.payments.{BankAccountDetailsFormModel, BankAccountDetailsFormProvider}
import models.{BankAccountHolder, ReputationResponseEnum, VerifyBankDetailsResponseModel}
import pages.Waypoints
import pages.payments.{BankAccountDetailsPage, BankAccountHolderPage}
import play.api.data.FormError
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{BarsService, UserDataService}
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

  private val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(BankAccountHolderPage) {
        accountHolder =>

          val maybeGuidance = guidance(accountHolder)
          val preparedForm = request.userAnswers.get(BankAccountDetailsPage) match {
            case None => form
            case Some(value) => form.fill(BankAccountDetailsFormModel(value, None))
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
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(BankAccountDetailsPage, value.details))
                  _ <- userDataService.set(updatedAnswers)
                } yield Redirect(BankAccountDetailsPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)

              if (featureFlags.verifyBankDetails) {
                barsService.verifyBankDetails(value.details).flatMap {
                  getBarsError(_).map { barsError =>

                    if (barsError.softError && value.softError.getOrElse(false)) {
                      saveAndRedirect
                    } else {
                      val updatedValue = value.copy(softError = Some(barsError.softError))
                      Future.successful(BadRequest(view(form.fill(updatedValue).withError(barsError.error), waypoints, maybeGuidance)))
                    }
                  }.getOrElse(saveAndRedirect)
                }
              } else {
                saveAndRedirect
              }
            }
        )
      }
  }

  private def getBarsError(validationResult: Option[VerifyBankDetailsResponseModel]): Option[BarsError] =
    validationResult.flatMap { result =>
      if (result.sortCodeIsPresentOnEISCD == ReputationResponseEnum.No) {
        Some(BarsError.SortCodeNotPresent)
      } else if (result.accountNumberIsWellFormatted == ReputationResponseEnum.No) {
        Some(BarsError.AccountNumberNotWellFormed)
      } else if (result.nonStandardAccountDetailsRequiredForBacs == ReputationResponseEnum.Yes) {
        Some(BarsError.NonStandardDetailsRequired)
      } else if (result.sortCodeSupportsDirectCredit == ReputationResponseEnum.No) {
        Some(BarsError.AccountDoesNotSupportDirectCredit)
      } else if (result.accountExists == ReputationResponseEnum.No) {
        Some(BarsError.AccountDoesNotExist)
      } else if (result.nameMatches == ReputationResponseEnum.No) {
        Some(BarsError.NameDoesNotMatch)
      }  else {
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

sealed trait BarsError {
  val softError: Boolean
  val error: FormError
}

object BarsError {
  object SortCodeNotPresent extends BarsError {
    override val softError: Boolean = false
    override val error: FormError = FormError("sortCode", "bankAccountDetails.error.sortCode.doesNotExist")
  }

  object AccountNumberNotWellFormed extends BarsError {
    override val softError: Boolean = false
    override val error: FormError = FormError("sortCode", "bankAccountDetails.error.sortCode.failedModulusCheck")
  }

  object NonStandardDetailsRequired extends BarsError {
    override val softError: Boolean = false
    override val error: FormError = FormError("sortCode", "bankAccountDetails.error.sortCode.nonStandardDetailsRequired")
  }

  object AccountDoesNotSupportDirectCredit extends BarsError {
    override val softError: Boolean = false
    override val error: FormError = FormError("sortCode", "bankAccountDetails.error.sortCode.doesNotSupportDirectCredit")
  }

  object AccountDoesNotExist extends BarsError {
    override val softError: Boolean = false
    override val error: FormError = FormError("accountNumber", "bankAccountDetails.error.accountNumber.accountDoesNotExist")
  }

  object NameDoesNotMatch extends BarsError {
    override val softError: Boolean = true
    override val error: FormError = FormError("firstName", "bankAccountDetails.error.firstName.nameDoesNotMatch")
  }
}
