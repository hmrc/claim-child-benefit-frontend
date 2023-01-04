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

package models.tasklist

import models.UserAnswers
import models.tasklist.SectionStatus.{Completed, InProgress, NotStarted}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.applicant.{ApplicantDateOfBirthPage, ApplicantNinoKnownPage, ApplicantNinoPage, CheckApplicantDetailsPage}
import services.JourneyProgressService

class ApplicantSectionSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach with OptionValues {

  private val mockJourneyProgressService = mock[JourneyProgressService]
  private val relationshipSection = new RelationshipSection(mockJourneyProgressService)

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".isShown" - {

    "must be true" in {

      val answers = UserAnswers("id")
      val section = new ApplicantSection(mockJourneyProgressService, relationshipSection)

      section.isShown(answers) mustEqual true
    }
  }

  ".continue" - {

    "must be whatever the Journey Progress service reports" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantDateOfBirthPage)

      val answers = UserAnswers("id")
      val section = new ApplicantSection(mockJourneyProgressService, relationshipSection)
      val result = section.continue(answers)

      result.value mustEqual ApplicantDateOfBirthPage
      verify(mockJourneyProgressService, times(1)).continue(ApplicantNinoKnownPage, answers)
    }
  }

  ".progress" - {

    "must be Not Started when the Journey Progress service returns Applicant Nino Known" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantNinoKnownPage)

      val answers = UserAnswers("id")
      val section = new ApplicantSection(mockJourneyProgressService, relationshipSection)
      val result = section.progress(answers)

      result mustEqual NotStarted
      verify(mockJourneyProgressService, times(1)).continue(ApplicantNinoKnownPage, answers)
    }

    "must be Completed when the Journey Progress service returns Check Applicant Details" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(CheckApplicantDetailsPage)

      val answers = UserAnswers("id")
      val section = new ApplicantSection(mockJourneyProgressService, relationshipSection)
      val result = section.progress(answers)

      result mustEqual Completed
      verify(mockJourneyProgressService, times(1)).continue(ApplicantNinoKnownPage, answers)
    }

    "must be In Progress when the Journey Progress service returns any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantNinoPage)

      val answers = UserAnswers("id")
      val section = new ApplicantSection(mockJourneyProgressService, relationshipSection)
      val result = section.progress(answers)

      result mustEqual InProgress
      verify(mockJourneyProgressService, times(1)).continue(ApplicantNinoKnownPage, answers)
    }
  }

  "the only prerequisite section must be Relationship" in {

    val answers = UserAnswers("id")
    val section = new ApplicantSection(mockJourneyProgressService, relationshipSection)

    section.prerequisiteSections(answers) must contain only relationshipSection
  }
}
