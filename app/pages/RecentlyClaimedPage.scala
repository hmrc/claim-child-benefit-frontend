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

package pages

import config.FrontendAppConfig
import controllers.routes
import models.{CheckMode, NormalMode, ServiceType, UserAnswers}
import pages.utils.ExternalPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

object RecentlyClaimedPage extends QuestionPage[ServiceType] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "recentlyClaimed"

  def navigate(waypoints: Waypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers, config: FrontendAppConfig): PageAndWaypoints = {
    val targetPage = waypoints match {
      case EmptyWaypoints => nextPage(updatedAnswers, config)
      case b: NonEmptyWaypoints =>
        b.currentMode match {
          case CheckMode => nextPageCheckMode(b, originalAnswers, updatedAnswers)
          case NormalMode => nextPage(updatedAnswers, config)
        }
    }
    val recalibratedWaypoints = waypoints.recalibrate(this, targetPage)

    PageAndWaypoints(targetPage, recalibratedWaypoints)
  }


  private def nextPage(answers: UserAnswers, config: FrontendAppConfig): Page =
    answers.get(this).map {
      case ServiceType.NewClaim | ServiceType.AddClaim =>
        if (answers.isAuthenticated) {
          TaskListPage
        } else {
          SignInPage
        }
      case ServiceType.CheckClaim => AlreadyClaimedPage
      case ServiceType.RestartChildBenefit => ExternalPage(config.childBenefitTaxChargeRestartUrl)
      case ServiceType.StopChildBenefit => ExternalPage(config.childBenefitTaxChargeStopUrl)
    }.orRecover

  override def route(waypoints: Waypoints): Call =
    routes.RecentlyClaimedController.onPageLoad(waypoints)
}
