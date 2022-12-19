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
import models.UserAnswers
import models.tasklist.SectionStatus.{Completed, NotStarted}
import pages.{AdditionalInformationPage, Page, RelationshipStatusPage}

import javax.inject.Inject

class AdditionalInfoSection @Inject()(
                                       relationshipSection: RelationshipSection,
                                       applicantSection: ApplicantSection,
                                       partnerSection: PartnerSection,
                                       childSection: ChildSection,
                                       paymentSection: PaymentSection
                                     ) extends Section {

  override val name: String = "taskList.additionalInfo"

  override def continue(answers: UserAnswers): Option[Page] =
    Some(AdditionalInformationPage)

  override def progress(answers: UserAnswers): SectionStatus =
    answers
      .get(AdditionalInformationPage)
      .map(_ => Completed)
      .getOrElse(NotStarted)

  override def prerequisiteSections(answers: UserAnswers): Set[Section] =
    answers.get(RelationshipStatusPage).map {
      case Married | Cohabiting => Set(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)
      case _ => Set(relationshipSection, applicantSection, childSection, paymentSection)
    }.getOrElse(Set(relationshipSection, applicantSection, childSection, paymentSection))
}
