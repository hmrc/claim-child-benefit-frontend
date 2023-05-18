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

package controllers.actions

import connectors.ClaimChildBenefitConnector
import controllers.routes
import models.requests.{AuthenticatedIdentifierRequest, IdentifierRequest, UnauthenticatedIdentifierRequest}
import models.{RecentClaim, TaxChargeChoice}
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckRecentClaimsActionSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with ScalaFutures
    with OptionValues {

  private val mockConnector = mock[ClaimChildBenefitConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector)
    super.beforeEach()
  }

  private class Harness(connector: ClaimChildBenefitConnector) extends CheckRecentClaimsAction(connector) {

    def callFilter[A](request: IdentifierRequest[A]): Future[Option[Result]] = filter(request)

  }

  "Check Recent Claims Action" - {

    "when the user is authenticated" - {

      val request = AuthenticatedIdentifierRequest(FakeRequest(), "userId", "nino")

      "must redirect to Recently Submitted when the user has recently submitted a claim" in {

        val recentClaim = RecentClaim("nino", Instant.now, TaxChargeChoice.NotRecorded)
        when(mockConnector.getRecentClaim()(any())) thenReturn Future.successful(Some(recentClaim))

        val action = new Harness(mockConnector)

        val result = action.callFilter(request).futureValue

        result.value mustEqual Redirect(routes.RecentlySubmittedController.onPageLoad())
        verify(mockConnector, times(1)).getRecentClaim()(any())
      }

      "must proceed when the user has not recently submitted a claim" in {

        when(mockConnector.getRecentClaim()(any())) thenReturn Future.successful(None)

        val action = new Harness(mockConnector)

        val result = action.callFilter(request).futureValue

        result must not be defined
        verify(mockConnector, times(1)).getRecentClaim()(any())
      }
    }

    "when the user is unauthenticated" - {

      val request = UnauthenticatedIdentifierRequest(FakeRequest(), "userId")

      "must proceed" in {

        val action = new Harness(mockConnector)

        val result = action.callFilter(request).futureValue

        result must not be defined
        verify(mockConnector, never).getRecentClaim()(any())
      }
    }
  }
}
