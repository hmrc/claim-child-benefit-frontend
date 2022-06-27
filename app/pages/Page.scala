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

package pages

import models.{CheckMode, NormalMode, UserAnswers}
import play.api.mvc.Call

import scala.language.implicitConversions

case class PageAndWaypoints(page: Page, waypoints: Waypoints) {

  lazy val route: Call = page.route(waypoints)

  def next(answers: UserAnswers): PageAndWaypoints =
    page.navigate(waypoints, answers)
}

object PageAndWaypoints {

  implicit def toCall(p: PageAndWaypoints): Call = p.route
}

trait Page {
  def navigate(waypoints: Waypoints, answers: UserAnswers): PageAndWaypoints = {
    val targetPage            = nextPage(waypoints, answers)
    val recalibratedWaypoints = waypoints.recalibrate(this, targetPage)

    PageAndWaypoints(targetPage, recalibratedWaypoints)
  }

  protected def nextPage(waypoints: Waypoints, answers: UserAnswers): Page =
    waypoints match {
      case EmptyWaypoints =>
        nextPageNormalMode(waypoints, answers)

      case b: NonEmptyWaypoints =>
        b.currentMode match {
          case CheckMode  => nextPageCheckMode(b, answers)
          case NormalMode => nextPageNormalMode(b, answers)
        }
    }

  protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    waypoints.next.page

  protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    IndexPage

  def route(waypoints: Waypoints): Call

  def changeLink(waypoints: Waypoints, sourcePage: CheckAnswersPage): Call =
    route(waypoints.setNextWaypoint(sourcePage.waypoint))

  def changeLink(waypoints: Waypoints, sourcePage: AddItemPage): Call =
    route(waypoints.setNextWaypoint(sourcePage.waypoint(CheckMode)))
}
