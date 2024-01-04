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

package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.NeedToUpliftIvView

class NeedToUpliftIvControllerSpec extends SpecBase {

  "NeedToUpliftIv Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.NeedToUpliftIvController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[NeedToUpliftIvView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "must redirect to uplift IV for a POST" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, routes.NeedToUpliftIvController.onSubmit().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "http://localhost:9948/iv-stub/uplift?origin=CHB&confidenceLevel=250&completionURL=http%3A%2F%2Flocalhost%3A11303%2Ffill-online%2Fclaim-child-benefit%2Frecently-claimed-child-benefit&failureURL=http%3A%2F%2Flocalhost%3A11303%2Ffill-online%2Fclaim-child-benefit%2Fidentity-verification%2Fcomplete%3FcontinueUrl%3D%252Ffill-online%252Fclaim-child-benefit%252Fneed-to-confirm-identity"
      }
    }
  }
}
