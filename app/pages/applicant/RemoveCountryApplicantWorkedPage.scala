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
import models.{Index, UserAnswers}
import pages.{NonEmptyWaypoints, Page, Waypoints}
import play.api.mvc.Call
import queries.DeriveNumberOfApplicantNationalities

case class RemoveCountryApplicantWorkedPage(index: Index) extends Page {

  override def route(waypoints: Waypoints): Call =
    routes.RemoveCountryApplicantWorkedController.onPageLoad(waypoints, index)

  override def nextPageNormalMode(waypoints: Waypoints, originalAnswers: UserAnswers): Page =
    originalAnswers.get(DeriveNumberOfApplicantNationalities).map {
      case n if n > 0 => AddCountryApplicantWorkedPage()
      case _ => ApplicantWorkedAbroadPage
    }.getOrElse(ApplicantWorkedAbroadPage)

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(DeriveNumberOfApplicantNationalities).map {
      case n if n > 0 => AddCountryApplicantWorkedPage()
      case _ => ApplicantWorkedAbroadPage
    }.getOrElse(ApplicantWorkedAbroadPage)
}
