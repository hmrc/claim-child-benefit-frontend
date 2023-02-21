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

package models.domain

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsString, Json}

class BiologicalSexSpec extends AnyFreeSpec with Matchers {

  ".build" - {

    "must return Female when given Female" in {

      BiologicalSex.build(models.ChildBiologicalSex.Female) mustEqual BiologicalSex.Female
    }

    "must return Male when given Male" in {

      BiologicalSex.build(models.ChildBiologicalSex.Male) mustEqual BiologicalSex.Male
    }

    "must return Unspecified when given Unspecified" in {

      BiologicalSex.build(models.ChildBiologicalSex.Unspecified) mustEqual BiologicalSex.Unspecified
    }
  }

  ".writes" - {

    "must write Female" in {

      Json.toJson[BiologicalSex](BiologicalSex.Female) mustEqual JsString("FEMALE")
    }

    "must write Male" in {

      Json.toJson[BiologicalSex](BiologicalSex.Male) mustEqual JsString("MALE")
    }

    "must write Unspecified" in {

      Json.toJson[BiologicalSex](BiologicalSex.Unspecified) mustEqual JsString("UNSPECIFIED")
    }
  }
}
