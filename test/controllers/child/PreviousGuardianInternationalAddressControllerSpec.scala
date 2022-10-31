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
import forms.child.PreviousGuardianInternationalAddressFormProvider
import models.{AdultName, Country, InternationalAddress}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{PreviousGuardianInternationalAddressPage, PreviousGuardianNamePage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.child.PreviousGuardianInternationalAddressView

import scala.concurrent.Future

class PreviousGuardianInternationalAddressControllerSpec extends SpecBase with MockitoSugar {

  private val previousGuardianName = AdultName("first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(PreviousGuardianNamePage(index), previousGuardianName).success.value

  val formProvider = new PreviousGuardianInternationalAddressFormProvider()
  val form = formProvider(previousGuardianName)
  private val waypoints = EmptyWaypoints

  lazy val previousGuardianInternationalAddressRoute = routes.PreviousGuardianInternationalAddressController.onPageLoad(waypoints, index).url

  private val country     = Country.internationalCountries.head
  private val validAnswer = InternationalAddress("line 1", None, "town", None, Some("postcode"), country)
  private val userAnswers = baseAnswers.set(PreviousGuardianInternationalAddressPage(index), validAnswer).success.value

  "PreviousGuardianInternationalAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousGuardianInternationalAddressRoute)

        val view = application.injector.instanceOf[PreviousGuardianInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, previousGuardianName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousGuardianInternationalAddressRoute)

        val view = application.injector.instanceOf[PreviousGuardianInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints, index, previousGuardianName)(request, messages(application)).toString
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
          FakeRequest(POST, previousGuardianInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town" -> "town"), ("postcode", "postcode"), ("country", country.code))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.PreviousGuardianInternationalAddressPage(index), validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual child.PreviousGuardianInternationalAddressPage(index).navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, previousGuardianInternationalAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PreviousGuardianInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, previousGuardianName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousGuardianInternationalAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousGuardianInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town" -> "town"), ("postcode", "postcode"), ("country", country.code))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
