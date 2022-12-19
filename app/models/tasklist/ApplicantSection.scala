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

package models.tasklist

import models.UserAnswers
import models.tasklist.SectionStatus.{Completed, InProgress, NotStarted}
import pages.Page
import pages.applicant.{ApplicantNinoKnownPage, CheckApplicantDetailsPage}
import services.JourneyProgressService

import javax.inject.Inject

class ApplicantSection @Inject()(journeyProgress: JourneyProgressService, relationshipSection: RelationshipSection) extends Section {

  override def continue(answers: UserAnswers): Page =
    journeyProgress.continue(ApplicantNinoKnownPage, answers)

  override def progress(answers: UserAnswers): SectionStatus =
    continue(answers) match {
      case ApplicantNinoKnownPage => NotStarted
      case CheckApplicantDetailsPage => Completed
      case _ => InProgress
    }

  override def prerequisiteSections(answers: UserAnswers): Set[Section] =
    Set(relationshipSection)
}