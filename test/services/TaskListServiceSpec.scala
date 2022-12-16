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
import models.tasklist.SectionStatus.{CannotStart, Completed, InProgress, NotStarted}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AlwaysLivedInUkPage, CheckRelationshipDetailsPage, EmptyWaypoints, RecentlyClaimedPage}
import pages.applicant.{ApplicantNinoKnownPage, ApplicantNinoPage, CheckApplicantDetailsPage}

class TaskListServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with TryValues with OptionValues with BeforeAndAfterEach {

  private val mockJourneyProgressService = mock[JourneyProgressService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".sections" - {

    "must return Your Details as `Not Started` and the other sections as `Cannot Start` when no answers have been provided" in {

      val answers = UserAnswers("id")
      val service = new TaskListService(mockJourneyProgressService)

      val result = service.sections(answers)

      result mustEqual Seq(
        SectionViewModel("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), NotStarted),
        SectionViewModel("taskList.maritalDetails", None, CannotStart),
        SectionViewModel("taskList.childDetails", None, CannotStart),
        SectionViewModel("taskList.paymentDetails", None, CannotStart),
        SectionViewModel("taskList.furtherDetails", None, CannotStart)
      )
    }
  }

  ".relationshipStatusSection" - {

    "must return a link and a status of Not Started when the Journey Progress service tells us to continue from the first page of the section" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(RecentlyClaimedPage)

      val answers = UserAnswers("id")
      val service = new TaskListService(mockJourneyProgressService)

      val result = service.relationshipSection(answers)

      result mustEqual SectionViewModel("taskList.relationshipDetails", Some(routes.RecentlyClaimedController.onPageLoad(EmptyWaypoints)), NotStarted)
      verify(mockJourneyProgressService, times(1)).continue(RecentlyClaimedPage, answers)
    }

    "must return a link and a status of Complete when the Journey Progress service tells us to continue from the Check Relationship Details page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(CheckRelationshipDetailsPage)

      val answers = UserAnswers("id")
      val service = new TaskListService(mockJourneyProgressService)

      val result = service.relationshipSection(answers)

      result mustEqual SectionViewModel("taskList.relationshipDetails", Some(routes.CheckRelationshipDetailsController.onPageLoad), Completed)
      verify(mockJourneyProgressService, times(1)).continue(RecentlyClaimedPage, answers)
    }

    "must return a link and a status of In Progress when the Journey Progress service tells us to continue from any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(AlwaysLivedInUkPage)

      val answers = UserAnswers("id")
      val service = new TaskListService(mockJourneyProgressService)

      val result = service.relationshipSection(answers)

      result mustEqual SectionViewModel("taskList.relationshipDetails", Some(routes.AlwaysLivedInUkController.onPageLoad(EmptyWaypoints)), InProgress)
      verify(mockJourneyProgressService, times(1)).continue(RecentlyClaimedPage, answers)
    }
  }

  ".applicantSection" - {

    "must return a link and a status of Not Started when the Journey Progress service tells us to continue from the first page of the section" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantNinoKnownPage)

      val answers = UserAnswers("id")
      val service = new TaskListService(mockJourneyProgressService)

      val result = service.applicantSection(answers)

      result mustEqual SectionViewModel("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), NotStarted)
      verify(mockJourneyProgressService, times(1)).continue(ApplicantNinoKnownPage, answers)
    }

    "must return a link and a status of Complete when the Journey Progress service tells us to continue from the Check Applicant Details page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(CheckApplicantDetailsPage)

      val answers = UserAnswers("id")
      val service = new TaskListService(mockJourneyProgressService)

      val result = service.applicantSection(answers)

      result mustEqual SectionViewModel("taskList.yourDetails", Some(applicantRoutes.CheckApplicantDetailsController.onPageLoad), Completed)
      verify(mockJourneyProgressService, times(1)).continue(ApplicantNinoKnownPage, answers)
    }

    "must return a link and a status of In Progress when the Journey Progress service tells us to continue from any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantNinoPage)

      val answers = UserAnswers("id")
      val service = new TaskListService(mockJourneyProgressService)

      val result = service.applicantSection(answers)

      result mustEqual SectionViewModel("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoController.onPageLoad(EmptyWaypoints)), InProgress)
      verify(mockJourneyProgressService, times(1)).continue(ApplicantNinoKnownPage, answers)
    }
  }
}
