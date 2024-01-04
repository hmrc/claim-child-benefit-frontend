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

package services

import connectors.BrmsConnector
import metrics.{BrmsMonitor, MetricsService}
import models.BirthRegistrationMatchingResult.{Matched, MatchingAttemptFailed, NotAttempted, NotMatched}
import models.ChildBirthRegistrationCountry.England
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResponseModel, BirthRegistrationMatchingResult, ChildName}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.OptionValues
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running
import repositories.BrmsCacheRepository
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

    "when the `match birth registration details` feature is turned on " - {

      "and Some details are provided" - {

        "and we do not have a result cached for this request" - {

          "must save the request / result pair, emit a metric, and return `matched` if the call to BRMS responds with `true`" in {

            val mockConnector = mock[BrmsConnector]
            val mockRepository = mock[BrmsCacheRepository]
            val mockMetrics = mock[MetricsService]

            when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(true))
            when(mockRepository.set(any(), any())) thenReturn Future.successful(true)
            when(mockRepository.getResult(any())) thenReturn Future.successful(None)

            val app =
              new GuiceApplicationBuilder()
                .overrides(
                  bind[BrmsConnector].toInstance(mockConnector),
                  bind[BrmsCacheRepository].toInstance(mockRepository),
                  bind[MetricsService].toInstance(mockMetrics)
                )
                .configure("features.match-birth-registration-details" -> true)
                .build()

            running(app) {

              val service = app.injector.instanceOf[BrmsService]
              val result = service.matchChild(Some(request)).futureValue

              result mustEqual Matched
              verify(mockRepository, times(1)).set(eqTo(request), eqTo(Matched))
              verify(mockMetrics, times(1)).count(BrmsMonitor.getCounter(Matched))
            }
          }

          "must save the request / result pair, emit a metric, and return `not matched` if the call to BRMS responds with `false`" in {

            val mockConnector = mock[BrmsConnector]
            val mockRepository = mock[BrmsCacheRepository]
            val mockMetrics = mock[MetricsService]

            when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(false))
            when(mockRepository.set(any(), any())) thenReturn Future.successful(true)
            when(mockRepository.getResult(any())) thenReturn Future.successful(None)

            val app =
              new GuiceApplicationBuilder()
                .overrides(
                  bind[BrmsConnector].toInstance(mockConnector),
                  bind[BrmsCacheRepository].toInstance(mockRepository),
                  bind[MetricsService].toInstance(mockMetrics)
                )
                .configure("features.match-birth-registration-details" -> true)
                .build()

            running(app) {

              val service = app.injector.instanceOf[BrmsService]
              val result = service.matchChild(Some(request)).futureValue

              result mustEqual NotMatched
              verify(mockRepository, times(1)).set(eqTo(request), eqTo(NotMatched))
              verify(mockMetrics, times(1)).count(BrmsMonitor.getCounter(NotMatched))
            }
          }

          "must emit a metric and return the result when trying to save the request / result pair fails" in {

            val mockConnector = mock[BrmsConnector]
            val mockRepository = mock[BrmsCacheRepository]
            val mockMetrics = mock[MetricsService]

            when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(true))
            when(mockRepository.set(any(), any())) thenReturn Future.failed(new Exception("foo"))
            when(mockRepository.getResult(any())) thenReturn Future.successful(None)

            val app =
              new GuiceApplicationBuilder()
                .overrides(
                  bind[BrmsConnector].toInstance(mockConnector),
                  bind[BrmsCacheRepository].toInstance(mockRepository),
                  bind[MetricsService].toInstance(mockMetrics)
                )
                .configure("features.match-birth-registration-details" -> true)
                .build()

            running(app) {

              val service = app.injector.instanceOf[BrmsService]
              val result = service.matchChild(Some(request)).futureValue

              result mustEqual Matched
              verify(mockRepository, times(1)).set(eqTo(request), eqTo(Matched))
              verify(mockMetrics, times(1)).count(BrmsMonitor.getCounter(Matched))
            }
          }

          "must not save the request / result pair, must emit a metric then return `matching attempt failed` if the call to BARS fails" in {

            val mockConnector = mock[BrmsConnector]
            val mockRepository = mock[BrmsCacheRepository]
            val mockMetrics = mock[MetricsService]

            when(mockConnector.matchChild(any())(any())) thenReturn Future.failed(new Exception("foo"))
            when(mockRepository.getResult(any())) thenReturn Future.successful(None)

            val app =
              new GuiceApplicationBuilder()
                .overrides(
                  bind[BrmsConnector].toInstance(mockConnector),
                  bind[BrmsCacheRepository].toInstance(mockRepository),
                  bind[MetricsService].toInstance(mockMetrics)
                )
                .configure("features.match-birth-registration-details" -> true)
                .build()

            running(app) {

              val service = app.injector.instanceOf[BrmsService]
              val result = service.matchChild(Some(request)).futureValue

              result mustEqual MatchingAttemptFailed
              verify(mockRepository, never()).set(any(), any())
              verify(mockMetrics, times(1)).count(BrmsMonitor.getCounter(MatchingAttemptFailed))
            }
          }
        }

        "and we have a result cached for this request" - {

          "must return the result from the cache and not emit a metric" in {

            val brmsResult = BirthRegistrationMatchingResult.Matched

            val mockConnector = mock[BrmsConnector]
            val mockRepository = mock[BrmsCacheRepository]
            val mockMetrics = mock[MetricsService]

            when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(true))
            when(mockRepository.getResult(any())) thenReturn Future.successful(Some(brmsResult))

            val app =
              new GuiceApplicationBuilder()
                .overrides(
                  bind[BrmsConnector].toInstance(mockConnector),
                  bind[BrmsCacheRepository].toInstance(mockRepository)
                )
                .configure("features.match-birth-registration-details" -> true)
                .build()

            running(app) {

              val service = app.injector.instanceOf[BrmsService]
              val result = service.matchChild(Some(request)).futureValue

              result mustEqual brmsResult
              verify(mockConnector, never()).matchChild(any())(any())
              verify(mockMetrics, never()).count(any())
            }
          }
        }
      }

      "and no details are provided" - {

        "must emit a metric and return NotAttempted" in {

          val mockConnector = mock[BrmsConnector]
          val mockRepository = mock[BrmsCacheRepository]
          val mockMetrics = mock[MetricsService]

          when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(true))
          when(mockRepository.set(any(), any())) thenReturn Future.successful(true)
          when(mockRepository.getResult(any())) thenReturn Future.successful(None)

          val app =
            new GuiceApplicationBuilder()
              .overrides(
                bind[BrmsConnector].toInstance(mockConnector),
                bind[BrmsCacheRepository].toInstance(mockRepository),
                bind[MetricsService].toInstance(mockMetrics)
              )
              .configure("features.match-birth-registration-details" -> true)
              .build()

          running(app) {

            val service = app.injector.instanceOf[BrmsService]
            val result = service.matchChild(None).futureValue

            result mustEqual NotAttempted
            verify(mockConnector, never()).matchChild(any())(any())
            verify(mockRepository, never()).set(any(), any())
            verify(mockMetrics, times(1)).count(BrmsMonitor.getCounter(NotAttempted))
          }
        }
      }
    }

    "when the `match birth registration details` feature is turned off" - {

      "must return `not attempted`, not emit a metric and not call BRMS" in {

        val mockConnector = mock[BrmsConnector]
        val mockMetrics   = mock[MetricsService]

        when(mockConnector.matchChild(any())(any())) thenReturn Future.successful(BirthRegistrationMatchingResponseModel(true))

        val app =
          new GuiceApplicationBuilder()
            .overrides(bind[BrmsConnector].toInstance(mockConnector))
            .configure("features.match-birth-registration-details" -> false)
            .build()

        running(app) {

          val service = app.injector.instanceOf[BrmsService]
          val result = service.matchChild(Some(request)).futureValue

          result mustEqual NotAttempted
          verify(mockConnector, never()).matchChild(any())(any())
          verify(mockMetrics, never()).count(any())
        }
      }
    }
  }
}