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

package pages.applicant

import controllers.applicant.routes
import models.{Index, UserAnswers}
import pages.{Page, Waypoints}
import play.api.mvc.Call
import queries.DeriveNumberOfPreviousFamilyNames

case class RemoveApplicantPreviousFamilyNamePage(index: Index) extends Page {

  override def route(waypoints: Waypoints): Call =
    routes.RemoveApplicantPreviousFamilyNameController.onPageLoad(waypoints, index)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(DeriveNumberOfPreviousFamilyNames).map {
      case n if n > 0 => AddApplicantPreviousFamilyNamePage
      case _ => ApplicantHasPreviousFamilyNamePage
    }.getOrElse(ApplicantHasPreviousFamilyNamePage)
}
