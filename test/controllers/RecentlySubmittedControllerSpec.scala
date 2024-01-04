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
import connectors.ClaimChildBenefitConnector
import models.{RecentClaim, TaxChargeChoice}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.RecentlySubmittedView

import java.time.format.DateTimeFormatter
import java.time.ZoneId
import scala.concurrent.Future

class RecentlySubmittedControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockConnector = mock[ClaimChildBenefitConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector)
    super.beforeEach()
  }

  "RecentlySubmitted Controller" - {

    "must return OK and the correct view for a GET" in {

      val recentClaim = RecentClaim("nino", fixedInstant, TaxChargeChoice.NotRecorded)
      val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy").withZone(ZoneId.systemDefault())
      val formattedDate = formatter.format(fixedInstant)

      when(mockConnector.getRecentClaim()(any())) thenReturn Future.successful(Some(recentClaim))

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[ClaimChildBenefitConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.RecentlySubmittedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RecentlySubmittedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(formattedDate)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery when a recent claim cannot be found for this user" in {

      when(mockConnector.getRecentClaim()(any())) thenReturn Future.successful(None)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[ClaimChildBenefitConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.RecentlySubmittedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
