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
import pages.{AddItemPage, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.DeriveNumberOfChildPreviousNames

final case class AddChildPreviousNamePage(index: Index) extends QuestionPage[Boolean] with AddItemPage {

  override val checkModeUrlFragment: String = s"change-child-name-${index.position}"
  override val normalModeUrlFragment: String = s"add-child-name-${index.position}"

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "addChildPreviousName"

  override def route(waypoints: Waypoints): Call =
    routes.AddChildPreviousNameController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers
          .get(DeriveNumberOfChildPreviousNames(index))
          .map(n => ChildPreviousNamePage(index, Index(n)))
          .orRecover

      case false =>
        ChildBiologicalSexPage(index)
    }.orRecover
}
