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

package controllers.actions

import base.SpecBase
import models.UserAnswers
import models.requests.{AuthenticatedIdentifierRequest, IdentifierRequest, OptionalDataRequest, UnauthenticatedIdentifierRequest}
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import services.UserDataService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: UserDataService) extends DataRetrievalActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {

    val userId = "userId"
    val nino = "nino"

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository = mock[UserDataService]
        when(sessionRepository.get("id")) thenReturn Future(None)
        val action = new Harness(sessionRepository)

        val result = action.callTransform(UnauthenticatedIdentifierRequest(FakeRequest(), "id")).futureValue

        result.userAnswers must not be defined
      }
    }

    "when there is data in the cache" - {

      "and the user is unauthenticated" - {

        "must build a userAnswers object and add it to the request" in {

          val sessionRepository = mock[UserDataService]
          val userAnswers = UserAnswers(userId)
          when(sessionRepository.get(userId)) thenReturn Future(Some(userAnswers))
          val action = new Harness(sessionRepository)

          val result = action.callTransform(UnauthenticatedIdentifierRequest(FakeRequest(), userId)).futureValue

          result.userAnswers.value mustEqual userAnswers
        }
      }

      "and the user is authenticated" - {

        "must build a userAnswers object, including the NINO from the request, and add it to the request" in {

          val sessionRepository = mock[UserDataService]
          val cachedAnswers = UserAnswers(userId)
          when(sessionRepository.get(userId)) thenReturn Future(Some(cachedAnswers))
          val action = new Harness(sessionRepository)

          val result = action.callTransform(AuthenticatedIdentifierRequest(FakeRequest(), userId, nino)).futureValue

          val expectedAnswers = cachedAnswers.copy(nino = Some(nino))
          result.userAnswers.value mustEqual expectedAnswers
        }
      }
    }
  }
}
