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

class BankAccountDetailsSpec extends AnyFreeSpec with Matchers {

  ".sortCodeTrimmed" - {

    "must remove spaces and dashes from the sort code" in {

      val details1 = BankAccountDetails("name on account", "bank name", "12 34 56", "00123456", None)
      val details2 = BankAccountDetails("name on account", "bank name", "12-34-56", "00123456", None)

      details1.sortCodeTrimmed mustEqual "123456"
      details2.sortCodeTrimmed mustEqual "123456"
    }
  }

  ".accountNumberPadded" - {

    "must left-pad the account number with zeros" in {

      val details1 = BankAccountDetails("name on account", "bank name", "123456", "123456", None)
      val details2 = BankAccountDetails("name on account", "bank name", "123456", "1234567", None)

      details1.accountNumberPadded mustEqual "00123456"
      details2.accountNumberPadded mustEqual "01234567"
    }
  }
}
