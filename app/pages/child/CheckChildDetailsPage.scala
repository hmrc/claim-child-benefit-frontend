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

package pages.child

import controllers.child.routes
import models.{Index, UserAnswers}
import pages.{CheckAnswersPage, Page, Waypoint, Waypoints}
import play.api.mvc.Call

final case class CheckChildDetailsPage(index: Index) extends CheckAnswersPage {

  override val urlFragment: String = s"check-child-${index.display}"

  override def route(waypoints: Waypoints): Call =
    routes.CheckChildDetailsController.onPageLoad(waypoints, index)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    AddChildPage
}

object CheckChildDetailsPage {

  def waypointFromString(s: String): Option[Waypoint] = {

    val pattern = """check-child-(\d{1,3})""".r.anchored

    s match {
      case pattern(indexDisplay) =>
        Some(CheckChildDetailsPage(Index(indexDisplay.toInt - 1)).waypoint)

      case _ =>
        None
    }
  }
}
