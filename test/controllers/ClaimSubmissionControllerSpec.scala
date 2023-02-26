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
import models.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.ClaimSubmissionService

import scala.concurrent.Future

class ClaimSubmissionControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockSubmissionService = mock[ClaimSubmissionService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockSubmissionService)
    super.beforeEach()
  }

  "ClaimSubmission controller" - {

    "when the user's claim cannot be submitted to CBS" - {

      "must redirect to Print" in {

        when(mockSubmissionService.canSubmit(any())(any())) thenReturn Future.successful(false)

        val app =
          applicationBuilder(Some(emptyUserAnswers))
            .overrides(bind[ClaimSubmissionService].toInstance(mockSubmissionService))
            .build()

        running(app) {
          val request = FakeRequest(GET, routes.ClaimSubmissionController.submit.url)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.PrintController.onPageLoad.url
        }
      }
    }

    "when the user's claim can be submitted to CBS" - {

      "must submit the claim and redirect to Submitted" in {

        when(mockSubmissionService.canSubmit(any())(any())) thenReturn Future.successful(true)
        when(mockSubmissionService.submit(any())(any()))    thenReturn Future.successful(Done)

        val app =
          applicationBuilder(Some(emptyUserAnswers))
            .overrides(bind[ClaimSubmissionService].toInstance(mockSubmissionService))
            .build()

        running(app) {

          val request = FakeRequest(GET, routes.ClaimSubmissionController.submit.url)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SubmittedController.onPageLoad.url
          verify(mockSubmissionService, times(1)).submit(any())(any())
        }
      }
    }
  }
}
