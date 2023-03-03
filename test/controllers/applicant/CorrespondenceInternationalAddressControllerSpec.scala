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

package controllers.applicant

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.applicant.CorrespondenceInternationalAddressFormProvider
import models.{Country, InternationalAddress}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.applicant.CorrespondenceInternationalAddressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.applicant.CorrespondenceInternationalAddressView

import scala.concurrent.Future

class CorrespondenceInternationalAddressControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new CorrespondenceInternationalAddressFormProvider()
  val form = formProvider()
  private val waypoints = EmptyWaypoints

  lazy val correspondenceInternationalAddressRoute = routes.CorrespondenceInternationalAddressController.onPageLoad(waypoints).url

  private val country     = Country.internationalCountries.head
  private val validAnswer = InternationalAddress("line 1", None, "town", None, Some("AA11 1AA"), country)
  private val userAnswers = emptyUserAnswers.set(CorrespondenceInternationalAddressPage, validAnswer).success.value

  "CorrespondenceAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, correspondenceInternationalAddressRoute)

        val view = application.injector.instanceOf[CorrespondenceInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, correspondenceInternationalAddressRoute)

        val view = application.injector.instanceOf[CorrespondenceInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, correspondenceInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town", "town"), ("postcode", "AA11 1AA"), ("country", country.code))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(CorrespondenceInternationalAddressPage, validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CorrespondenceInternationalAddressPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, correspondenceInternationalAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CorrespondenceInternationalAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, correspondenceInternationalAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, correspondenceInternationalAddressRoute)
            .withFormUrlEncodedBody(("line1", "line 1"), ("town", "town"), ("postcode", "AA11 1AA"), ("country", country.code))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
