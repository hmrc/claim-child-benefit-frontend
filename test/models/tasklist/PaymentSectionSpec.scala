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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.payments.{ApplicantIncomePage, CheckPaymentDetailsPage, PaymentFrequencyPage}
import services.JourneyProgressService

class PaymentSectionSpec
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
  private val childSection = new ChildSection(mockJourneyProgressService, applicantSection, partnerSection)

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".continue" - {

    "must be whatever the Journey Progress service reports" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(PaymentFrequencyPage)

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, applicantSection, partnerSection, childSection)
      val result = section.continue(answers)

      result.value mustEqual PaymentFrequencyPage
      verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
    }
  }

  ".progress" - {

    "must be Not Started when the Journey Progress service returns Applicant Income" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantIncomePage)

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual NotStarted
      verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
    }

    "must be Completed when the Journey Progress service returns Check Payment Details" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(CheckPaymentDetailsPage)

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual Completed
      verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
    }

    "must be In Progress when the Journey Progress service returns any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(PaymentFrequencyPage)

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual InProgress
      verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
    }
  }

  ".prerequisiteSections" - {

    "must contain Applicant, Partner and Child" in {

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, applicantSection, partnerSection, childSection)

      section.prerequisiteSections(answers) must contain theSameElementsAs Seq(applicantSection, partnerSection, childSection)
    }
  }
}
