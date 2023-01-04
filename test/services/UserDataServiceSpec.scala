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

package services

import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import play.api.libs.json.Json
import repositories.SessionRepository

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.Future

class UserDataServiceSpec
  extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with BeforeAndAfterEach {

  private val instant   = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock = Clock.fixed(instant, ZoneId.systemDefault)
  private val userId    = "foo"
  private val answers   = UserAnswers(userId, Json.obj("bar" -> "baz"), lastUpdated = Instant.now(stubClock))

  private val mockRepo = mock[SessionRepository]

  private val service = new UserDataService(mockRepo)

  override def beforeEach(): Unit = {
    reset(mockRepo)
    super.beforeEach()
  }

  ".get" - {

    "must return user answers when they exist in the repository" in {

      when(mockRepo.get(eqTo(userId))) thenReturn Future.successful(Some(answers))

      service.get(userId).futureValue.value mustEqual answers
    }

    "must return None when answers do not exist in the repository" in {

      when(mockRepo.get(eqTo(userId))) thenReturn Future.successful(None)

      service.get(userId).futureValue must not be defined
    }
  }

  ".set" - {

    "must write the answers to the repository" in {

      when(mockRepo.set(any())) thenReturn Future.successful(true)

      service.set(answers).futureValue mustEqual true
      verify(mockRepo, times(1)).set(eqTo(answers))
    }
  }

  ".keepAlive" - {

    "must keep the repository record alive" in {

      when(mockRepo.keepAlive(any())) thenReturn Future.successful(true)

      service.keepAlive(userId).futureValue mustEqual true
      verify(mockRepo, times(1)).keepAlive(eqTo(userId))
    }
  }

  ".clear" - {

    "must remove the record from the repository" in {

      when(mockRepo.clear(any())) thenReturn Future.successful(true)

      service.clear(userId).futureValue mustEqual true
      verify(mockRepo, times(1)).clear(eqTo(userId))
    }
  }
}
