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
import models.JourneyModel.Applicant
import models.{AdultName, CurrentlyReceivingChildBenefit, JourneyModelProvider, UkAddress, UserAnswers}
import models.tasklist.Section
import models.tasklist.SectionStatus.{CannotStart, InProgress, NotStarted}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.applicant.ApplicantNinoKnownPage
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class TaskListServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with TryValues with OptionValues with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val mockJourneyModelProvider = mock[JourneyModelProvider]

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyModelProvider)
    super.beforeEach()
  }

  ".sections" - {

    "must return Your Details as `Not Started` and the other sections as `Cannot Start` when no answers have been provided" in {

      val answers = UserAnswers("id")
      val service = new TaskListService()

      val result = service.sections(answers).futureValue

      result mustEqual Seq(
        Section("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), NotStarted),
        Section("taskList.maritalDetails", None, CannotStart),
        Section("taskList.childDetails", None, CannotStart),
        Section("taskList.paymentDetails", None, CannotStart),
        Section("taskList.furtherDetails", None, CannotStart)
      )
    }
  }

  ".applicantSection" - {

    "must return a link and a status of Not Started when no answers have been provided" in {

      val answers = UserAnswers("id")
      val service = new TaskListService()

      val result = service.applicantSection(answers).futureValue

      result mustEqual Section("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), NotStarted)
    }

    "must return a link and a status of In Progress when one question has been answered" in {

      val answers = UserAnswers("id").set(ApplicantNinoKnownPage, true).success.value
      val service = new TaskListService()

      val result = service.applicantSection(answers).futureValue

      result mustEqual Section("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), InProgress)
    }
//
//    "must return a link and a status of Complete when the Journey Model provider can return an Applicant" in {
//
//      val applicant = Applicant(
//        name = AdultName("first", None, "last"),
//        previousFamilyNames = Nil,
//        dateOfBirth = LocalDate.now,
//        nationalInsuranceNumber = None,
//        currentAddress = UkAddress("first", None, "town", None, "AA11 1AA"),
//        previousAddress = None,
//        telephoneNumber = "0777 777777",
//        nationality = "British",
//        alwaysLivedInUk = true,
//        memberOfHMForcesOrCivilServantAbroad = Some(false),
//        currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming
//      )
//
//      when(mockJourneyModelProvider.)
//
//      val answers = UserAnswers("id")
//      val service = new TaskListService()
//
//      val result = service.applicantSection(answers).futureValue
//
//      result mustEqual Section("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), Complete)
//    }
  }
}
