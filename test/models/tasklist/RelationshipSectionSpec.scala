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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.{AlwaysLivedInUkPage, CheckRelationshipDetailsPage, RecentlyClaimedPage}
import services.JourneyProgressService

class RelationshipSectionSpec extends AnyFreeSpec with Matchers with MockitoSugar with BeforeAndAfterEach with OptionValues {

  private val mockJourneyProgressService = mock[JourneyProgressService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".isShown" - {

    "must be true" in {

      val answers = UserAnswers("id")
      val section = new RelationshipSection(mockJourneyProgressService)

      section.isShown(answers) mustEqual true
    }
  }

  ".continue" - {

    "must be whatever the Journey Progress service reports" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(RecentlyClaimedPage)

      val answers = UserAnswers("id")
      val section = new RelationshipSection(mockJourneyProgressService)
      val result = section.continue(answers)

      result.value mustEqual RecentlyClaimedPage
      verify(mockJourneyProgressService, times(1)).continue(RecentlyClaimedPage, answers)
    }
  }

  ".progress" - {

    "must be Not Started when the Journey Progress service returns Recently Claimed" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(RecentlyClaimedPage)

      val answers = UserAnswers("id")
      val section = new RelationshipSection(mockJourneyProgressService)
      val result = section.progress(answers)

      result mustEqual NotStarted
      verify(mockJourneyProgressService, times(1)).continue(RecentlyClaimedPage, answers)
    }

    "must be Completed when the Journey Progress service returns Check Relationship Details" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(CheckRelationshipDetailsPage)

      val answers = UserAnswers("id")
      val section = new RelationshipSection(mockJourneyProgressService)
      val result = section.progress(answers)

      result mustEqual Completed
      verify(mockJourneyProgressService, times(1)).continue(RecentlyClaimedPage, answers)
    }

    "must be In Progress when the Journey Progress service returns any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(AlwaysLivedInUkPage)

      val answers = UserAnswers("id")
      val section = new RelationshipSection(mockJourneyProgressService)
      val result = section.progress(answers)

      result mustEqual InProgress
      verify(mockJourneyProgressService, times(1)).continue(RecentlyClaimedPage, answers)
    }
  }

  "there must not be any prerequisite sections" in {

    val answers = UserAnswers("id")
    val section = new RelationshipSection(mockJourneyProgressService)
    section.prerequisiteSections(answers) mustBe empty
  }
}
