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

package controllers.payments

import base.SpecBase
import forms.payments.WantToBePaidFormProvider
import models.Income._
import models.RelationshipStatus._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.income.{ApplicantIncomePage, ApplicantOrPartnerIncomePage}
import pages.{EmptyWaypoints, RelationshipStatusPage}
import pages.payments._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.payments._

import scala.concurrent.Future

class WantToBePaidControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new WantToBePaidFormProvider()
  private val form = formProvider()
  private val waypoints = EmptyWaypoints

  "WantToBePaid Controller" - {

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
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidCoupleUnder50kView]

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
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidCoupleUnder60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
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
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidCoupleOver60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
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
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnder50kView]

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
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnder60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
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
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleOver60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
          }
        }
      }
    }

    "must return OK and the correct, pre-populated view for a GET" - {

      "when the applicant is married or cohabiting" - {

        "and their income is between 50k and 60k" in {

          val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantOrPartnerIncomePage, BetweenThresholds).success.value
              .set(WantToBePaidPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidCoupleUnder60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
          }
        }

        "and their income is over 60k" in {

          val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantOrPartnerIncomePage, AboveUpperThreshold).success.value
              .set(WantToBePaidPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidCoupleOver60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
          }
        }
      }

      "when the applicant is single, separated, divorced or widowed" - {

        "and their income is between 50k and 60k" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, BetweenThresholds).success.value
              .set(WantToBePaidPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnder60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
          }
        }

        "and their income is over 60k" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, AboveUpperThreshold).success.value
              .set(WantToBePaidPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(GET, routes.WantToBePaidController.onPageLoad(waypoints).url)

            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleOver60kView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
          }
        }
      }
    }

    "must save the answer and redirect to the next page for a POST of valid data" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =

          FakeRequest(POST, routes.WantToBePaidController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(WantToBePaidPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual WantToBePaidPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).route.url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" - {

      "when the applicant is married or cohabiting" - {

        "and their income is between 50k and 60k" in {

          val relationshipStatus = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantOrPartnerIncomePage, BetweenThresholds).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidCoupleUnder60kView]

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
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
            val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidCoupleOver60kView]

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
          }
        }
      }

      "when the applicant is single, separated, divorced or widowed" - {

        "and their income is between 50k and 60k" in {

          val relationshipStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

          val answers =
            emptyUserAnswers
              .set(RelationshipStatusPage, relationshipStatus).success.value
              .set(ApplicantIncomePage, BetweenThresholds).success.value

          val application = applicationBuilder(userAnswers = Some(answers)).build()

          running(application) {
            val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleUnder60kView]

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
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
            val request = FakeRequest(POST, routes.WantToBePaidController.onSubmit(waypoints).url).withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val result = route(application, request).value

            val view = application.injector.instanceOf[WantToBePaidSingleOver60kView]

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
          }
        }
      }
    }
  }
}
