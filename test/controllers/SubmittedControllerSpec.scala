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

package controllers

import base.SpecBase
import connectors.ClaimChildBenefitConnector
import generators.ModelGenerators
import models.RecentClaim
import models.TaxChargeChoice._
import models.TaxChargePayer._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, status, _}
import uk.gov.hmrc.domain.Nino
import views.html.{SubmittedNoTaxChargeView, SubmittedWithTaxChargeBeingPaidView, SubmittedWithTaxChargeNotBeingPaidView}

import java.time.Instant
import scala.concurrent.Future

class SubmittedControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach with ModelGenerators {

  private val mockConnector = mock[ClaimChildBenefitConnector]
  private val now = Instant.now
  private val nino = arbitrary[Nino].sample.value.nino

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector)
    super.beforeEach()
  }

  private val app = applicationBuilder().overrides(bind[ClaimChildBenefitConnector].toInstance(mockConnector)).build()

  "Submitted Controller" - {

    "must return OK and the correct view for a GET" - {

      "when the applicant was not impacted by HICBC" in {

        val recentClaim = RecentClaim(nino, now, DoesNotApply)
        when(mockConnector.getRecentClaim()(any())).thenReturn(Future.successful(Some(recentClaim)))

        val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

        val result = route(app, request).value

        val view = app.injector.instanceOf[SubmittedNoTaxChargeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(app)).toString
      }

      "when the applicant opted out of HICBC" in {

        val recentClaim = RecentClaim(nino, now, OptedOut)
        when(mockConnector.getRecentClaim()(any())).thenReturn(Future.successful(Some(recentClaim)))

        val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

        val result = route(app, request).value

        val view = app.injector.instanceOf[SubmittedWithTaxChargeNotBeingPaidView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(app)).toString
      }

      "when the applicant opted in to HICBC" in {

        val taxChargePayer = Gen.oneOf(Applicant, Partner, ApplicantOrPartner).sample.value
        val recentClaim = RecentClaim(nino, now, OptedIn(taxChargePayer))
        when(mockConnector.getRecentClaim()(any())).thenReturn(Future.successful(Some(recentClaim)))

        val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

        val result = route(app, request).value

        val view = app.injector.instanceOf[SubmittedWithTaxChargeBeingPaidView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(taxChargePayer)(request, messages(app)).toString
      }
    }

    "must redirect to Recently Submitted when the user has a recent claim but the tax charge choice was not recorded" in {

      val recentClaim = RecentClaim(nino, now, NotRecorded)
      when(mockConnector.getRecentClaim()(any())).thenReturn(Future.successful(Some(recentClaim)))

      val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.RecentlySubmittedController.onPageLoad().url
    }

    "must redirect to the home page when we cannot find a recent claim for this user" in {

      when(mockConnector.getRecentClaim()(any())).thenReturn(Future.successful(None))

      val request = FakeRequest(GET, routes.SubmittedController.onPageLoad.url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IndexController.onPageLoad.url
    }

    ".exitSurvey" - {

      "must clear the user's session and redirect to the exit survey" in {

        val application = applicationBuilder().build()

        running(application) {

          val request = FakeRequest(routes.SubmittedController.exitSurvey).withSession("foo" -> "bar")

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          session(result).get("foo") must not be defined
        }
      }
    }
  }
}
