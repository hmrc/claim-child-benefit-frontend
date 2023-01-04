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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsObject, Json}

class IvResultSpec extends AnyFreeSpec with Matchers {

  private def json(result: String): JsObject =
    Json.obj(
      "progress" -> Json.obj(
        "result" -> result
      )
    )

  "reads" - {

    "must read Success from an Iv Result" in {
      json("Success").as[IvResult] mustEqual IvResult.Success
    }

    "must read Incomplete from an Iv Result" in {
      json("Incomplete").as[IvResult] mustEqual IvResult.Incomplete
    }
    
    "must read FailedMatching from an Iv Result" in {
      json("FailedMatching").as[IvResult] mustEqual IvResult.FailedMatching
    }

    "must read FailedIdentityVerification from an Iv Result" in {
      json("FailedIdentityVerification").as[IvResult] mustEqual IvResult.FailedIdentityVerification
    }

    "must read InsufficientEvidence from an Iv Result" in {
      json("InsufficientEvidence").as[IvResult] mustEqual IvResult.InsufficientEvidence
    }

    "must read LockedOut from an Iv Result" in {
      json("LockedOut").as[IvResult] mustEqual IvResult.LockedOut
    }

    "must read UserAborted from an Iv Result" in {
      json("UserAborted").as[IvResult] mustEqual IvResult.UserAborted
    }

    "must read Timeout from an Iv Result" in {
      json("Timeout").as[IvResult] mustEqual IvResult.Timeout
    }

    "must read TechnicalIssue from an Iv Result" in {
      json("TechnicalIssue").as[IvResult] mustEqual IvResult.TechnicalIssue
    }

    "must read PreconditionFailed from an Iv Result" in {
      json("PreconditionFailed").as[IvResult] mustEqual IvResult.IvPreconditionFailed
    }
  }
}
