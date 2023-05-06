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

package controllers

import base.SpecBase
import models.Income.{AboveUpperThreshold, BelowLowerThreshold, BetweenThresholds}
import models.RelationshipStatus._
import models.TaxChargePayer
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.partner.RelationshipStatusPage
import pages.payments.{ApplicantIncomePage, PartnerIncomePage, WantToBePaidPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.{SubmittedNoTaxChargeView, SubmittedWithTaxChargeView}

class SubmittedControllerSpec extends SpecBase {

  "Submitted Controller" - {

    "must return OK and the correct view for a GET" - {

      "when the applicant is single, separated, divorced or widowed" - {

        val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value
        val wantToBePaid = arbitrary[Boolean].sample.value

        "and their income is below the lower threshold" in {

          val answers =
            emptyUserAnswers
              .set(WantToBePaidPage, wantToBePaid).success.value
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, BelowLowerThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[SubmittedNoTaxChargeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(hasPartner = false)(request, messages(application)).toString
          }
        }

        "and their income is above the lower threshold" in {

          val income = Gen.oneOf(BetweenThresholds, AboveUpperThreshold).sample.value

          val answers =
            emptyUserAnswers
              .set(WantToBePaidPage, wantToBePaid).success.value
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, income).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[SubmittedWithTaxChargeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(wantToBePaid, hasPartner = false, TaxChargePayer.Applicant)(request, messages(application)).toString
          }
        }
      }

      "when the applicant is married or cohabiting" - {

        val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value
        val wantToBePaid = arbitrary[Boolean].sample.value

        "and their income is below the lower threshold" - {

          val applicantIncome = BelowLowerThreshold

          "and their partner's income is below the lower threshold" in {

            val partnerIncome = BelowLowerThreshold

            val answers =
              emptyUserAnswers
                .set(WantToBePaidPage, wantToBePaid).success.value
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, applicantIncome).success.value
                .set(PartnerIncomePage, partnerIncome).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SubmittedNoTaxChargeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(hasPartner = true)(request, messages(application)).toString
            }
          }

          "and their partner's income is above the lower threshold" in {

            val partnerIncome = Gen.oneOf(BetweenThresholds, AboveUpperThreshold).sample.value

            val answers =
              emptyUserAnswers
                .set(WantToBePaidPage, wantToBePaid).success.value
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, applicantIncome).success.value
                .set(PartnerIncomePage, partnerIncome).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SubmittedWithTaxChargeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(wantToBePaid, hasPartner = true, TaxChargePayer.Partner)(request, messages(application)).toString
            }
          }
        }

        "and their income is between the thresholds" - {

          val applicantIncome = BetweenThresholds

          "and their partner's income is below the lower threshold" in {

            val partnerIncome = BelowLowerThreshold

            val answers =
              emptyUserAnswers
                .set(WantToBePaidPage, wantToBePaid).success.value
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, applicantIncome).success.value
                .set(PartnerIncomePage, partnerIncome).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SubmittedWithTaxChargeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(wantToBePaid, hasPartner = true, TaxChargePayer.Applicant)(request, messages(application)).toString
            }
          }

          "and their partner's income is between the thresholds" in {

            val partnerIncome = BetweenThresholds

            val answers =
              emptyUserAnswers
                .set(WantToBePaidPage, wantToBePaid).success.value
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, applicantIncome).success.value
                .set(PartnerIncomePage, partnerIncome).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SubmittedWithTaxChargeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(wantToBePaid, hasPartner = true, TaxChargePayer.ApplicantOrPartner)(request, messages(application)).toString
            }
          }

          "and their partner's income is above the lower threshold" in {

            val partnerIncome = AboveUpperThreshold

            val answers =
              emptyUserAnswers
                .set(WantToBePaidPage, wantToBePaid).success.value
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, applicantIncome).success.value
                .set(PartnerIncomePage, partnerIncome).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SubmittedWithTaxChargeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(wantToBePaid, hasPartner = true, TaxChargePayer.Partner)(request, messages(application)).toString
            }
          }
        }

        "and their income is above the upper threshold" - {

          val applicantIncome = AboveUpperThreshold

          "and their partner's income is below the upper threshold" in {

            val partnerIncome = Gen.oneOf(BelowLowerThreshold, BetweenThresholds).sample.value

            val answers =
              emptyUserAnswers
                .set(WantToBePaidPage, wantToBePaid).success.value
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, applicantIncome).success.value
                .set(PartnerIncomePage, partnerIncome).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SubmittedWithTaxChargeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(wantToBePaid, hasPartner = true, TaxChargePayer.Applicant)(request, messages(application)).toString
            }
          }

          "and their partner's income is above the upper threshold" in {

            val partnerIncome = AboveUpperThreshold

            val answers =
              emptyUserAnswers
                .set(WantToBePaidPage, wantToBePaid).success.value
                .set(RelationshipStatusPage, relationshipStatus).success.value
                .set(ApplicantIncomePage, applicantIncome).success.value
                .set(PartnerIncomePage, partnerIncome).success.value

            val application = applicationBuilder(userAnswers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

              val result = route(application, request).value

              val view = application.injector.instanceOf[SubmittedWithTaxChargeView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(wantToBePaid, hasPartner = true, TaxChargePayer.ApplicantOrPartner)(request, messages(application)).toString
            }
          }
        }

      }
    }
  }
}