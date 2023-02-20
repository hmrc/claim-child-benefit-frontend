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

package controllers

import base.SpecBase
import config.FeatureFlags
import forms.RecentlyClaimedFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, RecentlyClaimedPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.RecentlyClaimedView

import scala.concurrent.Future

class RecentlyClaimedControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new RecentlyClaimedFormProvider()
  private val form = formProvider()
  private val waypoints = EmptyWaypoints
  private val mockFeatureFlags = mock[FeatureFlags]
  private val recentlyClaimedPage = new RecentlyClaimedPage(mockFeatureFlags)
  when(mockFeatureFlags.allowAuthenticatedSessions) thenReturn false

  private lazy val anyChildLivedWithOthersRoute = routes.RecentlyClaimedController.onPageLoad(waypoints).url

  "RecentlyClaimed Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, anyChildLivedWithOthersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RecentlyClaimedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(recentlyClaimedPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, anyChildLivedWithOthersRoute)

        val view = application.injector.instanceOf[RecentlyClaimedView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), waypoints)(request, messages(application)).toString
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
          FakeRequest(POST, anyChildLivedWithOthersRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(recentlyClaimedPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recentlyClaimedPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, anyChildLivedWithOthersRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RecentlyClaimedView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints)(request, messages(application)).toString
      }
    }

    "must return OK for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, anyChildLivedWithOthersRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to the next page for a POST if no existing data is found" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, anyChildLivedWithOthersRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(recentlyClaimedPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual recentlyClaimedPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).url
      }
    }
  }
}
