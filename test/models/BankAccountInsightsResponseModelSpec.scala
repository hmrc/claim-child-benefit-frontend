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

package models

import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class BankAccountInsightsResponseModelSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  ".riskAboveTolerance" - {

    "must be true if the risk score is 100 or greater" in {

      forAll(Gen.choose(100, Int.MaxValue)) { riskScore =>
        val insight = BankAccountInsightsResponseModel("foo", riskScore, "bar")

        insight.riskAboveTolerance `mustBe` true
      }
    }

    "must be false if the risk score is less than 100" in {

      forAll(Gen.choose(0, 99)) { riskScore =>
        val insight = BankAccountInsightsResponseModel("foo", riskScore, "bar")

        insight.riskAboveTolerance `mustBe` false
      }
    }

    "must deserialise from JSON with `correlationId`" in {

      val json = Json.obj(
        "correlationId" -> "correlation",
        "riskScore"     -> 0,
        "reason"        -> "reason"
      )

      val expectedResult = BankAccountInsightsResponseModel("correlation", 0, "reason")

      json.validate[BankAccountInsightsResponseModel] `mustEqual` JsSuccess(expectedResult)
    }

    "must deserialise from JSON with `bankAccountInsightsCorrelationId`" in {

      val json = Json.obj(
        "bankAccountInsightsCorrelationId" -> "correlation",
        "riskScore"                        -> 0,
        "reason"                           -> "reason"
      )

      val expectedResult = BankAccountInsightsResponseModel("correlation", 0, "reason")

      json.validate[BankAccountInsightsResponseModel] `mustEqual` JsSuccess(expectedResult)
    }

    "must deserialise from JSON with both 'correlationId' and `bankAccountInsightsCorrelationId`" in {

      val json = Json.obj(
        "bankAccountInsightsCorrelationId" -> "correlation",
        "correlationId"                    -> "correlation",
        "riskScore"                        -> 0,
        "reason"                           -> "reason"
      )

      val expectedResult = BankAccountInsightsResponseModel("correlation", 0, "reason")

      json.validate[BankAccountInsightsResponseModel] `mustEqual` JsSuccess(expectedResult)
    }
  }
}
