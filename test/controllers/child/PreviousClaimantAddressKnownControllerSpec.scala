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
import forms.child.PreviousClaimantAddressKnownFormProvider
import models.{AdultName, Done}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{PreviousClaimantAddressKnownPage, PreviousClaimantNamePage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.child.PreviousClaimantAddressKnownView

import scala.concurrent.Future

class PreviousClaimantAddressKnownControllerSpec extends SpecBase with MockitoSugar {

  private val name = AdultName(None, "first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(PreviousClaimantNamePage(index), name).success.value

  val formProvider = new PreviousClaimantAddressKnownFormProvider()
  val form = formProvider(name)
  private val waypoints = EmptyWaypoints

  lazy val previousClaimantAddressKnownRoute =
    routes.PreviousClaimantAddressKnownController.onPageLoad(waypoints, index).url

  "PreviousClaimantAddressKnown Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousClaimantAddressKnownRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PreviousClaimantAddressKnownView]

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form, waypoints, index, name)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(PreviousClaimantAddressKnownPage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousClaimantAddressKnownRoute)

        val view = application.injector.instanceOf[PreviousClaimantAddressKnownView]

        val result = route(application, request).value

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form.fill(true), waypoints, index, name)(
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
          FakeRequest(POST, previousClaimantAddressKnownRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.PreviousClaimantAddressKnownPage(index), true).success.value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` child
          .PreviousClaimantAddressKnownPage(index)
          .navigate(waypoints, emptyUserAnswers, expectedAnswers)
          .url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, previousClaimantAddressKnownRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PreviousClaimantAddressKnownView]

        val result = route(application, request).value

        status(result) `mustEqual` BAD_REQUEST
        contentAsString(result) `mustEqual` view(boundForm, waypoints, index, name)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousClaimantAddressKnownRoute)

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousClaimantAddressKnownRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
