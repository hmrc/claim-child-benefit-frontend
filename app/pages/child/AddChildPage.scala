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
import pages.{AddItemPage, AdditionalInformationPage, CheckYourAnswersPage, NonEmptyWaypoints, Page, QuestionPage, TaskListPage, Terminus, Waypoints}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Call
import queries.{Derivable, DeriveNumberOfChildren}

final case class AddChildPage(override val index: Option[Index] = None) extends AddItemPage(index) with QuestionPage[Boolean] with Terminus {

  override def isTheSamePage(other: Page): Boolean = other match {
    case _: AddChildPage => true
    case _ => false
  }

  override val normalModeUrlFragment: String = "add-child"
  override val checkModeUrlFragment: String = "change-child"

  override def path: JsPath = JsPath \ toString

  override def toString: String = "addChild"

  override def route(waypoints: Waypoints): Call =
    routes.AddChildController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        index
          .map(i => ChildNamePage(Index(i.position + 1)))
          .getOrElse {
            answers.get(deriveNumberOfItems)
              .map(n => ChildNamePage(Index(n)))
              .orRecover
          }

      case false =>
        TaskListPage
    }.orRecover

  override def deriveNumberOfItems: Derivable[Seq[JsObject], Int] = DeriveNumberOfChildren
}
