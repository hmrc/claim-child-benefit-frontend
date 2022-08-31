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

package controllers.partner

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.partner.PartnerWaitingForEntitlementDecisionFormProvider
import models.AdultName
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.partner.{PartnerNamePage, PartnerWaitingForEntitlementDecisionPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.partner.PartnerWaitingForEntitlementDecisionView

import scala.concurrent.Future

class PartnerWaitingForEntitlementDecisionControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")
  private val name = AdultName("first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(PartnerNamePage, name).success.value

  val formProvider = new PartnerWaitingForEntitlementDecisionFormProvider()
  val form = formProvider(name.firstName)
  private val waypoints = EmptyWaypoints

  lazy val partnerWaitingForEntitlementDecisionRoute = routes.PartnerWaitingForEntitlementDecisionController.onPageLoad(waypoints).url

  "PartnerWaitingForEntitlementDecision Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerWaitingForEntitlementDecisionRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerWaitingForEntitlementDecisionView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, name.firstName)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(PartnerWaitingForEntitlementDecisionPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerWaitingForEntitlementDecisionRoute)

        val view = application.injector.instanceOf[PartnerWaitingForEntitlementDecisionView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), waypoints, name.firstName)(request, messages(application)).toString
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
          FakeRequest(POST, partnerWaitingForEntitlementDecisionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(PartnerWaitingForEntitlementDecisionPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PartnerWaitingForEntitlementDecisionPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerWaitingForEntitlementDecisionRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnerWaitingForEntitlementDecisionView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, name.firstName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnerWaitingForEntitlementDecisionRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerWaitingForEntitlementDecisionRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
