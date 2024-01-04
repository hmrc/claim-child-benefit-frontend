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

package pages.child

import controllers.child.routes
import models.{ChildName, Index, NormalMode, UserAnswers}
import pages.{AddToListQuestionPage, AddToListSection, ChildPreviousNameSection, Page, QuestionPage, Waypoint, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.DeriveNumberOfChildPreviousNames

import scala.util.Try

final case class ChildPreviousNamePage(
                                        childIndex: Index,
                                        nameIndex: Index
                                      ) extends QuestionPage[ChildName] with AddToListQuestionPage {

  override val section: AddToListSection = ChildPreviousNameSection

  override val addItemWaypoint: Waypoint = AddChildPreviousNamePage(childIndex).waypoint(NormalMode)

  override def path: JsPath = JsPath \ "children" \ childIndex.position \ "previousNames" \ nameIndex.position

  override def toString: String = "childPreviousName"

  override def route(waypoints: Waypoints): Call =
    routes.ChildPreviousNameController.onPageLoad(waypoints, childIndex, nameIndex)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    AddChildPreviousNamePage(childIndex, Some(nameIndex))

  override def cleanup(value: Option[ChildName], userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.get(DeriveNumberOfChildPreviousNames(childIndex)).map {
      case n if n > 0 => super.cleanup(value, userAnswers)
      case _          => userAnswers.remove(ChildNameChangedByDeedPollPage(childIndex))
    }.getOrElse(
      userAnswers.remove(ChildNameChangedByDeedPollPage(childIndex))
    )
}
