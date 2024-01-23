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

package repositories

import config.FrontendAppConfig
import models.ChildBirthRegistrationCountry.England
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResult, BrmsCacheItem, ChildName}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, DefaultPlayMongoRepositorySupport, IndexedMongoQueriesSupport}

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class BrmsCacheItemRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[BrmsCacheItem]
    with CleanMongoCollectionSupport
    with IndexedMongoQueriesSupport
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar {

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.brmsCacheTtl) thenReturn 1

  protected override val repository = new BrmsCacheRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  ".set" - {

    "must save the item, setting the timestamp to `now`" in {

      val request = BirthRegistrationMatchingRequest(None, ChildName("first", None, "last"), LocalDate.now(stubClock), England).value
      val result = BirthRegistrationMatchingResult.Matched

      val expectedResult = BrmsCacheItem(request, result, instant)

      val setResult = repository.set(request, result).futureValue
      val insertedRecord = find(Filters.equal("request", request)).futureValue.headOption.value

      setResult mustEqual true
      insertedRecord mustEqual expectedResult
    }
  }

  ".get" - {

    "when there is a record for this request" - {

      "must return the record" in {

        val request1 = BirthRegistrationMatchingRequest(None, ChildName("first 1", None, "last 1"), LocalDate.now(stubClock), England).value
        val brmsResult1 = BirthRegistrationMatchingResult.Matched
        val request2 = BirthRegistrationMatchingRequest(None, ChildName("first 2", None, "last 2"), LocalDate.now(stubClock), England).value
        val brmsResult2 = BirthRegistrationMatchingResult.NotMatched

        insert(BrmsCacheItem(request1, brmsResult1, instant)).futureValue
        insert(BrmsCacheItem(request2, brmsResult2, instant)).futureValue

        val result1 = repository.getResult(request1).futureValue
        val result2 = repository.getResult(request2).futureValue

        result1.value mustEqual brmsResult1
        result2.value mustEqual brmsResult2
      }
    }

    "when there is not a record for this request" -{

      "must return None" in {

        val request = BirthRegistrationMatchingRequest(None, ChildName("first 1", None, "last 1"), LocalDate.now(stubClock), England).value

        val result = repository.getResult(request).futureValue

        result must not be defined
      }
    }
  }
}
