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

package pages.child

import controllers.child.routes
import models.{Index, UserAnswers}
import pages.{NonEmptyWaypoints, Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.AllChildPreviousNames

import scala.util.Try

final case class ChildHasPreviousNamePage(index: Index) extends ChildQuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "childHasPreviousName"

  override def route(waypoints: Waypoints): Call =
    routes.ChildHasPreviousNameController.onPageLoad(waypoints, index)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true => ChildNameChangedByDeedPollPage(index)
      case false => ChildBiologicalSexPage(index)
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers.get(ChildNameChangedByDeedPollPage(index))
        .map(_ => waypoints.next.page)
        .getOrElse(ChildNameChangedByDeedPollPage(index))

      case false =>
        waypoints.next.page

    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case false =>
        userAnswers
          .remove(ChildNameChangedByDeedPollPage(index))
          .flatMap(_.remove(AllChildPreviousNames(index)))

      case true =>
        super.cleanup(value, userAnswers)

    }.getOrElse(super.cleanup(value, userAnswers))
}
