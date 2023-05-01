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
import forms.applicant.AddCountryApplicantReceivedBenefitsFormProvider
import models.Done
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.applicant.AddCountryApplicantReceivedBenefitsPage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import viewmodels.checkAnswers.applicant.AddCountryApplicantReceivedBenefitsSummary
import views.html.applicant.AddCountryApplicantReceivedBenefitsView

import scala.concurrent.Future

class AddCountryApplicantReceivedBenefitsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddCountryApplicantReceivedBenefitsFormProvider()
  private val form = formProvider()
  private val waypoints = EmptyWaypoints

  private lazy val addCountryApplicantReceivedBenefitsRoute = routes.AddCountryApplicantReceivedBenefitsController.onPageLoad(waypoints).url

  "AddCountryApplicantReceivedBenefits Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addCountryApplicantReceivedBenefitsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddCountryApplicantReceivedBenefitsView]

        implicit val msgs: Messages = messages(application)
        val nationalities = AddCountryApplicantReceivedBenefitsSummary.rows(emptyUserAnswers, waypoints, AddCountryApplicantReceivedBenefitsPage())

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, nationalities)(request, implicitly).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addCountryApplicantReceivedBenefitsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(AddCountryApplicantReceivedBenefitsPage(), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AddCountryApplicantReceivedBenefitsPage().navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addCountryApplicantReceivedBenefitsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddCountryApplicantReceivedBenefitsView]

        implicit val msgs: Messages = messages(application)
        val nationalities = AddCountryApplicantReceivedBenefitsSummary.rows(emptyUserAnswers, waypoints, AddCountryApplicantReceivedBenefitsPage())

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, nationalities)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addCountryApplicantReceivedBenefitsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addCountryApplicantReceivedBenefitsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
