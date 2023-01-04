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
import pages.{NonEmptyWaypoints, Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.DeriveNumberOfChildPreviousNames

final case class RemoveChildPreviousNamePage(childIndex: Index, nameIndex: Index) extends ChildQuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "removeChildPreviousName"

  override def route(waypoints: Waypoints): Call =
    routes.RemoveChildPreviousNameController.onPageLoad(waypoints, childIndex, nameIndex)

  override def nextPageNormalMode(waypoints: Waypoints, originalAnswers: UserAnswers): Page =
    originalAnswers.get(DeriveNumberOfChildPreviousNames(childIndex)).map {
      case n if n > 0 => AddChildPreviousNamePage(childIndex)
      case _          => ChildHasPreviousNamePage(childIndex)
    }.getOrElse(ChildHasPreviousNamePage(childIndex))

  override def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers): Page =
    originalAnswers.get(DeriveNumberOfChildPreviousNames(childIndex)).map {
      case n if n > 0 => AddChildPreviousNamePage(childIndex)
      case _          => ChildHasPreviousNamePage(childIndex)
    }.getOrElse(ChildHasPreviousNamePage(childIndex))
}
