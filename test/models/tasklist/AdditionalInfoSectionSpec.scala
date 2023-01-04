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

import models.AdditionalInformation.NoInformation
import models.RelationshipStatus._
import models.UserAnswers
import models.tasklist.SectionStatus.{Completed, NotStarted}
import org.mockito.Mockito
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{AdditionalInformationPage, RelationshipStatusPage}
import services.JourneyProgressService

class AdditionalInfoSectionSpec
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
  private val paymentSection = new PaymentSection(mockJourneyProgressService, relationshipSection, applicantSection, partnerSection, childSection)

  override def beforeEach(): Unit = {
    Mockito.reset(mockJourneyProgressService)
    super.beforeEach()
  }

  ".isShown" - {

    "must be true" in {

      val answers = UserAnswers("id")
      val section = new AdditionalInfoSection(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)

      section.isShown(answers) mustEqual true
    }
  }

  ".continue" - {

    "must be Additional Information" in {

      val answers = UserAnswers("id")
      val section = new AdditionalInfoSection(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)

      section.continue(answers).value mustEqual AdditionalInformationPage
    }
  }

  ".progress" - {

    "must be Not Started if Additional Information has not been answered" in {

      val answers = UserAnswers("id")
      val section = new AdditionalInfoSection(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)

      section.progress(answers) mustEqual NotStarted
    }

    "must be Completed if Additional Information has been answered" in {

      val answers = UserAnswers("id").set(AdditionalInformationPage, NoInformation).success.value
      val section = new AdditionalInfoSection(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)

      section.progress(answers) mustEqual Completed
    }
  }

  ".prerequisiteSections" - {

    "must contain Relationship, Applicant, Child and Payment when the applicant is Single, Divorced, Widowed or Separated" in {

      forAll(Gen.oneOf(Single, Separated, Widowed, Divorced)) {
        relationshipStatus =>
          val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value
          val section = new AdditionalInfoSection(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)

          section.prerequisiteSections(answers) must contain theSameElementsAs Seq(relationshipSection, applicantSection, childSection, paymentSection)
      }
    }

    "must contain Relationship, Applicant, Partner, Child and Payment when the applicant is Married or Cohabiting" in {

      forAll(Gen.oneOf(Married, Cohabiting)) {
        relationshipStatus =>
          val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value
          val section = new AdditionalInfoSection(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)

          section.prerequisiteSections(answers) must contain theSameElementsAs Seq(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)
      }
    }

    "must contain Relationship, Applicant, Child and Payment when the applicant's relationship status is unknown'" in {

      val answers = UserAnswers("id")
      val section = new AdditionalInfoSection(relationshipSection, applicantSection, partnerSection, childSection, paymentSection)

      section.prerequisiteSections(answers) must contain theSameElementsAs Seq(relationshipSection, applicantSection, childSection, paymentSection)
    }
  }
}
