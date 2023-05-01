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

package controllers.auth

import base.SpecBase
import models.{Done, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{EmptyWaypoints, TaskListPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.auth.{UnsupportedAffinityGroupAgentView, UnsupportedAffinityGroupOrganisationView}

import java.time.Instant
import scala.concurrent.Future

class AuthControllerSpec extends SpecBase with MockitoSugar {

  "signOut" - {

    "when the user is unauthenticated" - {

      "must clear user answers and redirect to application reset" in {

        val mockUserDataService = mock[UserDataService]
        when(mockUserDataService.clear(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(None)
            .overrides(bind[UserDataService].toInstance(mockUserDataService))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.AuthController.signOut.url)

          val result = route(application, request).value

          val expectedRedirectUrl = routes.ApplicationResetController.onPageLoad.url

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual expectedRedirectUrl
          verify(mockUserDataService, times(1)).clear(eqTo(userAnswersId))
        }
      }
    }

    "when the user is authenticated" - {

      "must clear user answers and redirect to BAS sign-out" in {

        val mockUserDataService = mock[UserDataService]
        when(mockUserDataService.clear(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(None, userIsAuthenticated = true)
            .overrides(bind[UserDataService].toInstance(mockUserDataService))
            .build()

        running(application) {

          val request = FakeRequest(GET, routes.AuthController.signOut.url)

          val result = route(application, request).value

          val expectedRedirectUrl = "http://localhost:9553/bas-gateway/sign-out-without-state?continue=http%3A%2F%2Flocalhost%3A11303%2Ffill-online%2Fclaim-child-benefit%2Fapplication-form-has-been-reset"

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual expectedRedirectUrl
          verify(mockUserDataService, times(1)).clear(eqTo(userAnswersId))
        }
      }
    }
  }

  "unsupportedAffinityGroupAgent" - {

    "must return Ok and the correct view" in {

      val application = applicationBuilder(None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedAffinityGroupAgent("continueUrl").url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[UnsupportedAffinityGroupAgentView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("continueUrl")(request, messages(application)).toString
      }
    }
  }

  "unsupportedAffinityGroupOrganisation" - {

    "must return Ok and the correct view" in {

      val application = applicationBuilder(None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedAffinityGroupOrganisation("continueUrl").url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[UnsupportedAffinityGroupOrganisationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("continueUrl")(request, messages(application)).toString
      }
    }
  }

  "redirectToRegister" - {

    "must Redirect to bas-gateway register" in {

      val application = applicationBuilder(None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.redirectToRegister("continueUrl").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/register?origin=CHB&continueUrl=continueUrl&accountType=Individual"
      }
    }
  }

  "redirectToLogin" - {

    "must Redirect to bas-gateway login" in {

      val application = applicationBuilder(None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.redirectToLogin("continueUrl").url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-in?origin=CHB&continue=continueUrl"
      }
    }
  }

  "signedIn" - {

    "must redirect to the task list when the user already has user answers" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.signedIn().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TaskListPage.route(EmptyWaypoints).url
      }
    }

    "must save an empty UserAnswers then redirect to the task list when the user does not have any user answers" in {

      val mockUserDataService = mock[UserDataService]
      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(None)
          .overrides(bind[UserDataService].toInstance(mockUserDataService))
          .build()

      running(application) {

        val request = FakeRequest(GET, routes.AuthController.signedIn().url)
        val result = route(application, request).value
        val expectedAnswers = UserAnswers(userAnswersId, lastUpdated = Instant.now(clockAtFixedInstant))

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TaskListPage.route(EmptyWaypoints).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }
  }

  "signOutAndApplyUnauthenticated" - {

    "must sign a user out and continue at the start of the service" in {

      val application = applicationBuilder(None).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.signOutAndApplyUnauthenticated().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-out-without-state?continue=http%3A%2F%2Flocalhost%3A11303%2Ffill-online%2Fclaim-child-benefit%2Frecently-claimed-child-benefit"
      }
    }
  }
}
