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

package controllers.income

import base.SpecBase
import models.Income._
import models.RelationshipStatus._
import org.scalacheck.Gen
import pages.{EmptyWaypoints, RelationshipStatusPage}
import pages.income._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.income._

class TaxChargeExplanationControllerSpec extends SpecBase {

  private val waypoints = EmptyWaypoints

  "TaxChargeExplanation Controller" - {

    "must return OK and the correct view for a GET" - {

      "when the applicant is married or cohabiting" - {

        "and their income is under 50k" in {

          val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantOrPartnerIncomePage, BelowLowerThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.TaxChargeExplanationController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[TaxChargeExplanationCoupleUnder50kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
          }
        }

        "and their income is between 50k and 60k" in {

          val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantOrPartnerIncomePage, BetweenThresholds).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.TaxChargeExplanationController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[TaxChargeExplanationCoupleUnder60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
          }
        }

        "and their income is over 60k" in {

          val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantOrPartnerIncomePage, AboveUpperThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.TaxChargeExplanationController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[TaxChargeExplanationCoupleOver60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
          }
        }
      }

      "when the applicant is single, separated, divorced or widowed" - {

        "and their income is under 50k" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, BelowLowerThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.TaxChargeExplanationController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[TaxChargeExplanationSingleUnder50kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
          }
        }

        "and their income is between 50k and 60k" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, BetweenThresholds).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.TaxChargeExplanationController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[TaxChargeExplanationSingleUnder60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
          }
        }

        "and their income is over 60k" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, AboveUpperThreshold).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.TaxChargeExplanationController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[TaxChargeExplanationSingleOver60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(waypoints)(request, messages(application)).toString
          }
        }
      }
    }

    "must redirect to the next page for a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.TaxChargeExplanationController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TaxChargeExplanationPage.navigate(waypoints, emptyUserAnswers, emptyUserAnswers).route.url
      }
    }
  }
}
