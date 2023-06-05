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

package controllers.child

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.child.PreviousGuardianPhoneNumberFormProvider
import models.{AdultName, Done}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{PreviousGuardianNamePage, PreviousGuardianPhoneNumberPage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.child.PreviousGuardianPhoneNumberView

import scala.concurrent.Future

class PreviousGuardianPhoneNumberControllerSpec extends SpecBase with MockitoSugar {

  private val previousGuardianName = AdultName(None, "first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(PreviousGuardianNamePage(index), previousGuardianName).success.value

  val formProvider = new PreviousGuardianPhoneNumberFormProvider()
  val form = formProvider(previousGuardianName)
  private val waypoints = EmptyWaypoints

  lazy val previousGuardianPhoneNumberRoute = routes.PreviousGuardianPhoneNumberController.onPageLoad(waypoints, index).url

  private val validAnswer = "0777 777777"
  private val userAnswers = baseAnswers.set(PreviousGuardianPhoneNumberPage(index), validAnswer).success.value

  "PreviousGuardianPhoneNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousGuardianPhoneNumberRoute)

        val view = application.injector.instanceOf[PreviousGuardianPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, previousGuardianName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousGuardianPhoneNumberRoute)

        val view = application.injector.instanceOf[PreviousGuardianPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints, index, previousGuardianName)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, previousGuardianPhoneNumberRoute)
            .withFormUrlEncodedBody(("value" -> validAnswer))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.PreviousGuardianPhoneNumberPage(index), validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual child.PreviousGuardianPhoneNumberPage(index).navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, previousGuardianPhoneNumberRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PreviousGuardianPhoneNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, previousGuardianName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousGuardianPhoneNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousGuardianPhoneNumberRoute)
            .withFormUrlEncodedBody("value" -> "invalid value")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
