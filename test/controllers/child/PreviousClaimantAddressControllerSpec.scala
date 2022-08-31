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

package controllers.child

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.child.PreviousClaimantAddressFormProvider
import models.{Address, AdultName}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{PreviousClaimantAddressPage, PreviousClaimantNamePage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.child.PreviousClaimantAddressView

import scala.concurrent.Future

class PreviousClaimantAddressControllerSpec extends SpecBase with MockitoSugar {

  private val previousClaimantName = AdultName("first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(PreviousClaimantNamePage(index), previousClaimantName).success.value

  val formProvider = new PreviousClaimantAddressFormProvider()
  val form = formProvider()
  private val waypoints = EmptyWaypoints

  lazy val previousClaimantAddressRoute = routes.PreviousClaimantAddressController.onPageLoad(waypoints, index).url

  private val validAnswer = Address("line 1", None, "town", None, "postcode")
  private val userAnswers = baseAnswers.set(PreviousClaimantAddressPage(index), validAnswer).success.value

  "PreviousClaimantAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousClaimantAddressRoute)

        val view = application.injector.instanceOf[PreviousClaimantAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, previousClaimantName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousClaimantAddressRoute)

        val view = application.injector.instanceOf[PreviousClaimantAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints, index, previousClaimantName)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, previousClaimantAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town" -> "town"), ("postcode", "postcode"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.PreviousClaimantAddressPage(index), validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual child.PreviousClaimantAddressPage(index).navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, previousClaimantAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PreviousClaimantAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, previousClaimantName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousClaimantAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousClaimantAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town" -> "town"), ("postcode", "postcode"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
