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

import controllers.AnswerExtractor
import controllers.actions._
import forms.payments.BuildingSocietyDetailsFormProvider
import models.BankAccountHolder
import pages.Waypoints
import pages.payments.{BankAccountHolderPage, BuildingSocietyDetailsPage}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserDataService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.payments.BuildingSocietyDetailsView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BuildingSocietyDetailsController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              userDataService: UserDataService,
                                              identify: IdentifierAction,
                                              checkRecentClaims: CheckRecentClaimsAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: BuildingSocietyDetailsFormProvider,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: BuildingSocietyDetailsView
                                            )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport
    with AnswerExtractor {

  val form = formProvider()

  def onPageLoad(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData) {
    implicit request =>
      getAnswer(BankAccountHolderPage) {
        accountHolder =>

          val maybeGuidance = guidance(accountHolder)

          val preparedForm = request.userAnswers.get(BuildingSocietyDetailsPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, waypoints, maybeGuidance))
      }
  }

  def onSubmit(waypoints: Waypoints): Action[AnyContent] = (identify andThen checkRecentClaims andThen getData andThen requireData).async {
    implicit request =>
      getAnswerAsync(BankAccountHolderPage) {
        accountHolder =>

          val maybeGuidance = guidance(accountHolder)

          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, waypoints, maybeGuidance))),

            value => {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(BuildingSocietyDetailsPage, value))
                _ <- userDataService.set(updatedAnswers)
              } yield Redirect(BuildingSocietyDetailsPage.navigate(waypoints, request.userAnswers, updatedAnswers).route)
            }
          )
      }
  }

  private def guidance(accountHolder: BankAccountHolder)(implicit messages: Messages): Option[String] =
    accountHolder match {
      case BankAccountHolder.Applicant => None
      case BankAccountHolder.JointNames => Some(messages("buildingSocietyDetails.jointNames.p1"))
      case BankAccountHolder.SomeoneElse => Some(messages("buildingSocietyDetails.someoneElse.p1"))
    }
}
