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

package views

import base.SpecBase
import generators.ModelGenerators
import models.requests._
import org.scalacheck.Arbitrary.arbitrary
import play.api.test.FakeRequest
import uk.gov.hmrc.domain.Nino
import views.ViewUtils.RequestOps

class RequestOpsSpec extends SpecBase with ModelGenerators {

  private val baseRequest = FakeRequest("", "")
  private val userId = "id"
  private val nino = arbitrary[Nino].sample.value.nino

  ".signedIn" - {

    "must be true for a DataRequest that wraps an AuthenticatedIdentifierAction" in {

      val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino)
      val dataRequest = DataRequest(identifierRequest, userId, emptyUserAnswers)

      dataRequest.signedIn mustBe true
    }

    "must be false for a DataRequest that wraps an UnauthenticatedIdentifierAction" in {

      val identifierRequest = UnauthenticatedIdentifierRequest(baseRequest, userId)
      val dataRequest = DataRequest(identifierRequest, userId, emptyUserAnswers)

      dataRequest.signedIn mustBe false
    }

    "must be true for an OptionalDataRequest that wraps an AuthenticatedIdentifierAction" in {

      val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino)
      val dataRequest = OptionalDataRequest(identifierRequest, userId, None)

      dataRequest.signedIn mustBe true
    }
    
    "must be false for an OptionalDataRequest that wraps an UnauthenticatedIdentifierAction" in {

      val identifierRequest = UnauthenticatedIdentifierRequest(baseRequest, userId)
      val dataRequest = OptionalDataRequest(identifierRequest, userId, None)

      dataRequest.signedIn mustBe false
    }

    "must be true for an AuthenticatedIdentifierAction" in {

      val identifierRequest = AuthenticatedIdentifierRequest(baseRequest, userId, nino)

      identifierRequest.signedIn mustBe true
    }

    "must be false for an UnauthenticatedIdentifierAction" in {

      val identifierRequest = UnauthenticatedIdentifierRequest(baseRequest, userId)

      identifierRequest.signedIn mustBe false
    }

    "must be false for a standard request" in {

      baseRequest.signedIn mustBe false
    }
  }
}
