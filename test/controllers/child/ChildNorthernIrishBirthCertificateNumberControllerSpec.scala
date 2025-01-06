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

package controllers.child

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.child.ChildNorthernIrishBirthCertificateNumberFormProvider
import models.{ChildName, Done, NorthernIrishBirthCertificateNumber}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{ChildNamePage, ChildNorthernIrishBirthCertificateNumberPage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.child.ChildNorthernIrishBirthCertificateNumberView

import scala.concurrent.Future

class ChildNorthernIrishBirthCertificateNumberControllerSpec extends SpecBase with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val childName = ChildName("first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(ChildNamePage(index), childName).success.value
  private val validAnswer = NorthernIrishBirthCertificateNumber("B12345678")

  private val formProvider = new ChildNorthernIrishBirthCertificateNumberFormProvider()
  private val form = formProvider(childName)

  private lazy val childNorthernIrishBirthCertificateNumberRoute =
    routes.ChildNorthernIrishBirthCertificateNumberController.onPageLoad(waypoints, index).url

  "ChildNorthernIrishBirthCertificateNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, childNorthernIrishBirthCertificateNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChildNorthernIrishBirthCertificateNumberView]

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form, waypoints, index, childName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(ChildNorthernIrishBirthCertificateNumberPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, childNorthernIrishBirthCertificateNumberRoute)

        val view = application.injector.instanceOf[ChildNorthernIrishBirthCertificateNumberView]

        val result = route(application, request).value

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form.fill(validAnswer), waypoints, index, childName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) `thenReturn` Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, childNorthernIrishBirthCertificateNumberRoute)
            .withFormUrlEncodedBody(("value", validAnswer.value))

        val result = route(application, request).value
        val expectedAnswers =
          baseAnswers.set(child.ChildNorthernIrishBirthCertificateNumberPage(index), validAnswer).success.value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` child
          .ChildNorthernIrishBirthCertificateNumberPage(index)
          .navigate(waypoints, emptyUserAnswers, expectedAnswers)
          .url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, childNorthernIrishBirthCertificateNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ChildNorthernIrishBirthCertificateNumberView]

        val result = route(application, request).value

        status(result) `mustEqual` BAD_REQUEST
        contentAsString(result) `mustEqual` view(boundForm, waypoints, index, childName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, childNorthernIrishBirthCertificateNumberRoute)

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, childNorthernIrishBirthCertificateNumberRoute)
            .withFormUrlEncodedBody(("value", validAnswer.value))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
