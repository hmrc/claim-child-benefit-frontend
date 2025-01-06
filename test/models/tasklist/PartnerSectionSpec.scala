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

package models.tasklist

import models.UserAnswers
import models.tasklist.SectionStatus.{Completed, InProgress, NotStarted}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.{CheckPartnerDetailsPage, PartnerNinoPage, RelationshipStatusPage}
import services.JourneyProgressService

class PartnerSectionSpec
    extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach with OptionValues with TryValues {

  private val mockJourneyProgressService = mock[JourneyProgressService]
  private val applicantSection = new ApplicantSection(mockJourneyProgressService)

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".continue" - {

    "must be whatever the Journey Progress service reports" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(PartnerNinoPage)

      val answers = UserAnswers("id")
      val section = new PartnerSection(mockJourneyProgressService, applicantSection)
      val result = section.continue(answers)

      result.value `mustEqual` PartnerNinoPage
      verify(mockJourneyProgressService, times(1)).continue(RelationshipStatusPage, answers)
    }
  }

  ".progress" - {

    "must be Not Started when the Journey Progress service returns Relationship Status" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(RelationshipStatusPage)

      val answers = UserAnswers("id")
      val section = new PartnerSection(mockJourneyProgressService, applicantSection)
      val result = section.progress(answers)

      result `mustEqual` NotStarted
      verify(mockJourneyProgressService, times(1)).continue(RelationshipStatusPage, answers)
    }

    "must be Completed when the Journey Progress service returns Check Partner Details" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(CheckPartnerDetailsPage)

      val answers = UserAnswers("id")
      val section = new PartnerSection(mockJourneyProgressService, applicantSection)
      val result = section.progress(answers)

      result `mustEqual` Completed
      verify(mockJourneyProgressService, times(1)).continue(RelationshipStatusPage, answers)
    }

    "must be In Progress when the Journey Progress service returns any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(PartnerNinoPage)

      val answers = UserAnswers("id")
      val section = new PartnerSection(mockJourneyProgressService, applicantSection)
      val result = section.progress(answers)

      result `mustEqual` InProgress
      verify(mockJourneyProgressService, times(1)).continue(RelationshipStatusPage, answers)
    }
  }

  "the prerequisite sections must be Relationship and Applicant" in {

    val answers = UserAnswers("id")
    val section = new PartnerSection(mockJourneyProgressService, applicantSection)

    section.prerequisiteSections(answers) must contain theSameElementsAs Seq(applicantSection)
  }
}
