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

import models.RelationshipStatus.{Cohabiting, Married}
import models.tasklist.SectionStatus.{Completed, InProgress, NotStarted}
import models.{Index, UserAnswers}
import pages.{Page, RelationshipStatusPage}
import pages.child.{AddChildPage, ChildNamePage}
import services.JourneyProgressService

import javax.inject.Inject

class ChildSection @Inject()(
                              journeyProgress: JourneyProgressService,
                              relationshipSection: RelationshipSection,
                              applicantSection: ApplicantSection,
                              partnerSection: PartnerSection
                            ) extends Section {
  override def continue(answers: UserAnswers): Page =
    journeyProgress.continue(ChildNamePage(Index(0)), answers)

  override def progress(answers: UserAnswers): SectionStatus =
    continue(answers) match {
      case ChildNamePage(Index(0)) => NotStarted
      case AddChildPage => Completed
      case _ => InProgress
    }

  override def prerequisiteSections(answers: UserAnswers): Set[Section] =
    answers.get(RelationshipStatusPage).map {
      case Married | Cohabiting => Set(relationshipSection, applicantSection, partnerSection)
      case _ => Set(relationshipSection, applicantSection)
    }.getOrElse(Set(relationshipSection, applicantSection))
}
