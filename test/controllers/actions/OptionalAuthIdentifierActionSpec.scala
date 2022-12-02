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

import auth.Retrievals._
import base.SpecBase
import config.FrontendAppConfig
import controllers.auth.{routes => authRoutes}
import models.requests.{AuthenticatedIdentifierRequest, UnauthenticatedIdentifierRequest}
import play.api.mvc.BodyParsers
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Individual, Organisation}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.auth.core.{AuthConnector, ConfidenceLevel, CredentialStrength, MissingBearerToken}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class OptionalAuthIdentifierActionSpec extends SpecBase {

  "OptionalAuthIdentifierAction" - {

    val application = applicationBuilder(userAnswers = None).build()
    val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
    val config = application.injector.instanceOf[FrontendAppConfig]

    val sessionId = "sessionId"
    val userId = "userId"
    val nino = "nino"

    "when the user is authenticated" - {

      "with weak credentials" - {

        "they must be redirected to uplift MFA" in {

          val authAction = new OptionalAuthIdentifierAction(
            new FakeAuthConnector(Some(Individual) ~ Some(CredentialStrength.weak) ~ Some(ConfidenceLevel.L250) ~ Some(userId) ~ Some(nino)),
            bodyParsers,
            config
          )
          val request = FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

          val result = authAction(a => Ok(a.userId))(request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/uplift-mfa?origin=CHB&continueUrl=http%3A%2F%2Flocalhost%3A11303%2F"
        }
      }

      "with strong credentials" - {

        "as an agent" - {

          "they must be redirected to Unsupported Affinity Group - Agent" in {

            val authAction = new OptionalAuthIdentifierAction(
              new FakeAuthConnector(Some(Agent) ~ Some(CredentialStrength.strong) ~ ConfidenceLevel.L50 ~ Some(userId) ~ Some(nino)),
              bodyParsers,
              config
            )
            val request = FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

            val result = authAction(a => Ok(a.userId))(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual authRoutes.AuthController.unsupportedAffinityGroupAgent.url
          }
        }

        "as an organisation user" - {

          "they must be redirected to Unsupported Affinity Group - Organisation" in {

            val authAction = new OptionalAuthIdentifierAction(
              new FakeAuthConnector(Some(Organisation) ~ Some(CredentialStrength.strong) ~ ConfidenceLevel.L50 ~ Some(userId) ~ Some(nino)),
              bodyParsers,
              config
            )
            val request = FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

            val result = authAction(a => Ok(a.userId))(request)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual authRoutes.AuthController.unsupportedAffinityGroupOrganisation("http://localhost:11303/").url
          }
        }

        "as an individual" - {

          "with confidence level less than 250" - {

            "must be redirected to uplift their confidence level" in {

              val authAction = new OptionalAuthIdentifierAction(
                new FakeAuthConnector(Some(Individual) ~ Some(CredentialStrength.strong) ~ ConfidenceLevel.L200 ~ Some(userId) ~ Some(nino)),
                bodyParsers,
                config
              )
              val request = FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

              val result = authAction(a => Ok(a.userId))(request)

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual "http://localhost:9948/iv-stub/uplift?origin=CHB&confidenceLevel=250&completionURL=http%3A%2F%2Flocalhost%3A11303%2F&failureURL=http%3A%2F%2Flocalhost%3A11303%2Ffill-online%2Fclaim-child-benefit%2Fidentity-verification%2Fcomplete%3FcontinueUrl%3D%252F"
            }
          }

          "with confidence level 250 or greater" - {

            "they must succeed with an AuthenticatedIdentifierRequest" in {

              val authAction = new OptionalAuthIdentifierAction(
                new FakeAuthConnector(Some(Individual) ~ Some(CredentialStrength.strong) ~ ConfidenceLevel.L250 ~ Some(userId) ~ Some(nino)),
                bodyParsers,
                config
              )
              val request = FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

              val result = authAction(a => a match {
                case x: AuthenticatedIdentifierRequest[_] => Ok(s"${x.userId} ${x.nino}")
                case y: UnauthenticatedIdentifierRequest[_] => Ok(y.userId)
              })(request)

              status(result) mustEqual OK
              contentAsString(result) mustEqual s"$userId $nino"
            }
          }
        }
      }
    }

    "when the user is unauthenticated" - {

      "must use an UnauthenticatedIdentifierRequest" in {

        val authAction = new OptionalAuthIdentifierAction(new FakeFailingAuthConnector(MissingBearerToken()), bodyParsers, config)
        val request = FakeRequest().withSession(SessionKeys.sessionId -> sessionId)

        val result = authAction(a => a match {
          case x: AuthenticatedIdentifierRequest[_] => Ok(s"${x.userId} ${x.nino}")
          case y: UnauthenticatedIdentifierRequest[_] => Ok(y.userId)
        })(request)

        status(result) mustEqual OK
        contentAsString(result) mustEqual sessionId
      }

      "must redirect to the session expired page when the user is unauthenticated and has no session identifier" in {

        val authAction = new OptionalAuthIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), bodyParsers, config)
        val request = FakeRequest()

        val result = authAction(a => Ok(a.userId))(request)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(controllers.routes.JourneyRecoveryController.onPageLoad().url)
      }
    }
  }

  class FakeAuthConnector[T](value: T) extends AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.fromTry(Try(value.asInstanceOf[A]))
  }

  class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {
    override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
      Future.failed(exceptionToReturn)
  }
}
