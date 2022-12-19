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

import models.RelationshipStatus._
import models.tasklist.SectionStatus.{CannotStart, Completed, InProgress, NotStarted}
import models.{PaymentFrequency, RelationshipStatus, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.mockito.MockitoSugar.mock
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.RelationshipStatusPage
import pages.child.AddChildPage
import pages.income.{ApplicantIncomePage, ApplicantOrPartnerIncomePage}
import pages.payments.{CheckPaymentDetailsPage, PaymentFrequencyPage}
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
  private val relationshipSection = new RelationshipSection(mockJourneyProgressService)
  private val applicantSection = new ApplicantSection(mockJourneyProgressService, relationshipSection)
  private val partnerSection = new PartnerSection(mockJourneyProgressService, relationshipSection, applicantSection)
  private val childSection = new ChildSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection)

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".isShown" - {

    "must be true" in {

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)

      section.isShown(answers) mustEqual true
    }
  }

  ".continue" - {

    "must be whatever the Journey Progress service reports when the applicant is single, separated, divorced or widowed" in {

      forAll(Gen.oneOf(Single, Separated, Divorced, Widowed)) {
        relationshipStatus =>
          when(mockJourneyProgressService.continue(any(), any())).thenReturn(PaymentFrequencyPage)

          val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value
          val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
          val result = section.continue(answers)

          result.value mustEqual PaymentFrequencyPage
          verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
      }
    }

    "must be whatever the Journey Progress service reports when the applicant is married or cohabiting" in {

      forAll(Gen.oneOf(Married, Cohabiting)) {
        relationshipStatus =>
          when(mockJourneyProgressService.continue(any(), any())).thenReturn(PaymentFrequencyPage)

          val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value
          val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
          val result = section.continue(answers)

          result.value mustEqual PaymentFrequencyPage
          verify(mockJourneyProgressService, times(1)).continue(ApplicantOrPartnerIncomePage, answers)
      }
    }

    "must be None when relationship status has not been answered" in {

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
      val result = section.continue(answers)

      result must not be defined
    }
  }

  ".progress" - {

    "must be Not Started when the Journey Progress service returns Applicant Income" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantIncomePage)

      val answers = UserAnswers("id").set(RelationshipStatusPage, Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value).success.value
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual NotStarted
      verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
    }

    "must be Not Started when the Journey Progress service returns Applicant or Partner Income" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(ApplicantOrPartnerIncomePage)

      val answers = UserAnswers("id").set(RelationshipStatusPage, Gen.oneOf(Married, Cohabiting).sample.value).success.value
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual NotStarted
      verify(mockJourneyProgressService, times(1)).continue(ApplicantOrPartnerIncomePage, answers)
    }

    "must be Completed when the Journey Progress service returns Check Payment Details" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(CheckPaymentDetailsPage)

      val answers = UserAnswers("id").set(RelationshipStatusPage, Single).success.value
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual Completed
      verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
    }

    "must be In Progress when the Journey Progress service returns any other page" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(PaymentFrequencyPage)

      val answers = UserAnswers("id").set(RelationshipStatusPage, Single).success.value
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual InProgress
      verify(mockJourneyProgressService, times(1)).continue(ApplicantIncomePage, answers)
    }

    "must be Cannot Start when relationship status has not been answered" in {

      when(mockJourneyProgressService.continue(any(), any())).thenReturn(PaymentFrequencyPage)

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)
      val result = section.progress(answers)

      result mustEqual CannotStart
    }
  }

  ".prerequisiteSections" - {

    "must contain Relationship, Applicant and Child when the applicant is Single, Divorced, Widowed or Separated" in {

      forAll(Gen.oneOf(Single, Separated, Widowed, Divorced)) {
        relationshipStatus =>
          val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value
          val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)

          section.prerequisiteSections(answers) must contain theSameElementsAs Seq(relationshipSection, applicantSection, childSection)
      }
    }

    "must contain Relationship, Applicant, Partner and Child when the applicant is Married or Cohabiting" in {

      forAll(Gen.oneOf(Married, Cohabiting)) {
        relationshipStatus =>
          val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value
          val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)

          section.prerequisiteSections(answers) must contain theSameElementsAs Seq(relationshipSection, applicantSection, partnerSection, childSection)
      }
    }

    "must contain Relationship, Applicant and Child when the applicant's relationship status is unknown'" in {

      val answers = UserAnswers("id")
      val section = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)

      section.prerequisiteSections(answers) must contain theSameElementsAs Seq(relationshipSection, applicantSection, childSection)
    }
  }
}
