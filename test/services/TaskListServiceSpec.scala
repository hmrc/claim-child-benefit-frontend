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

import controllers.applicant.{routes => applicantRoutes}
import controllers.routes
import models.RelationshipStatus._
import models.{Index, UserAnswers}
import models.tasklist._
import models.tasklist.SectionStatus.{CannotStart, Completed, InProgress, NotStarted}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AlwaysLivedInUkPage, ApplicantNamePage, CheckRelationshipDetailsPage, EmptyWaypoints, RecentlyClaimedPage, RelationshipStatusPage}
import pages.applicant.{ApplicantNinoKnownPage, ApplicantNinoPage, CheckApplicantDetailsPage}
import pages.child.ChildNamePage
import pages.income.ApplicantIncomePage
import pages.partner.PartnerNamePage
import play.api.mvc.Call

class TaskListServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with TryValues with OptionValues with BeforeAndAfterEach {

  private val mockRelationshipSection = mock[RelationshipSection]
  private val mockApplicantSection = mock[ApplicantSection]
  private val mockChildSection = mock[ChildSection]
  private val mockPartnerSection = mock[PartnerSection]
  private val mockPaymentSection = mock[PaymentSection]
  private val mockAdditionalInfoSection = mock[AdditionalInfoSection]

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRelationshipSection,
      mockApplicantSection,
      mockChildSection,
      mockPartnerSection,
      mockPaymentSection,
      mockAdditionalInfoSection
    )
    super.beforeEach()
  }

  private implicit val arbitraryProgress: Arbitrary[SectionStatus] = Arbitrary {
    Gen.oneOf(Completed, InProgress, NotStarted)
  }

  ".sections" - {

    "must return all sections that should be shown in the correct order" in {

      val service = new TaskListService(
        mockRelationshipSection,
        mockApplicantSection,
        mockPartnerSection,
        mockChildSection,
        mockPaymentSection,
        mockAdditionalInfoSection
      )

      when(mockRelationshipSection.asViewModel(any())).thenReturn(Some(SectionViewModel("relationship", Some(Call("", "rel")), Completed)))
      when(mockApplicantSection.asViewModel(any())).thenReturn(Some(SectionViewModel("applicant", Some(Call("", "app")), Completed)))
      when(mockPartnerSection.asViewModel(any())).thenReturn(None)
      when(mockChildSection.asViewModel(any())).thenReturn(Some(SectionViewModel("child", Some(Call("", "child")), InProgress)))
      when(mockPaymentSection.asViewModel(any())).thenReturn(Some(SectionViewModel("payment", None, CannotStart)))
      when(mockAdditionalInfoSection.asViewModel(any())).thenReturn(Some(SectionViewModel("additional info", None, CannotStart)))

      val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

      val answers = UserAnswers("id").set(RelationshipStatusPage, relationshipStatus).success.value

      val result = service.sections(answers)

      result mustEqual Seq(
        SectionViewModel("relationship", Some(Call("", "rel")), Completed),
        SectionViewModel("applicant", Some(Call("", "app")), Completed),
        SectionViewModel("child", Some(Call("", "child")), InProgress),
        SectionViewModel("payment", None, CannotStart),
        SectionViewModel("additional info", None, CannotStart)
      )
    }
  }
}
