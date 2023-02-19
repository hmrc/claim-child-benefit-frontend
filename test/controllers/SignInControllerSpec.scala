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
import controllers.auth.{routes => authRoutes}
import config.FrontendAppConfig
import forms.SignInFormProvider
import pages.{EmptyWaypoints, TaskListPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.SignInView

class SignInControllerSpec extends SpecBase {

  private lazy val signInRoute = routes.SignInController.onPageLoad(EmptyWaypoints).url
  private val formProvider = new SignInFormProvider()
  private val form = formProvider()

  "SignIn Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(None).build()

      running(application) {

        val request = FakeRequest(GET, signInRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[SignInView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, EmptyWaypoints)(request, messages(application)).toString
      }
    }

    "must redirect to sign in when `true` is submitted" in {

      val application = applicationBuilder(None).build()

      running(application) {

        val request = FakeRequest(POST, signInRoute).withFormUrlEncodedBody("value" -> "true")
        val result = route(application, request).value
        val config = application.injector.instanceOf[FrontendAppConfig]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual authRoutes.AuthController.redirectToLogin(config.loginContinueUrl).url
      }
    }

    "must redirect to the task list when `false` is submitted" in {

      val application = applicationBuilder(None).build()

      running(application) {

        val request = FakeRequest(POST, signInRoute).withFormUrlEncodedBody("value" -> "false")
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual TaskListPage.route(EmptyWaypoints).url
      }
    }
    
    "must return a bad request when invalid data is submitted" in {

      val application = applicationBuilder(None).build()

      running(application) {

        val request = FakeRequest(POST, signInRoute).withFormUrlEncodedBody("value" -> "")
        val result = route(application, request).value
        val view = application.injector.instanceOf[SignInView]
        val boundForm = form.bind(Map("value" -> ""))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, EmptyWaypoints)(request, messages(application)).toString
      }
    }
  }
}
