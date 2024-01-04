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

package controllers.child

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.child.AddChildPreviousNameFormProvider
import models.{ChildName, Done}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{AddChildPreviousNamePage, ChildNamePage}
import pages.{EmptyWaypoints, child}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import viewmodels.checkAnswers.child.AddChildPreviousNameSummary
import views.html.child.AddChildPreviousNameView

import scala.concurrent.Future

class AddChildPreviousNameControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AddChildPreviousNameFormProvider()
  val form = formProvider()
  private val waypoints = EmptyWaypoints

  private val childName = ChildName("first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(ChildNamePage(index), childName).success.value

  lazy val addChildPreviousNameRoute = routes.AddChildPreviousNameController.onPageLoad(waypoints, index).url

  "AddChildPreviousName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addChildPreviousNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddChildPreviousNameView]

        implicit val msgs: Messages = messages(application)
        val previousNames = AddChildPreviousNameSummary.rows(baseAnswers, index, waypoints, AddChildPreviousNamePage(index))

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, childName.firstName, previousNames)(request,implicitly).toString
      }
    }

    "must not repopulate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(child.AddChildPreviousNamePage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addChildPreviousNameRoute)

        val view = application.injector.instanceOf[AddChildPreviousNameView]

        implicit val msgs: Messages = messages(application)
        val previousNames = AddChildPreviousNameSummary.rows(baseAnswers, index, waypoints, child.AddChildPreviousNamePage(index))

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, childName.firstName, previousNames)(request, implicitly).toString
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
          FakeRequest(POST, addChildPreviousNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.AddChildPreviousNamePage(index), true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual child.AddChildPreviousNamePage(index).navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addChildPreviousNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddChildPreviousNameView]

        implicit val msgs: Messages = messages(application)
        val previousNames = AddChildPreviousNameSummary.rows(baseAnswers, index, waypoints, child.AddChildPreviousNamePage(index))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, childName.firstName, previousNames)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addChildPreviousNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addChildPreviousNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
