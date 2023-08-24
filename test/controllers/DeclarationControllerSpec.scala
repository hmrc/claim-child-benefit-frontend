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
import connectors.ClaimChildBenefitConnector.{AlreadyInPaymentException, InvalidClaimStateException}
import models.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ClaimSubmissionService
import views.html.DeclarationView

import scala.concurrent.Future

class DeclarationControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private lazy val declarationRoute = routes.DeclarationController.onPageLoad.url

  private val mockSubmissionService = mock[ClaimSubmissionService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockSubmissionService)
    super.beforeEach()
  }

  "Declaration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, declarationRoute)
        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    "when the user's claim cannot be submitted to CBS" - {

      "must redirect to Print" in {

        when(mockSubmissionService.canSubmit(any())(any())) thenReturn Future.successful(false)

        val app =
          applicationBuilder(Some(emptyUserAnswers))
            .overrides(bind[ClaimSubmissionService].toInstance(mockSubmissionService))
            .build()

        running(app) {
          val request = FakeRequest(POST, routes.DeclarationController.onSubmit.url)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.PrintController.onPageLoad.url
        }
      }
    }

    "when the user's claim can be submitted to CBS" - {

      "must submit the claim" - {

        "and redirect to Submitted when the submission is successful" in {

          when(mockSubmissionService.canSubmit(any())(any())) thenReturn Future.successful(true)
          when(mockSubmissionService.submit(any())(any(), any())) thenReturn Future.successful(Done)

          val app =
            applicationBuilder(Some(emptyUserAnswers))
              .overrides(bind[ClaimSubmissionService].toInstance(mockSubmissionService))
              .build()

          running(app) {

            val request = FakeRequest(POST, routes.DeclarationController.onSubmit.url)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.SubmittedController.onPageLoad.url
            verify(mockSubmissionService, times(1)).submit(any())(any(), any())
          }
        }

        "and redirect to SubmissionFailedExistingClaim when there is an existing claim for the user in CBS" in {

          when(mockSubmissionService.canSubmit(any())(any())) thenReturn Future.successful(true)
          when(mockSubmissionService.submit(any())(any(), any())) thenReturn Future.failed(new InvalidClaimStateException)

          val app =
            applicationBuilder(Some(emptyUserAnswers))
              .overrides(bind[ClaimSubmissionService].toInstance(mockSubmissionService))
              .build()

          running(app) {
            val request = FakeRequest(POST, routes.DeclarationController.onSubmit.url)
            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.SubmissionFailedExistingClaimController.onPageLoad().url
            verify(mockSubmissionService, times(1)).submit(any())(any(), any())
          }
        }

        "and redirect to SubmissionFailedAlreadyInPayment when the user has an existing claim which is already in payment" in {

          when(mockSubmissionService.canSubmit(any())(any())) thenReturn Future.successful(true)
          when(mockSubmissionService.submit(any())(any(), any())) thenReturn Future.failed(new AlreadyInPaymentException)

          val app =
            applicationBuilder(Some(emptyUserAnswers))
              .overrides(bind[ClaimSubmissionService].toInstance(mockSubmissionService))
              .build()

          running(app) {
            val request = FakeRequest(POST, routes.DeclarationController.onSubmit.url)
            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.SubmissionFailedAlreadyInPaymentController.onPageLoad().url
            verify(mockSubmissionService, times(1)).submit(any())(any(), any())
          }
        }

        "and redirect to Print when the submission fails for another reason" in {

          when(mockSubmissionService.canSubmit(any())(any())) thenReturn Future.successful(true)
          when(mockSubmissionService.submit(any())(any(), any())) thenReturn Future.failed(new Exception("foo"))

          val app =
            applicationBuilder(Some(emptyUserAnswers))
              .overrides(bind[ClaimSubmissionService].toInstance(mockSubmissionService))
              .build()

          running(app) {

            val request = FakeRequest(POST, routes.DeclarationController.onSubmit.url)

            val result = route(app, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.PrintController.onPageLoad.url
            verify(mockSubmissionService, times(1)).submit(any())(any(), any())
          }
        }
      }
    }
  }
}
