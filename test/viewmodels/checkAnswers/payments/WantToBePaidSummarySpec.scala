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

package viewmodels.checkAnswers.payments

import models.Income._
import models.RelationshipStatus._
import models.UserAnswers
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.{CheckYourAnswersPage, EmptyWaypoints, RelationshipStatusPage}
import pages.income.{ApplicantIncomePage, ApplicantOrPartnerIncomePage}
import pages.payments.WantToBePaidPage
import play.api.test.Helpers._
import play.api.i18n.Messages

class WantToBePaidSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".row" - {

    "must return Some when the applicant's income is between the thresholds" in {

      val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, relationship).success.value
          .set(ApplicantIncomePage, BetweenThresholds).success.value
          .set(WantToBePaidPage, true).success.value

      WantToBePaidSummary.row(answers, EmptyWaypoints, CheckYourAnswersPage) mustBe defined
    }

    "must return Some when the applicant's income is above the upper threshold" in {

      val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, relationship).success.value
          .set(ApplicantIncomePage, AboveUpperThreshold).success.value
          .set(WantToBePaidPage, true).success.value

      WantToBePaidSummary.row(answers, EmptyWaypoints, CheckYourAnswersPage) mustBe defined
    }

    "must return None when the applicant's income is below the lower threshold" in {

      val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, relationship).success.value
          .set(ApplicantIncomePage, BelowLowerThreshold).success.value
          .set(WantToBePaidPage, true).success.value

      WantToBePaidSummary.row(answers, EmptyWaypoints, CheckYourAnswersPage) must not be defined
    }

    "must return Some when the applicant or their partner's income is between the thresholds" in {

      val relationship = Gen.oneOf(Married, Cohabiting).sample.value

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, relationship).success.value
          .set(ApplicantOrPartnerIncomePage, BetweenThresholds).success.value
          .set(WantToBePaidPage, true).success.value

      WantToBePaidSummary.row(answers, EmptyWaypoints, CheckYourAnswersPage) mustBe defined
    }

    "must return Some when the applicant or their partner's income is above the upper threshold" in {

      val relationship = Gen.oneOf(Married, Cohabiting).sample.value

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, relationship).success.value
          .set(ApplicantOrPartnerIncomePage, AboveUpperThreshold).success.value
          .set(WantToBePaidPage, true).success.value

      WantToBePaidSummary.row(answers, EmptyWaypoints, CheckYourAnswersPage) mustBe defined
    }

    "must return None when the applicant or their partner's income is below the lower threshold" in {

      val relationship = Gen.oneOf(Married, Cohabiting).sample.value

      val answers =
        UserAnswers("id")
          .set(RelationshipStatusPage, relationship).success.value
          .set(ApplicantOrPartnerIncomePage, BelowLowerThreshold).success.value
          .set(WantToBePaidPage, true).success.value

      WantToBePaidSummary.row(answers, EmptyWaypoints, CheckYourAnswersPage) must not be defined
    }
  }
}
