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

package controllers.auth

import base.SpecBase
import connectors.IvConnector
import models.IvResult
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.auth._

import scala.concurrent.Future

class IvControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockIvConnector = mock[IvConnector]

  override def beforeEach(): Unit = {
    Mockito.reset(mockIvConnector)
    super.beforeEach()
  }

  private val journeyId = "journeyId"
  private val continueUrl = "continueUrl"
  private val app = applicationBuilder(None).overrides(bind[IvConnector].toInstance(mockIvConnector)).build()
  private val request = FakeRequest(GET, routes.IvController.handleIvFailure(continueUrl, Some(journeyId)).url)

  "handleIvFailure" - {

    "must redirect to the continue URL when the IV journey status is Success" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.Success))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual continueUrl
    }

    "must redirect to Iv Incomplete when the IV journey status is Incomplete" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.Incomplete))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivIncomplete.url
    }

    "must redirect to Iv Failed Matching when the IV journey status is FailedMatching" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.FailedMatching))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivFailedMatching(continueUrl).url
    }

    "must redirect to Iv Failed Identity Verification when the IV journey status is FailedIdentityVerification" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.FailedIdentityVerification))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivFailedIdentityVerification(continueUrl).url
    }

    "must redirect to Iv Insufficient Evidence when the IV journey status is InsufficientEvidence" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.InsufficientEvidence))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivInsufficientEvidence.url
    }

    "must redirect to Iv User Aborted when the IV journey status is UserAborted" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.UserAborted))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivUserAborted(continueUrl).url
    }

    "must redirect to Iv Locked Out when the IV journey status is LockedOut" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.LockedOut))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivLockedOut.url
    }

    "must redirect to Iv Precondition Failed when the IV journey status is IvPreconditionFailed" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.IvPreconditionFailed))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivPreconditionFailed.url
    }

    "must redirect to Iv Technical Issue when the IV journey status is IvTechnicalIssue" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.TechnicalIssue))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivTechnicalIssue.url
    }

    "must redirect to Iv Timeout when the IV journey status is IvTimeout" in {

      when(mockIvConnector.getJourneyStatus(eqTo(journeyId))(any())).thenReturn(Future.successful(IvResult.Timeout))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivTimeout.url
    }

    "must redirect to Iv Error when called with no journey Id" in {

      val request = FakeRequest(GET, routes.IvController.handleIvFailure(continueUrl, None).url)
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.IvController.ivError.url
    }
  }

  "ivIncomplete" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivIncomplete.url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvIncompleteView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }

  "ivFailedMatching" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivFailedMatching(continueUrl).url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvFailedMatchingView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(continueUrl)(request, messages(app)).toString
    }
  }

  "ivFailedIdentityVerification" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivFailedIdentityVerification(continueUrl).url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvFailedIdentityVerificationView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(continueUrl)(request, messages(app)).toString
    }
  }

  "ivInsufficientEvidence" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivInsufficientEvidence.url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvInsufficientEvidenceView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }

  "ivUserAborted" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivUserAborted(continueUrl).url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvUserAbortedView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(continueUrl)(request, messages(app)).toString
    }
  }

  "ivLockedOut" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivLockedOut.url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvLockedOutView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }

  "ivPreconditionFailed" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivPreconditionFailed.url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvPreconditionFailedView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }

  "ivTechnicalIssue" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivTechnicalIssue.url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvTechnicalIssueView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }

  "ivTimeout" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivTimeout.url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvTimeoutView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }

  "ivError" - {

    "must return OK and the correct view" in {

      val request = FakeRequest(GET, routes.IvController.ivError.url)
      val result = route(app, request).value

      val view = app.injector.instanceOf[IvErrorView]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view()(request, messages(app)).toString
    }
  }
}
