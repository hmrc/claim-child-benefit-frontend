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

package controllers.payments

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.payments.ApplicantIncomeFormProvider
import models.RelationshipStatus._
import models.{Done, Income}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.partner.RelationshipStatusPage
import pages.payments.ApplicantIncomePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.payments.ApplicantIncomeView

import scala.concurrent.Future

class ApplicantIncomeControllerSpec extends SpecBase with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private lazy val applicantIncomeRoute = routes.ApplicantIncomeController.onPageLoad(waypoints).url

  private val formProvider = new ApplicantIncomeFormProvider()
  private val form = formProvider()

  private val singleStatus = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value
  private val coupleStatus = Gen.oneOf(Married, Cohabiting).sample.value
  private val singleAnswers = emptyUserAnswers.set(RelationshipStatusPage, singleStatus).success.value
  private val coupleAnswers = emptyUserAnswers.set(RelationshipStatusPage, coupleStatus).success.value

  "ApplicantIncome Controller" - {

    "must return OK and the correct view for a GET when the user has a partner" in {

      val application = applicationBuilder(userAnswers = Some(coupleAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, applicantIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ApplicantIncomeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, waypoints, hasPartner = true)(request, messages(application), clockAtFixedInstant).toString
      }
    }

    "must return OK and the correct view for a GET when the user does not have a partner" in {

      val application = applicationBuilder(userAnswers = Some(singleAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, applicantIncomeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ApplicantIncomeView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, waypoints, hasPartner = false)(request, messages(application), clockAtFixedInstant).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = singleAnswers.set(ApplicantIncomePage, Income.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, applicantIncomeRoute)

        val view = application.injector.instanceOf[ApplicantIncomeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(Income.values.head), waypoints, hasPartner = false)(request, messages(application), clockAtFixedInstant).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(coupleAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, applicantIncomeRoute)
            .withFormUrlEncodedBody(("value", Income.values.head.toString))

        val result = route(application, request).value
        val expectedAnswers = coupleAnswers.set(ApplicantIncomePage, Income.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual ApplicantIncomePage.navigate(waypoints, coupleAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(singleAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, applicantIncomeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[ApplicantIncomeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, hasPartner = false)(request, messages(application), clockAtFixedInstant).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, applicantIncomeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, applicantIncomeRoute)
            .withFormUrlEncodedBody(("value[0]", Income.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
