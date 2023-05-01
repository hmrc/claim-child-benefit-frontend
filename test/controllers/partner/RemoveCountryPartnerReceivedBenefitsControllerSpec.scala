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

package controllers.partner

import base.SpecBase
import controllers.{routes => baseRoutes}
import forms.partner.RemoveCountryPartnerReceivedBenefitsFormProvider
import models.{AdultName, Country, Done}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.{CountryPartnerReceivedBenefitsPage, PartnerNamePage, RemoveCountryPartnerReceivedBenefitsPage}
import pages.{EmptyWaypoints, partner}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import views.html.partner.RemoveCountryPartnerReceivedBenefitsView

import scala.concurrent.Future

class RemoveCountryPartnerReceivedBenefitsControllerSpec extends SpecBase with MockitoSugar {

  private val country = Country.internationalCountries.head
  private val name = AdultName(None, "first", None, "last")

  val formProvider = new RemoveCountryPartnerReceivedBenefitsFormProvider()
  val form = formProvider(name.firstName, country.name)
  private val waypoints = EmptyWaypoints
  private val baseAnswers =
    emptyUserAnswers
      .set(PartnerNamePage, name).success.value
      .set(CountryPartnerReceivedBenefitsPage(index), country).success.value

  lazy val removeCountryPartnerReceivedBenefitsRoute = routes.RemoveCountryPartnerReceivedBenefitsController.onPageLoad(waypoints, index).url

  "RemoveCountryPartnerReceivedBenefits Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeCountryPartnerReceivedBenefitsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveCountryPartnerReceivedBenefitsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, waypoints, index, name.firstName, country.name)(request, messages(application)).toString
      }
    }

    "must remove the previous name and redirect to the next page when the answer is yes" in {

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
          FakeRequest(POST, removeCountryPartnerReceivedBenefitsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.remove(partner.CountryPartnerReceivedBenefitsPage(index)).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RemoveCountryPartnerReceivedBenefitsPage(index).navigate(waypoints, baseAnswers, expectedAnswers).url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }

    "must not remove the previous name and redirect to the next page when the answer is no" in {

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
          FakeRequest(POST, removeCountryPartnerReceivedBenefitsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual partner.RemoveCountryPartnerReceivedBenefitsPage(index).navigate(waypoints, baseAnswers, baseAnswers).url
        verify(mockUserDataService, never()).set(any())(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeCountryPartnerReceivedBenefitsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveCountryPartnerReceivedBenefitsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, waypoints, index, name.firstName, country.name)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeCountryPartnerReceivedBenefitsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeCountryPartnerReceivedBenefitsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
