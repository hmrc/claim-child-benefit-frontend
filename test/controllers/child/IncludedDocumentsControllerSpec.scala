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
import forms.child.IncludedDocumentsFormProvider
import models.ApplicantRelationshipToChild.BirthChild
import models.{ChildName, IncludedDocuments}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.child.{ApplicantRelationshipToChildPage, ChildNamePage, IncludedDocumentsPage}
import pages.{EmptyWaypoints, child}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.child.IncludedDocumentsView

import scala.concurrent.Future

class IncludedDocumentsControllerSpec extends SpecBase with MockitoSugar {

  private val waypoints = EmptyWaypoints

  private lazy val includedDocumentsRoute = routes.IncludedDocumentsController.onPageLoad(waypoints, index).url

  private val childName           = ChildName("first", None, "last")
  private val relationshipToChild = BirthChild
  private val allowedValues       = IncludedDocuments.standardDocuments(relationshipToChild)

  private val baseAnswers =
    emptyUserAnswers
      .set(ChildNamePage(index), childName).success.value
      .set(ApplicantRelationshipToChildPage(index), relationshipToChild).success.value

  private val formProvider = new IncludedDocumentsFormProvider()

  "IncludedDocuments Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, includedDocumentsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IncludedDocumentsView]
        implicit val msgs = messages(application)
        val form = formProvider(childName, relationshipToChild)

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(form, waypoints, index, childName, relationshipToChild)(request, implicitly).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(IncludedDocumentsPage(index), allowedValues.toSet).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, includedDocumentsRoute)

        val view = application.injector.instanceOf[IncludedDocumentsView]
        implicit val msgs = messages(application)
        val form = formProvider(childName, relationshipToChild)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(allowedValues.toSet), waypoints, index, childName, relationshipToChild)(request, implicitly).toString
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
          FakeRequest(POST, includedDocumentsRoute)
            .withFormUrlEncodedBody(("value[0]", allowedValues.head.toString))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(child.IncludedDocumentsPage(index), Set(allowedValues.head)).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual child.IncludedDocumentsPage(index).navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, includedDocumentsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        implicit val msgs = messages(application)
        val form = formProvider(childName, relationshipToChild)

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[IncludedDocumentsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, childName, relationshipToChild)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, includedDocumentsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, includedDocumentsRoute)
            .withFormUrlEncodedBody(("value[0]", allowedValues.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
