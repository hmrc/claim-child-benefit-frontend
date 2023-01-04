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

package repositories

import config.FrontendAppConfig
import models.{BirthRegistrationMatchingRequest, BirthRegistrationMatchingResult, BrmsCacheItem}
import org.mongodb.scala.model._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BrmsCacheRepository @Inject()(
                                     mongoComponent: MongoComponent,
                                     appConfig: FrontendAppConfig,
                                     clock: Clock
                                   )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[BrmsCacheItem](
    collectionName = "brms-results",
    mongoComponent = mongoComponent,
    domainFormat   = BrmsCacheItem.format,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("timestamp"),
        IndexOptions()
          .name("timestampIdx")
          .expireAfter(appConfig.brmsCacheTtl, TimeUnit.SECONDS)
      ),
      IndexModel(
        Indexes.ascending("request"),
        IndexOptions()
          .name("requestIdx")
          .unique(true)
      )
    ),
    extraCodecs = Seq(Codecs.playFormatCodec(BirthRegistrationMatchingRequest.format))
  ) {

  def set(request: BirthRegistrationMatchingRequest, result: BirthRegistrationMatchingResult): Future[Boolean] = {

    val item = BrmsCacheItem(request, result, Instant.now(clock))

    collection.replaceOne(
      filter = Filters.equal("request", item.request),
      replacement = item,
      options = ReplaceOptions().upsert(true)
    )
      .toFuture()
      .map(_ => true)
  }

  def getResult(request: BirthRegistrationMatchingRequest): Future[Option[BirthRegistrationMatchingResult]] = {
    collection.find(Filters.equal("request", request))
      .headOption
      .map(_.map(_.result))
  }
}
