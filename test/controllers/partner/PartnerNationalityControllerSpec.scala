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

package controllers.partner

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.partner.PartnerNationalityFormProvider
import models.{AdultName, Done, Nationality}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.partner.{PartnerNamePage, PartnerNationalityPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.partner.PartnerNationalityView

import scala.concurrent.Future

class PartnerNationalityControllerSpec extends SpecBase with MockitoSugar {

  private val waypoints = EmptyWaypoints
  private val name = AdultName(None, "first", None, "last")
  private val baseAnswers = emptyUserAnswers.set(PartnerNamePage, name).success.value

  val formProvider = new PartnerNationalityFormProvider()
  val form = formProvider(name.firstName, index, Seq.empty[Nationality])
  val validAnswer = Nationality.allNationalities.head

  lazy val partnerNationalityRoute = routes.PartnerNationalityController.onPageLoad(waypoints, index).url

  "PartnerNationality Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerNationalityRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerNationalityView]

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form, waypoints, index, name.firstName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(PartnerNationalityPage(index), validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerNationalityRoute)

        val view = application.injector.instanceOf[PartnerNationalityView]

        val result = route(application, request).value

        status(result) `mustEqual` OK
        contentAsString(result) `mustEqual` view(form.fill(validAnswer), waypoints, index, name.firstName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) `thenReturn` Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerNationalityRoute)
            .withFormUrlEncodedBody(("value", validAnswer.name))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(PartnerNationalityPage(index), validAnswer).success.value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` PartnerNationalityPage(index)
          .navigate(waypoints, emptyUserAnswers, expectedAnswers)
          .url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerNationalityRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnerNationalityView]

        val result = route(application, request).value

        status(result) `mustEqual` BAD_REQUEST
        contentAsString(result) `mustEqual` view(boundForm, waypoints, index, name.firstName)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnerNationalityRoute)

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerNationalityRoute)
            .withFormUrlEncodedBody(("value", validAnswer.name))

        val result = route(application, request).value

        status(result) `mustEqual` SEE_OTHER
        redirectLocation(result).value `mustEqual` baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
