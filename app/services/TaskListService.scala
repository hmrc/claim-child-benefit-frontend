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

package services

import controllers.applicant.{routes => applicantRoutes}
import controllers.routes
import models.UserAnswers
import models.tasklist.SectionViewModel
import models.tasklist.SectionStatus._
import pages.{CheckRelationshipDetailsPage, EmptyWaypoints, RecentlyClaimedPage}
import pages.applicant.{ApplicantNinoKnownPage, CheckApplicantDetailsPage}

import javax.inject.Inject

class TaskListService @Inject()(journeyProgress: JourneyProgressService) {

  def sections(answers: UserAnswers): Seq[SectionViewModel] =
    Seq(
      SectionViewModel("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), NotStarted),
      SectionViewModel("taskList.maritalDetails", None, CannotStart),
      SectionViewModel("taskList.childDetails", None, CannotStart),
      SectionViewModel("taskList.paymentDetails", None, CannotStart),
      SectionViewModel("taskList.furtherDetails", None, CannotStart)
    )

  private[services] def relationshipSection(answers: UserAnswers): SectionViewModel = {
    val page = journeyProgress.continue(RecentlyClaimedPage, answers)
    val status = page match {
      case RecentlyClaimedPage => NotStarted
      case CheckRelationshipDetailsPage => Completed
      case _ => InProgress
    }

    SectionViewModel("taskList.relationshipDetails", Some(page.route(EmptyWaypoints)), status)
  }

  private[services] def applicantSection(answers: UserAnswers): SectionViewModel = {
    val page = journeyProgress.continue(ApplicantNinoKnownPage, answers)
    val status = page match {
      case ApplicantNinoKnownPage => NotStarted
      case CheckApplicantDetailsPage => Completed
      case _ => InProgress
    }

    SectionViewModel("taskList.yourDetails", Some(page.route(EmptyWaypoints)), status)
  }
}
