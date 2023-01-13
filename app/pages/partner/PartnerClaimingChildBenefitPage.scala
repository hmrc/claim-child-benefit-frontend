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

package pages.partner

import controllers.partner.routes
import models.PartnerClaimingChildBenefit._
import models.{Index, PartnerClaimingChildBenefit, UserAnswers}
import pages.child.ChildNamePage
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object PartnerClaimingChildBenefitPage extends QuestionPage[PartnerClaimingChildBenefit] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "partnerClaimingChildBenefit"

  override def route(waypoints: Waypoints): Call =
    routes.PartnerClaimingChildBenefitController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case GettingPayments | NotGettingPayments | WaitingToHear => PartnerEldestChildNamePage
      case NotClaiming                                          => ChildNamePage(Index(0))
    }.orRecover

  override def cleanup(value: Option[PartnerClaimingChildBenefit], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case NotClaiming =>
        userAnswers.remove(PartnerEldestChildNamePage)
          .flatMap(_.remove(PartnerEldestChildDateOfBirthPage))

      case GettingPayments | NotGettingPayments | WaitingToHear =>
        super.cleanup(value, userAnswers)
    }.getOrElse(super.cleanup(value, userAnswers))
}
