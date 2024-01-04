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
import models.ChildName
import pages.EmptyWaypoints
import pages.child.{CheckChildDetailsPage, ChildNamePage}
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.child.ChildNameSummary
import viewmodels.govuk.SummaryListFluency
import views.html.child.CheckChildDetailsView

class CheckChildDetailsControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    val waypoints = EmptyWaypoints
    val childName = ChildName("first", None, "last")
    val baseAnswers = emptyUserAnswers.set(ChildNamePage(index), childName).success.value

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckChildDetailsController.onPageLoad(waypoints, index).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckChildDetailsView]
        val msgs = application.injector.instanceOf[MessagesApi].preferred(request)
        val list = SummaryListViewModel(
          Seq(
            ChildNameSummary.row(baseAnswers, index, waypoints, CheckChildDetailsPage(index))(msgs)
          ).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, waypoints, index, childName)(request, msgs).toString
      }
    }

    "must redirect to the next page for a POST" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckChildDetailsController.onPageLoad(waypoints, index).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual CheckChildDetailsPage(index).navigate(waypoints, emptyUserAnswers, emptyUserAnswers).route.url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckChildDetailsController.onPageLoad(waypoints, index).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckChildDetailsController.onPageLoad(waypoints, index).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual baseRoutes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
