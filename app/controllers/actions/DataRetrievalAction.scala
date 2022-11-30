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

import models.requests.{AuthenticatedIdentifierRequest, IdentifierRequest, OptionalDataRequest, UnauthenticatedIdentifierRequest}
import play.api.mvc.ActionTransformer
import services.UserDataService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(
                                         val userDataService: UserDataService
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction {

  override protected def transform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = {

    val nino: Option[String] = request match {
      case a: AuthenticatedIdentifierRequest[_]   => Some(a.nino)
      case _: UnauthenticatedIdentifierRequest[_] => None
    }

    userDataService.get(request.userId).map { maybeAnswers =>
      OptionalDataRequest(request, request.userId, maybeAnswers.map(_.copy(nino = nino)))
    }
  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, OptionalDataRequest]
