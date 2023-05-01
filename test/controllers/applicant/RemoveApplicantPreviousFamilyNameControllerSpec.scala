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
import forms.applicant.RemoveApplicantPreviousFamilyNameFormProvider
import models.{ApplicantPreviousName, Done}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.applicant.{ApplicantPreviousFamilyNamePage, RemoveApplicantPreviousFamilyNamePage}
import pages.{EmptyWaypoints, applicant}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.applicant.RemoveApplicantPreviousFamilyNameView

import scala.concurrent.Future

class RemoveApplicantPreviousFamilyNameControllerSpec extends SpecBase with MockitoSugar {

  private val name = ApplicantPreviousName("name")

  val formProvider = new RemoveApplicantPreviousFamilyNameFormProvider()
  val form = formProvider(name.lastName)
  private val waypoints = EmptyWaypoints
  private val baseAnswers = emptyUserAnswers.set(ApplicantPreviousFamilyNamePage(index), name).success.value

  lazy val removeApplicantPreviousFamilyNameRoute = routes.RemoveApplicantPreviousFamilyNameController.onPageLoad(waypoints, index).url

  "RemoveApplicantPreviousFamilyName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeApplicantPreviousFamilyNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveApplicantPreviousFamilyNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, name.lastName)(request, messages(application)).toString
      }
    }

    "must remove the previous name and redirect to the next page when the answer is yes" in {

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
          FakeRequest(POST, removeApplicantPreviousFamilyNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.remove(applicant.ApplicantPreviousFamilyNamePage(index)).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveApplicantPreviousFamilyNamePage(index).navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must not remove the previous name and redirect to the next page when the answer is no" in {

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
          FakeRequest(POST, removeApplicantPreviousFamilyNameRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual applicant.RemoveApplicantPreviousFamilyNamePage(index).navigate(waypoints, baseAnswers, baseAnswers).url
        verify(mockUserDataService, never()).set(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeApplicantPreviousFamilyNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveApplicantPreviousFamilyNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, name.lastName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeApplicantPreviousFamilyNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeApplicantPreviousFamilyNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
