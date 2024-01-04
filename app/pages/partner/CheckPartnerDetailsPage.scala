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

package pages.partner


import controllers.partner.routes
import models.UserAnswers
import pages.{CheckAnswersPage, Page, TaskListPage, Terminus, Waypoints}
import play.api.mvc.Call

object CheckPartnerDetailsPage extends CheckAnswersPage with Terminus {

  override def isTheSamePage(other: Page): Boolean = other match {
    case CheckPartnerDetailsPage => true
    case _ => false
  }

  override val urlFragment: String = "check-partners-details"

  override def route(waypoints: Waypoints): Call = routes.CheckPartnerDetailsController.onPageLoad

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    TaskListPage
}
