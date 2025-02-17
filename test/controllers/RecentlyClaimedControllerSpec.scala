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

package controllers

import base.SpecBase
import com.typesafe.config.ConfigFactory
import config.FrontendAppConfig
import forms.RecentlyClaimedFormProvider
import models.{Done, ServiceType, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, RecentlyClaimedPage}
import play.api.Configuration
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
  private val defaultServiceType = ServiceType.values.head

  private lazy val recentlyClaimedRoute = routes.RecentlyClaimedController.onPageLoad(waypoints).url

  "RecentlyClaimed Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, recentlyClaimedRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RecentlyClaimedView]

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form, waypoints)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(RecentlyClaimedPage(new FrontendAppConfig(new Configuration(ConfigFactory.load()))), defaultServiceType)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, recentlyClaimedRoute)

        val view = application.injector.instanceOf[RecentlyClaimedView]

        val result = route(application, request).value

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form.fill(defaultServiceType), waypoints)(
          request,
          messages(application)
        ).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) `thenReturn` Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, recentlyClaimedRoute)
            .withFormUrlEncodedBody(("serviceType", defaultServiceType.toString))

        val result = route(application, request).value
        val config = application.injector.instanceOf[FrontendAppConfig]
        val recentlyClaimedPage = RecentlyClaimedPage(config)
        val expectedAnswers = emptyUserAnswers.set(recentlyClaimedPage, defaultServiceType).success.value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` recentlyClaimedPage
          .navigate(waypoints, emptyUserAnswers, expectedAnswers)
          .url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, recentlyClaimedRoute)
            .withFormUrlEncodedBody(("serviceType", "notAValidValue"))

        val boundForm = form.bind(Map("serviceType" -> "notAValidValueEither"))

        val view = application.injector.instanceOf[RecentlyClaimedView]

        val result = route(application, request).value

        status(result) `mustEqual` BAD_REQUEST
        contentAsString(result) `mustEqual` view(boundForm, waypoints)(request, messages(application)).toString
      }
    }

    "must return OK for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, recentlyClaimedRoute)

        val result = route(application, request).value

        status(result) `mustEqual` OK
      }
    }

    "must redirect to the next page for a POST if no existing data is found and the user is unauthenticated" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) `thenReturn` Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, recentlyClaimedRoute)
            .withFormUrlEncodedBody(("serviceType", defaultServiceType.toString))
        val config = application.injector.instanceOf[FrontendAppConfig]
        val recentlyClaimedPage = RecentlyClaimedPage(config)
        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(recentlyClaimedPage, defaultServiceType).success.value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` recentlyClaimedPage
          .navigate(waypoints, emptyUserAnswers, expectedAnswers)
          .url
      }
    }

    "must redirect to the next page for a POST if no existing data is found and the user is authenticated" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) `thenReturn` Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = None, userIsAuthenticated = true)
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, recentlyClaimedRoute)
            .withFormUrlEncodedBody(("serviceType", defaultServiceType.toString))
        val config = application.injector.instanceOf[FrontendAppConfig]
        val recentlyClaimedPage = RecentlyClaimedPage(config)
        val result = route(application, request).value

        val expectedAnswers =
          emptyUserAnswers.copy(nino = Some("nino")).set(recentlyClaimedPage, defaultServiceType).success.value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` recentlyClaimedPage
          .navigate(waypoints, emptyUserAnswers, expectedAnswers)
          .url
      }
    }
  }
}
