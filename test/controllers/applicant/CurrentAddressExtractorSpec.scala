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

package controllers.applicant

import base.SpecBase
import controllers.{routes => baseRoutes}
import generators.ModelGenerators
import models.{InternationalAddress, UkAddress, UserAnswers}
import models.requests.{DataRequest, UnauthenticatedIdentifierRequest}
import org.scalacheck.Arbitrary.arbitrary
import pages.applicant.{ApplicantCurrentInternationalAddressPage, ApplicantCurrentUkAddressPage}
import play.api.mvc.Results.{Ok, Redirect}
import play.api.libs.json.Json
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest

import scala.concurrent.Future

class CurrentAddressExtractorSpec extends SpecBase with ModelGenerators {

  private def buildRequest(answers: UserAnswers): DataRequest[AnyContent] =
    DataRequest(UnauthenticatedIdentifierRequest(FakeRequest(), "id"), answers.id, answers)

  private class TestController() extends CurrentAddressExtractor {

    def get()(implicit request: DataRequest[AnyContent]): Future[Result] =
      getCurrentAddress {
        currentAddress =>
          Future.successful(Ok(Json.toJson(currentAddress)))
      }
  }

  ".getCurrentAddress" - {

    "must pass a UK address into the provided block when it exists in user answers" in {

      val ukAddress = arbitrary[UkAddress].sample.value
      val answers = emptyUserAnswers.set(ApplicantCurrentUkAddressPage, ukAddress).success.value
      implicit val request = buildRequest(answers)

      val controller = new TestController()

      controller.get().futureValue mustEqual Ok(Json.toJson(ukAddress))
    }

    "must pass an international address into the provided block when it exists in user answers" in {

      val internationalAddress = arbitrary[InternationalAddress].sample.value
      val answers = emptyUserAnswers.set(ApplicantCurrentInternationalAddressPage, internationalAddress).success.value
      implicit val request = buildRequest(answers)

      val controller = new TestController()

      controller.get().futureValue mustEqual Ok(Json.toJson(internationalAddress))
    }

    "must pass a UK address into the provided block when both UK and international addresses exist in user answers" in {

      val ukAddress = arbitrary[UkAddress].sample.value
      val internationalAddress = arbitrary[InternationalAddress].sample.value
      val answers =
        emptyUserAnswers
          .set(ApplicantCurrentUkAddressPage, ukAddress).success.value
          .set(ApplicantCurrentInternationalAddressPage, internationalAddress).success.value

      implicit val request = buildRequest(answers)

      val controller = new TestController()

      controller.get().futureValue mustEqual Ok(Json.toJson(ukAddress))
    }

    "must redirect to Journey Recovery when neither a UK nor an international address exists in user answers" in {

      implicit val request = buildRequest(emptyUserAnswers)
      val controller = new TestController()

      controller.get().futureValue mustEqual Redirect(baseRoutes.JourneyRecoveryController.onPageLoad())
    }
  }
}
