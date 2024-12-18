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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class StringNormaliserSpec extends AnyFreeSpec with Matchers with StringNormaliser {

  ".normalise" - {

    "must replace accented characters with unaccented equivalents" in {

      val accentedChars = Seq(
        'À' to 'Å',
        'Ç' to 'Ö',
        'Ø' to 'Ý',
        'à' to 'å',
        'ç' to 'ö',
        'ø' to 'ý',
        'Ā' to 'ľ',
        'Ł' to 'ň',
        'Ŋ' to 'ő',
        'Ŕ' to 'ſ'
      ).flatten :+ 'ÿ'

      val unaccentedChars = Seq(
        'a' to 'z',
        'A' to 'Z'
      ).flatten

      val input = accentedChars.mkString
      normalise(input).toCharArray.filterNot(unaccentedChars.contains) `mustBe` empty
    }

    "must replace `’` with `'`" in {

      normalise("’") `mustEqual` "'"
    }
  }
}
