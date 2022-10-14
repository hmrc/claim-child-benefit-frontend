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

package services

import connectors.BrmsConnector
import models.BirthRegistrationMatchingResult.{Matched, NotMatched}
import models.ChildBirthRegistrationCountry.England
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResponseModel, ChildName}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BrmsServiceSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures {

  ".matchChild" - {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    val request = BirthRegistrationMatchingRequest(None, ChildName("first", None, "last"), LocalDate.now, England).value

    "must return `matched` if the call to BRMS responds with `true`" in {

      val mockConnector = mock[BrmsConnector]
      when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(true))

      val app =
        new GuiceApplicationBuilder()
          .overrides(bind[BrmsConnector].toInstance(mockConnector))
          .build()

      running(app) {

        val service = app.injector.instanceOf[BrmsService]
        val result = service.matchChild(request).futureValue

        result mustEqual Matched
      }
    }

    "must return `not matched` if the call to BRMS responds with `false`" in {

      val mockConnector = mock[BrmsConnector]
      when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(false))

      val app =
        new GuiceApplicationBuilder()
          .overrides(bind[BrmsConnector].toInstance(mockConnector))
          .build()

      running(app) {

        val service = app.injector.instanceOf[BrmsService]
        val result = service.matchChild(request).futureValue

        result mustEqual NotMatched
      }
    }
  }
}
