/*
 * Copyright 2024 HM Revenue & Customs
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
import pages.partner.RelationshipStatusPage
import pages.EmptyWaypoints
import pages.payments.{ApplicantIncomePage, CheckPaymentDetailsPage, PartnerIncomePage, WantToBePaidPage}
import play.api.test.Helpers._
import play.api.i18n.Messages

class WantToBePaidSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".row" - {

    "when the applicant does not have a partner" - {

      val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

      "must return Some when the applicant's income is between the thresholds" in {

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, BetweenThresholds)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `mustBe` defined
      }

      "must return Some when the applicant's income is above the upper threshold" in {

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, AboveUpperThreshold)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `mustBe` defined
      }

      "must return None when the applicant's income is below the lower threshold" in {

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, BelowLowerThreshold)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `must` not `be` defined
      }
    }

    "when the applicant has a partner" - {

      val relationship = Gen.oneOf(Married, Cohabiting).sample.value

      "must return Some when the applicant's income is between the thresholds" in {

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, BetweenThresholds)
            .success
            .value
            .set(PartnerIncomePage, BelowLowerThreshold)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `mustBe` defined
      }

      "must return Some when the applicant's income is above the upper threshold" in {

        val relationship = Gen.oneOf(Married, Cohabiting).sample.value

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, AboveUpperThreshold)
            .success
            .value
            .set(PartnerIncomePage, BelowLowerThreshold)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `mustBe` defined
      }

      "must return Some when the partner's income is between the thresholds" in {

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, BelowLowerThreshold)
            .success
            .value
            .set(PartnerIncomePage, BetweenThresholds)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `mustBe` defined
      }

      "must return Some when the partner's income is above the upper threshold" in {

        val relationship = Gen.oneOf(Married, Cohabiting).sample.value

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, BelowLowerThreshold)
            .success
            .value
            .set(PartnerIncomePage, AboveUpperThreshold)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `mustBe` defined
      }

      "must return None when the applicant and their partner's income are both below the lower threshold" in {

        val relationship = Gen.oneOf(Married, Cohabiting).sample.value

        val answers =
          UserAnswers("id")
            .set(RelationshipStatusPage, relationship)
            .success
            .value
            .set(ApplicantIncomePage, BelowLowerThreshold)
            .success
            .value
            .set(PartnerIncomePage, BelowLowerThreshold)
            .success
            .value
            .set(WantToBePaidPage, true)
            .success
            .value

        WantToBePaidSummary.row(answers, EmptyWaypoints, CheckPaymentDetailsPage) `must` not `be` defined
      }
    }
  }
}
