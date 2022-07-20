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

package utils

import cats.implicits._
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class BooleanMonadSyntaxSpec extends AnyFreeSpec with Matchers with OptionValues {

  import MonadOps._

  "&&" - {

    "must return true when both operands are true" in {
      (true.some && true.some).value mustEqual true
    }

    "must return false when both operands are false" in {
      (false.some && false.some).value mustEqual false
    }

    "must return false when the second operand is None" in {
      (false.some && None).value mustEqual false
    }

    "must return None when the first operand is None" in {
      (Option.empty[Boolean] && true.some) mustNot be (defined)
    }

    "must return None when both operands are None" in {
      (Option.empty[Boolean] && None) mustNot be (defined)
    }
  }
}
