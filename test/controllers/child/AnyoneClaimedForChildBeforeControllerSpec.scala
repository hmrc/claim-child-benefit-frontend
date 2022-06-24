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
import forms.child.AnyoneClaimedForChildBeforeFormProvider
import models.{AnyoneClaimedForChildBefore, ChildName, RelationshipStatus, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{AnyoneClaimedForChildBeforePage, ChildNamePage}
import pages.{EmptyWaypoints, RelationshipStatusPage, child}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.child.AnyoneClaimedForChildBeforeView

import scala.concurrent.Future

class AnyoneClaimedForChildBeforeControllerSpec extends SpecBase with MockitoSugar {

  private implicit val msgs: Messages = stubMessages(stubMessagesApi())

  private val waypoints          = EmptyWaypoints
  private val childName          = ChildName("first", None, "last")
  private val relationshipStatus = RelationshipStatus.Married

  private val baseAnswers =
    emptyUserAnswers
      .set(ChildNamePage(index), childName).success.value
      .set(RelationshipStatusPage, relationshipStatus).success.value

  lazy val anyoneClaimedForChildBeforeRoute = routes.AnyoneClaimedForChildBeforeController.onPageLoad(waypoints, index).url

  val formProvider = new AnyoneClaimedForChildBeforeFormProvider()
  val form = formProvider(childName, relationshipStatus)

  "AnyoneClaimedForChildBefore Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, anyoneClaimedForChildBeforeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AnyoneClaimedForChildBeforeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, childName, relationshipStatus)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AnyoneClaimedForChildBeforePage(index), AnyoneClaimedForChildBefore.allValues.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, anyoneClaimedForChildBeforeRoute)

        val view = application.injector.instanceOf[AnyoneClaimedForChildBeforeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form.fill(AnyoneClaimedForChildBefore.allValues.head),
          waypoints,
          index,
          childName,
          relationshipStatus
        )(request, messages(application)).toString
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
          FakeRequest(POST, anyoneClaimedForChildBeforeRoute)
            .withFormUrlEncodedBody(("value", AnyoneClaimedForChildBefore.allValues.head.toString))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.AnyoneClaimedForChildBeforePage(index), AnyoneClaimedForChildBefore.allValues.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual child.AnyoneClaimedForChildBeforePage(index).navigate(waypoints, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, anyoneClaimedForChildBeforeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[AnyoneClaimedForChildBeforeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, childName, relationshipStatus)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, anyoneClaimedForChildBeforeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, anyoneClaimedForChildBeforeRoute)
            .withFormUrlEncodedBody(("value", AnyoneClaimedForChildBefore.allValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
