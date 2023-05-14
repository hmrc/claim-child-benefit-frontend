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

import _root_.auth.Retrievals._
import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, MissingBearerToken}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import views.html.IndexView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class IndexControllerSpec extends SpecBase with MockitoSugar {

  "Index Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IndexView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    ".startAgain must clear the user's cache and redirect to the home page" in {

      val mockUserDataService = mock[UserDataService]
      when(mockUserDataService.clear(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UserDataService].toInstance(mockUserDataService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.startAgain.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad.url
        verify(mockUserDataService, times(1)).clear(any())
      }
    }

    ".onSubmit" - {

      "must redirect to Need to Uplift IV when the user is signed in as an individual with CL less than 250" in {

        val authConnector = new FakeAuthConnector(Some(Individual) ~ ConfidenceLevel.L200)

        val application =
          applicationBuilder()
            .overrides(bind[AuthConnector].toInstance(authConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.IndexController.onSubmit.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.NeedToUpliftIvController.onPageLoad().url
        }
      }

      "must redirect to Recently Claimed when the user is signed in as an individual with CL 250" in {

        val authConnector = new FakeAuthConnector(Some(Individual) ~ ConfidenceLevel.L250)

        val application =
          applicationBuilder()
            .overrides(bind[AuthConnector].toInstance(authConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.IndexController.onSubmit.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RecentlyClaimedController.onPageLoad().url
        }
      }

      "must redirect to Recently Claimed when the user is signed in as an organisation" in {

        val authConnector = new FakeAuthConnector(Some(Organisation) ~ ConfidenceLevel.L250)

        val application =
          applicationBuilder()
            .overrides(bind[AuthConnector].toInstance(authConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.IndexController.onSubmit.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RecentlyClaimedController.onPageLoad().url
        }
      }

      "must redirect to Recently Claimed when the user is signed in as an agent" in {

        val authConnector = new FakeAuthConnector(Some(Agent) ~ ConfidenceLevel.L250)

        val application =
          applicationBuilder()
            .overrides(bind[AuthConnector].toInstance(authConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.IndexController.onSubmit.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RecentlyClaimedController.onPageLoad().url
        }
      }

      "must redirect to Recently Claimed when the user is not signed in" in {

        val authConnector = new FakeFailingAuthConnector(MissingBearerToken())

        val application =
          applicationBuilder()
            .overrides(bind[AuthConnector].toInstance(authConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.IndexController.onSubmit.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RecentlyClaimedController.onPageLoad().url
        }
      }
    }
  }

  class FakeAuthConnector[T](value: T) extends AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.fromTry(Try(value.asInstanceOf[A]))
  }

  class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.failed(exceptionToReturn)
  }
}
