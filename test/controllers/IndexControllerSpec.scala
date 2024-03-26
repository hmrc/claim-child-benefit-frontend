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

import _root_.auth.Retrievals._
import base.SpecBase
import config.FrontendAppConfig
import controllers.actions.OptionalAuthIdentifierAction
import models.Done
import models.requests.{AuthenticatedIdentifierRequest, UnauthenticatedIdentifierRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Results.Ok
import play.api.mvc.{BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.UserDataService
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, CredentialStrength}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class IndexControllerSpec extends SpecBase with MockitoSugar {

  "Index Controller" - {

    ".startAgain must clear the user's cache and redirect to the home page" in {

      val mockUserDataService = mock[UserDataService]
      when(mockUserDataService.clear()(any())) thenReturn Future.successful(Done)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UserDataService].toInstance(mockUserDataService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.IndexController.startAgain.url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RecentlyClaimedController.onPageLoad().url
        verify(mockUserDataService, times(1)).clear()(any())
      }
    }

    ".onPageLoad" - {

      "must redirect to PEGA Child Benefit service if user is logged in" in {

        val application =
          applicationBuilder().build()

        val bodyParsers: BodyParsers.Default = application.injector.instanceOf[BodyParsers.Default]
        val config: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new OptionalAuthIdentifierAction(
          new FakeAuthConnector(Some(Individual) ~ Some(CredentialStrength.strong) ~ ConfidenceLevel.L250 ~ Some("userId") ~ Some("nino")),
          bodyParsers,
          config
        )

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

          val result: Future[Result] = authAction(a => a match {
            case x: AuthenticatedIdentifierRequest[_] => Ok(s"${x.userId} ${x.nino}")
            case y: UnauthenticatedIdentifierRequest[_] => Ok(y.userId)
          })(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "https://account.hmrc.gov.uk/child-benefit/make_a_claim/recently-claimed-child-benefit"
        }
      }

      "must redirect to recently claimed if user is not logged in" in {

        val application =
          applicationBuilder().build()

        running(application) {
          val request = FakeRequest(GET, routes.IndexController.onPageLoad.url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.RecentlyClaimedController.onPageLoad().url
        }
      }
    }
  }
}

class FakeAuthConnector[T](value: T) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.fromTry(Try(value.asInstanceOf[A]))
}
