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

package pages.applicant

import controllers.applicant.routes
import models.{ApplicantPreviousName, Index, NormalMode, UserAnswers}
import pages.{AddToListQuestionPage, AddToListSection, Page, PreviousFamilyNamesSection, QuestionPage, Waypoint, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

case class ApplicantPreviousFamilyNamePage(index: Index) extends QuestionPage[ApplicantPreviousName] with AddToListQuestionPage {

  override val addItemWaypoint: Waypoint = AddApplicantPreviousFamilyNamePage().waypoint(NormalMode)
  override val section: AddToListSection = PreviousFamilyNamesSection

  override def path: JsPath = JsPath \ toString \ index.position

  override def toString: String = "applicantPreviousFamilyNames"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantPreviousFamilyNameController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    AddApplicantPreviousFamilyNamePage(Some(index))
}
