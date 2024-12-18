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

package models.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{Json, OWrites}

import java.time.LocalDate

class CbsDateFormatSpec extends AnyFreeSpec with Matchers {

  case class TestModel(date: LocalDate)

  object TestModel extends CbsDateFormats {

    implicit val writes: OWrites[TestModel] = Json.writes
  }

  "CbsDateWrites" - {

    "must write dates in dd/mm/yyyy format" in {

      val model = TestModel(LocalDate.of(2023, 1, 2))
      val json = Json.toJson(model)

      json `mustEqual` Json.obj("date" -> "02/01/2023")
    }
  }
}
