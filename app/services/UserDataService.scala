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

import connectors.UserAnswersConnector
import logging.Logging
import models.{Done, UserAnswers}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UserDataService @Inject()(
                                 repository: SessionRepository,
                                 connector: UserAnswersConnector
                               )(implicit ec: ExecutionContext) extends Logging {

  def get(userId: String): Future[Option[UserAnswers]] =
    repository.get(userId)

  def set(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Done] =
    repository
      .set(answers).map(_ => Done)

  def keepAlive(userId: String): Future[Boolean] =
    repository.keepAlive(userId)

  def clear(userId: String): Future[Boolean] =
    repository.clear(userId)
}
