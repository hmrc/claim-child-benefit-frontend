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

package pages.income

import controllers.income.routes
import models.RelationshipStatus._
import models.UserAnswers
import pages.partner.PartnerNamePage
import pages.payments.{ClaimedChildBenefitBeforePage, WantToBePaidPage, WantToBePaidWeeklyPage}
import pages.{NonEmptyWaypoints, Page, RelationshipStatusPage, Waypoints}
import play.api.mvc.Call

case object TaxChargeExplanationPage extends Page {

  override def route(waypoints: Waypoints): Call =
    routes.TaxChargeExplanationController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    ClaimedChildBenefitBeforePage

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(RelationshipStatusPage).map {
      case Married | Cohabiting =>
        answers.get(PartnerNamePage)
          .map(_ => waypoints.next.page)
          .getOrElse(PartnerNamePage)

      case Single | Divorced | Separated | Widowed =>
        answers.get(WantToBePaidPage).map {
          case true =>
            answers.get(WantToBePaidWeeklyPage)
              .map(_ => waypoints.next.page)
              .getOrElse(WantToBePaidWeeklyPage)
          case false =>
            waypoints.next.page
        }.orRecover
    }.orRecover
}
