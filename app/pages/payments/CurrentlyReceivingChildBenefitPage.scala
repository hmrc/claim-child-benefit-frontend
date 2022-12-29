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

package pages.payments

import controllers.payments.routes
import models.CurrentlyReceivingChildBenefit.{GettingPayments, NotClaiming, NotGettingPayments}
import models.{CurrentlyReceivingChildBenefit, UserAnswers}
import pages.applicant.CheckApplicantDetailsPage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object CurrentlyReceivingChildBenefitPage extends QuestionPage[CurrentlyReceivingChildBenefit] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "currentlyReceivingChildBenefit"

  override def route(waypoints: Waypoints): Call =
    routes.CurrentlyReceivingChildBenefitController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case GettingPayments | NotGettingPayments => EldestChildNamePage
      case NotClaiming                          => CheckApplicantDetailsPage
    }.orRecover

  override def cleanup(value: Option[CurrentlyReceivingChildBenefit], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case NotClaiming =>
        userAnswers.remove(EldestChildNamePage)
          .flatMap(_.remove(EldestChildDateOfBirthPage))

      case _ =>
        super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))
}
