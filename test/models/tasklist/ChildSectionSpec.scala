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

import models.tasklist.SectionStatus.{Completed, InProgress, NotStarted}
import models.{Index, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.child.{AddChildPage, ChildDateOfBirthPage, ChildNamePage}
import services.JourneyProgressService

class ChildSectionSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with ScalaCheckPropertyChecks
    with OptionValues
    with TryValues {

  private val mockJourneyProgressService = mock[JourneyProgressService]
  private val applicantSection = new ApplicantSection(mockJourneyProgressService)
  private val partnerSection = new PartnerSection(mockJourneyProgressService, applicantSection)

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".continue" - {

    "must be whatever the Journey Progress service reports" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ChildDateOfBirthPage(Index(0)))

      val answers = UserAnswers("id")
      val section = new ChildSection(mockJourneyProgressService, applicantSection, partnerSection)
      val result = section.continue(answers)

      result.value mustEqual ChildDateOfBirthPage(Index(0))
      verify(mockJourneyProgressService, times(1)).continue(ChildNamePage(Index(0)), answers)
    }
  }

  ".progress" - {

    "must be Not Started when the Journey Progress service returns Child Name for index 0" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ChildNamePage(Index(0)))

      val answers = UserAnswers("id")
      val section = new ChildSection(mockJourneyProgressService, applicantSection, partnerSection)
      val result = section.progress(answers)

      result mustEqual NotStarted
      verify(mockJourneyProgressService, times(1)).continue(ChildNamePage(Index(0)), answers)
    }

    "must be Completed when the Journey Progress service returns Add Child" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(AddChildPage())

      val answers = UserAnswers("id")
      val section = new ChildSection(mockJourneyProgressService, applicantSection, partnerSection)
      val result = section.progress(answers)

      result mustEqual Completed
      verify(mockJourneyProgressService, times(1)).continue(ChildNamePage(Index(0)), answers)
    }

    "must be In Progress when the Journey Progress service returns any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ChildDateOfBirthPage(Index(1)))

      val answers = UserAnswers("id")
      val section = new ChildSection(mockJourneyProgressService, applicantSection, partnerSection)
      val result = section.progress(answers)

      result mustEqual InProgress
      verify(mockJourneyProgressService, times(1)).continue(ChildNamePage(Index(0)), answers)
    }
  }

  ".prerequisiteSections" - {

    "must contain Applicant and Partner" in {

      val answers = UserAnswers("id")
      val section = new ChildSection(mockJourneyProgressService, applicantSection, partnerSection)

      section.prerequisiteSections(answers) must contain theSameElementsAs Seq(applicantSection, partnerSection)
    }
  }
}
