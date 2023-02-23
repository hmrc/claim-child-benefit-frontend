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
import org.mockito.Mockito
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AdditionalInformationPage, IncludeAdditionalInformationPage}
import services.JourneyProgressService

class AdditionalInfoSectionSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with OptionValues
    with TryValues {

  private val mockJourneyProgressService = mock[JourneyProgressService]
  private val applicantSection = new ApplicantSection(mockJourneyProgressService)
  private val partnerSection = new PartnerSection(mockJourneyProgressService, applicantSection)
  private val childSection = new ChildSection(mockJourneyProgressService, applicantSection, partnerSection)
  private val paymentSection = new PaymentSection(mockJourneyProgressService, applicantSection, partnerSection, childSection)

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".continue" - {

    "must be Include Additional Information" in {

      val answers = UserAnswers("id")
      val section = new AdditionalInfoSection(applicantSection, partnerSection, childSection, paymentSection)

      section.continue(answers).value mustEqual IncludeAdditionalInformationPage
    }
  }

  ".progress" - {

    "must be Not Started if Include Additional Information has not been answered" in {

      val answers = UserAnswers("id")
      val section = new AdditionalInfoSection(applicantSection, partnerSection, childSection, paymentSection)

      section.progress(answers) mustEqual NotStarted
    }

    "must be in progress if Include Additional Information has been answered as true but Additional Information has not been answered" in {

      val answers = UserAnswers("id").set(IncludeAdditionalInformationPage, true).success.value
      val section = new AdditionalInfoSection(applicantSection, partnerSection, childSection, paymentSection)

      section.progress(answers) mustEqual InProgress
    }

    "must be Completed if Include Additional Information has been answered as false" in {

      val answers = UserAnswers("id").set(IncludeAdditionalInformationPage, false).success.value
      val section = new AdditionalInfoSection(applicantSection, partnerSection, childSection, paymentSection)

      section.progress(answers) mustEqual Completed
    }

    "must be Completed if Include Additional Information has been answered as true and Additional Information has been answered" in {

      val answers =
        UserAnswers("id")
          .set(IncludeAdditionalInformationPage, true).success.value
          .set(AdditionalInformationPage, "foo").success.value

      val section = new AdditionalInfoSection(applicantSection, partnerSection, childSection, paymentSection)

      section.progress(answers) mustEqual Completed
    }
  }

  ".prerequisiteSections" - {

    "must contain Applicant, Child and Payment" in {

      val answers = UserAnswers("id")
      val section = new AdditionalInfoSection(applicantSection, partnerSection, childSection, paymentSection)

      section.prerequisiteSections(answers) must contain theSameElementsAs Seq(applicantSection, partnerSection, childSection, paymentSection)
    }
  }
}
