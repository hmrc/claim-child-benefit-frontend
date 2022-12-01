/*
 * Copyright 2022 HM Revenue & Customs
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
  }
}
