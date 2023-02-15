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
import models.{CheckMode, Index, NormalMode, UserAnswers}
import pages.{AddItemPage, Page, QuestionPage, Waypoint, Waypoints}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Call
import queries.{Derivable, DeriveNumberOfChildPreviousNames}

final case class AddChildPreviousNamePage(childIndex: Index, nameIndex: Option[Index] = None) extends AddItemPage(nameIndex) with QuestionPage[Boolean] {

  override def isTheSamePage(other: Page): Boolean = other match {
    case p: AddChildPreviousNamePage => p.childIndex == this.childIndex
    case _ => false
  }

  override val checkModeUrlFragment: String = s"change-child-name-${childIndex.display}"
  override val normalModeUrlFragment: String = s"add-child-name-${childIndex.display}"

  override def path: JsPath = JsPath \ "children" \ childIndex.position \ toString

  override def toString: String = "addChildPreviousName"

  override def route(waypoints: Waypoints): Call =
    routes.AddChildPreviousNameController.onPageLoad(waypoints, childIndex)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        nameIndex
          .map(i => ChildPreviousNamePage(childIndex, Index(i.position + 1)))
          .getOrElse {
            answers
              .get(deriveNumberOfItems)
              .map(n => ChildPreviousNamePage(childIndex, Index(n)))
              .orRecover
          }

      case false =>
        ChildDateOfBirthPage(childIndex)
    }.orRecover

  override def deriveNumberOfItems: Derivable[Seq[JsObject], Int] = DeriveNumberOfChildPreviousNames(childIndex)
}

object AddChildPreviousNamePage {

  def waypointFromString(s: String): Option[Waypoint] = {

    val normalModePattern = """add-child-name-(\d{1,3})""".r.anchored
    val checkModePattern = """change-child-name-(\d{1,3})""".r.anchored

    s match {
      case normalModePattern(indexDisplay) =>
        Some(AddChildPreviousNamePage(Index(indexDisplay.toInt - 1), None).waypoint(NormalMode))

      case checkModePattern(indexDisplay) =>
        Some(AddChildPreviousNamePage(Index(indexDisplay.toInt - 1), None).waypoint(CheckMode))

      case _ =>
        None
    }
  }
}