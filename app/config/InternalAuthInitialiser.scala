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

package config

import models.Done
import play.api.{Configuration, Logging}
import play.api.libs.json.{JsObject, Json, OFormat}
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}

abstract class InternalAuthTokenInitialiser {
  val initialised: Future[Done]
}

@Singleton
class NoOpInternalAuthTokenInitialiser @Inject() () extends InternalAuthTokenInitialiser {
  override val initialised: Future[Done] = Future.successful(Done)
}

@Singleton
class InternalAuthTokenInitialiserImpl @Inject() (
                                                   configuration: Configuration,
                                                   httpClient: HttpClientV2
                                                 )(implicit ec: ExecutionContext) extends InternalAuthTokenInitialiser with Logging {

  private val internalAuthService: Service =
    configuration.get[Service]("microservice.services.internal-auth")

  private val childBenefitToken: String =
    configuration.get[String]("internal-auth.token")

  private val homeOfficeImmigrationToken: String =
    configuration.get[String]("internal-auth.home-office-immigration-proxy.token")

  private val appName: String =
    configuration.get[String]("appName")

  override val initialised: Future[Done] =
    for {
      _ <- ensureAuthToken(childBenefitToken)
      _ <- ensureAuthToken(homeOfficeImmigrationToken)
    } yield Done

  Await.result(initialised, 30.seconds)

  private case class Resource(resourceType: String, resourceLocation: String, actions: Seq[String])

  private object Resource {
    implicit val formats: OFormat[Resource] = Json.format[Resource]
  }
  private case class Permission(token: String, principle: String, permissions: Seq[Resource])

  private object Permission {
    implicit val formats: OFormat[Permission] = Json.format[Permission]
  }

  private val permissions: Seq[Permission] = Seq(
    Permission(childBenefitToken, appName, Seq(Resource("claim-child-benefit", "*", List("*")))),
    Permission(homeOfficeImmigrationToken, appName, Seq(Resource("home-office-immigration-status-proxy", s"status/public-funds/nino/$appName", List("WRITE"))))
  )

  private def ensureAuthToken(token: String): Future[Done] = {
    authTokenIsValid(token).flatMap { isValid =>
      if (isValid) {
        logger.info("Auth token is already valid")
        Future.successful(Done)
      } else {
        createClientAuthToken()
      }
    }
  }

  private def createClientAuthToken(): Future[Done] = {
    logger.info("Initialising auth token")
    httpClient.post(url"${internalAuthService.baseUrl}/test-only/token")(HeaderCarrier())
      .withBody(Json.toJson(permissions))
      .execute
      .flatMap { response =>
        if (response.status == 201) {
          logger.info("Auth token initialised")
          Future.successful(Done)
        } else {
          Future.failed(new RuntimeException("Unable to initialise internal-auth token"))
        }
      }
  }

  private def authTokenIsValid(token: String): Future[Boolean] = {
    logger.info("Checking auth token")
    httpClient.get(url"${internalAuthService.baseUrl}/test-only/token")(HeaderCarrier())
      .setHeader("Authorization" -> token)
      .execute
      .map(_.status == 200)
  }
}
