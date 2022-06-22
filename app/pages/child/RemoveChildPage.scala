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
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.DeriveNumberOfChildren

final case class RemoveChildPage(index: Index) extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "removeChild"

  override def route(waypoints: Waypoints): Call =
    routes.RemoveChildController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(DeriveNumberOfChildren).map {
      case n if n > 0 => AddChildPage
      case _ => ChildNamePage(Index(0))
    }.getOrElse(ChildNamePage(Index(0)))
}
