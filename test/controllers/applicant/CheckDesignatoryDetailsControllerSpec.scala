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

package controllers.applicant

import base.SpecBase
import models.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.EmptyWaypoints
import pages.applicant.CheckDesignatoryDetailsPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import viewmodels.govuk.SummaryListFluency
import views.html.applicant.CheckDesignatoryDetailsView

import scala.concurrent.Future

class CheckDesignatoryDetailsControllerSpec extends SpecBase with SummaryListFluency with MockitoSugar {

  "Check Designatory Details" - {

    "must return OK and the correct view for a GET" in {

      val app = applicationBuilder(Some(emptyUserAnswers)).build()

      running(app) {
        val request = FakeRequest(GET, routes.CheckDesignatoryDetailsController.onPageLoad.url)

        val result = route(app, request).value

        val view = app.injector.instanceOf[CheckDesignatoryDetailsView]
        val emptyList = SummaryListViewModel(Nil)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(emptyList)(request, messages(app)).toString
      }
    }

    "must save `true` and redirect to the next page for a POST" in {


      val mockUserDataService = mock[UserDataService]

      when(mockUserDataService.set(any())(any())) thenReturn Future.successful(Done)

      val app =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[UserDataService].toInstance(mockUserDataService)
          )
          .build()

      running(app) {
        val request = FakeRequest(POST, routes.CheckDesignatoryDetailsController.onSubmit.url)

        val result = route(app, request).value
        val expectedAnswers = emptyUserAnswers.set(CheckDesignatoryDetailsPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CheckDesignatoryDetailsPage.navigate(EmptyWaypoints, emptyUserAnswers, expectedAnswers).route.url
        verify(mockUserDataService, times(1)).set(eqTo(expectedAnswers))(any())
      }
    }
  }
}
