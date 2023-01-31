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

package pages.applicant

import controllers.applicant.routes
import models.{Index, Nationality, NormalMode, UserAnswers}
import pages.{AddToListQuestionPage, AddToListSection, ApplicantNationalitiesSection, Page, QuestionPage, Waypoint, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

final case class ApplicantNationalityPage(index: Index) extends QuestionPage[Nationality] with AddToListQuestionPage {

  override val section: AddToListSection = ApplicantNationalitiesSection
  override val addItemWaypoint: Waypoint = AddApplicantNationalityPage().waypoint(NormalMode)

  override def path: JsPath = JsPath \ toString \ index.position

  override def toString: String = "applicantNationalities"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantNationalityController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    AddApplicantNationalityPage(Some(index))
}
