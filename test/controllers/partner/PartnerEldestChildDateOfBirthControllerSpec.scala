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
import forms.partner.PartnerEldestChildDateOfBirthFormProvider
import models.AdultName
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.partner.{PartnerEldestChildDateOfBirthPage, PartnerNamePage}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.partner.PartnerEldestChildDateOfBirthView

import java.time.LocalDate
import scala.concurrent.Future

class PartnerEldestChildDateOfBirthControllerSpec extends SpecBase with MockitoSugar {

  private val name = AdultName("first", None, "last")
  val formProvider = new PartnerEldestChildDateOfBirthFormProvider(clockAtFixedInstant)
  private def form = formProvider(name.firstName)
  private val waypoints = EmptyWaypoints

  val validAnswer = LocalDate.now(clockAtFixedInstant).minusYears(1)

  lazy val partnerEldestChildDateOfBirthRoute = routes.PartnerEldestChildDateOfBirthController.onPageLoad(waypoints).url

  val baseAnswers = emptyUserAnswers.set(PartnerNamePage, name).success.value

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, partnerEldestChildDateOfBirthRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, partnerEldestChildDateOfBirthRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "PartnerEldestChildDateOfBirth Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val result = route(application, getRequest).value

        val view = application.injector.instanceOf[PartnerEldestChildDateOfBirthView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, name.firstName)(getRequest, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(PartnerEldestChildDateOfBirthPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[PartnerEldestChildDateOfBirthView]

        val result = route(application, getRequest).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), waypoints, name.firstName)(getRequest, messages(application)).toString
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
        val result = route(application, postRequest).value
        val expectedAnswers = baseAnswers.set(PartnerEldestChildDateOfBirthPage, validAnswer).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PartnerEldestChildDateOfBirthPage.navigate(waypoints, emptyUserAnswers, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request =
        FakeRequest(POST, partnerEldestChildDateOfBirthRoute)
          .withFormUrlEncodedBody(("value", "invalid value"))

      running(application) {
        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[PartnerEldestChildDateOfBirthView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, name.firstName)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, postRequest).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
