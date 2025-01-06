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

import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class ChildNameSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  ".build" - {

    val alphaString = Gen.nonEmptyListOf(Gen.alphaChar).map(_.mkString)

    "must create a ChildName" in {

      forAll(alphaString, Gen.option(alphaString), alphaString) { case (first, middle, last) =>
        val name = models.ChildName(first, middle, last)
        val result = ChildName.build(name)

        result `mustEqual` ChildName(first, middle, last)
      }
    }

    "must normalise accented characters in first name, middle names and last name fields" in {

      val name = models.ChildName("āăą", Some("îïĩí"), "šŝś")
      val result = ChildName.build(name)

      result `mustEqual` ChildName("aaa", Some("iiii"), "sss")
    }

    "must replace ’ with ' in first name, middle names and last name fields" in {

      val name = models.ChildName("a’b", Some("c’d"), "e’f")
      val result = ChildName.build(name)

      result `mustEqual` ChildName("a'b", Some("c'd"), "e'f")
    }
  }
}
