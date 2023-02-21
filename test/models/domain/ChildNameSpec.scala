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

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ChildNameSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  ".build" - {

    "must create a ChildName" in {

      forAll(arbitrary[String], Gen.option(arbitrary[String]), arbitrary[String]) {
        case (first, middle, last) =>

          val name = models.ChildName(first, middle, last)
          val result = ChildName.build(name)

          result mustEqual ChildName(first, middle, last)
      }
    }
  }
}
