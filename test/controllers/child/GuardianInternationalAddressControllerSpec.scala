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
import forms.child.GuardianInternationalAddressFormProvider
import models.{AdultName, Country, Done, InternationalAddress}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{GuardianInternationalAddressPage, GuardianNamePage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.child.GuardianInternationalAddressView

import scala.concurrent.Future

class GuardianInternationalAddressControllerSpec extends SpecBase with MockitoSugar {

  private val guardianName = AdultName(None, "first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(GuardianNamePage(index), guardianName).success.value

  val formProvider = new GuardianInternationalAddressFormProvider()
  val form = formProvider(guardianName)
  private val waypoints = EmptyWaypoints

  lazy val guardianInternationalAddressRoute = routes.GuardianInternationalAddressController.onPageLoad(waypoints, index).url

  private val country     = Country.internationalCountries.head
  private val validAnswer = InternationalAddress("line 1", None, "town", None, Some("AA111AA"), country)
  private val userAnswers = baseAnswers.set(GuardianInternationalAddressPage(index), validAnswer).success.value

  "GuardianInternationalAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, guardianInternationalAddressRoute)

        val view = application.injector.instanceOf[GuardianInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, guardianName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, guardianInternationalAddressRoute)

        val view = application.injector.instanceOf[GuardianInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints, index, guardianName)(request, messages(application)).toString
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
          FakeRequest(POST, guardianInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town" -> "town"), ("postcode", "AA111AA"), ("country", country.code))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.GuardianInternationalAddressPage(index), validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual child.GuardianInternationalAddressPage(index).navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, guardianInternationalAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[GuardianInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, guardianName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, guardianInternationalAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, guardianInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town" -> "town"), ("postcode", "AA111AA"), ("country", country.code))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
