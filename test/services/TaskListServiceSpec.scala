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

package services

import models.RelationshipStatus._
import models.UserAnswers
import models.tasklist.SectionStatus.{CannotStart, Completed, InProgress}
import models.tasklist._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.RelationshipStatusPage
import play.api.mvc.Call

class TaskListServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with TryValues with OptionValues with BeforeAndAfterEach {

  private val mockApplicantSection = mock[ApplicantSection]
  private val mockChildSection = mock[ChildSection]
  private val mockPartnerSection = mock[PartnerSection]
  private val mockPaymentSection = mock[PaymentSection]
  private val mockAdditionalInfoSection = mock[AdditionalInfoSection]

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockApplicantSection,
      mockChildSection,
      mockPartnerSection,
      mockPaymentSection,
      mockAdditionalInfoSection
    )
    super.beforeEach()
  }

  ".sections" - {

    "must return all sections that should be shown in the correct order" in {

      val service = new TaskListService(
        mockApplicantSection,
        mockPartnerSection,
        mockChildSection,
        mockPaymentSection,
        mockAdditionalInfoSection
      )

      when(mockApplicantSection.asViewModel(any())).thenReturn(SectionViewModel("applicant", Some(Call("", "app")), Completed))
      when(mockPartnerSection.asViewModel(any())).thenReturn(SectionViewModel("partner", Some(Call("", "p")), Completed))
      when(mockChildSection.asViewModel(any())).thenReturn(SectionViewModel("child", Some(Call("", "child")), InProgress))
      when(mockPaymentSection.asViewModel(any())).thenReturn(SectionViewModel("payment", None, CannotStart))
      when(mockAdditionalInfoSection.asViewModel(any())).thenReturn(SectionViewModel("additional info", None, CannotStart))

      val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

      val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value

      val result = service.sections(answers)

      result mustEqual Seq(
        SectionViewModel("applicant", Some(Call("", "app")), Completed),
        SectionViewModel("partner", Some(Call("", "p")), Completed),
        SectionViewModel("child", Some(Call("", "child")), InProgress),
        SectionViewModel("payment", None, CannotStart),
        SectionViewModel("additional info", None, CannotStart)
      )
    }
  }
}
