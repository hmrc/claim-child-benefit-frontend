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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ScottishBirthCertificateDetailsSpec extends AnyFreeSpec with Matchers {

  ".display must space-separate the values" in {

    ScottishBirthCertificateDetails(123, 2022, 456).display `mustEqual` "123 2022 456"
  }

  ".brmsFormat must concatenate year, district then entry, left-padding entry with zeroes to three digits" in {

    ScottishBirthCertificateDetails(123, 2022, 45).brmsFormat `mustEqual` "2022123045"
  }
}
