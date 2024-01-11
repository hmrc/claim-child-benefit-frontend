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

import generators.ModelGenerators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.{DefaultMessagesApi, Messages}
import play.api.test.FakeRequest

class AddressSpec extends AnyFreeSpec with Matchers with OptionValues with ScalaCheckPropertyChecks with ModelGenerators {

  val testMessages = Map(
    "default" -> Map("title" -> "foo bar")
  )
  val messagesApi       = new DefaultMessagesApi(testMessages)
  implicit val messages: Messages = messagesApi.preferred(FakeRequest("GET", "/"))

  ".possibleLocalAuthority" - {

    "for a UK address" - {

      val address = UkAddress("line1", None, "town", None, "AA11AA")

      "must be true when any line contains a keyword as a standalone word" in {

        address.copy(line1 = "1 The    council  street").possibleLocalAuthorityAddress mustBe true
        address.copy(line1 = " Authority  Plaza").possibleLocalAuthorityAddress mustBe true
        address.copy(line2 = Some("COUNCIL OFFICE")).possibleLocalAuthorityAddress mustBe true
        address.copy(line2 = Some("  The District")).possibleLocalAuthorityAddress mustBe true
        address.copy(townOrCity = "metropolitan").possibleLocalAuthorityAddress mustBe true
        address.copy(county = Some("civic borough")).possibleLocalAuthorityAddress mustBe true
      }

      "must be false when any line contains a keyword as part of a larger word, but there no standalone keywords" in {

        address.copy(line1 = "1 CouncilStreet").possibleLocalAuthorityAddress mustBe false
        address.copy(line1 = " AuthorityPlaza").possibleLocalAuthorityAddress mustBe false
        address.copy(line2 = Some("COUNCILOFFICE")).possibleLocalAuthorityAddress mustBe false
        address.copy(line2 = Some("  TheDistrict")).possibleLocalAuthorityAddress mustBe false
        address.copy(townOrCity = "Knaresborough").possibleLocalAuthorityAddress mustBe false
        address.copy(county = Some("civicborough")).possibleLocalAuthorityAddress mustBe false
      }
    }

    "for an international address" - {

      val address = InternationalAddress("line1", None, "town", None, Some("AA11AA"), Country("ES", "Spain"))

      "must be false regardless of whether any line contains a keyword as a standalone word or as part of another word" in {

        address.copy(line1 = "1 The    council  street").possibleLocalAuthorityAddress mustBe false
        address.copy(line1 = " Authority  Plaza").possibleLocalAuthorityAddress mustBe false
        address.copy(line2 = Some("COUNCIL OFFICE")).possibleLocalAuthorityAddress mustBe false
        address.copy(line2 = Some("  The District")).possibleLocalAuthorityAddress mustBe false
        address.copy(townOrCity = "metropolitan").possibleLocalAuthorityAddress mustBe false
        address.copy(stateOrRegion = Some("civic borough")).possibleLocalAuthorityAddress mustBe false
        address.copy(line1 = "1 CouncilStreet").possibleLocalAuthorityAddress mustBe false
        address.copy(line1 = " AuthorityPlaza").possibleLocalAuthorityAddress mustBe false
        address.copy(line2 = Some("COUNCILOFFICE")).possibleLocalAuthorityAddress mustBe false
        address.copy(line2 = Some("  TheDistrict")).possibleLocalAuthorityAddress mustBe false
        address.copy(townOrCity = "Knaresborough").possibleLocalAuthorityAddress mustBe false
        address.copy(stateOrRegion = Some("civicborough")).possibleLocalAuthorityAddress mustBe false
      }
    }

    "for an NPS address" - {

      "with a GB country code" - {

        val address = NPSAddress("line1", None, None, None, None, None, Some(Country("GB", "United Kingdom")))

        "must be true when any line contains a keyword as a standalone word" in {

          address.copy(line1 = "1 The    council  street").possibleLocalAuthorityAddress mustBe true
          address.copy(line1 = " Authority  Plaza").possibleLocalAuthorityAddress mustBe true
          address.copy(line2 = Some("COUNCIL OFFICE")).possibleLocalAuthorityAddress mustBe true
          address.copy(line3 = Some("  The District")).possibleLocalAuthorityAddress mustBe true
          address.copy(line4 = Some("metropolitan")).possibleLocalAuthorityAddress mustBe true
          address.copy(line5 = Some("civic borough")).possibleLocalAuthorityAddress mustBe true
        }

        "must be false when any line contains a keyword as part of a larger word, but there no standalone keywords" in {

          address.copy(line1 = "1 CouncilStreet").possibleLocalAuthorityAddress mustBe false
          address.copy(line1 = " AuthorityPlaza").possibleLocalAuthorityAddress mustBe false
          address.copy(line2 = Some("COUNCILOFFICE")).possibleLocalAuthorityAddress mustBe false
          address.copy(line3 = Some("  TheDistrict")).possibleLocalAuthorityAddress mustBe false
          address.copy(line4 = Some("Knaresborough")).possibleLocalAuthorityAddress mustBe false
          address.copy(line5 = Some("civicborough")).possibleLocalAuthorityAddress mustBe false
        }
      }

      "with no country code" - {

        val address = NPSAddress("line1", None, None, None, None, None, None)

        "must be true when any line contains a keyword as a standalone word" in {

          address.copy(line1 = "1 The    council  street").possibleLocalAuthorityAddress mustBe true
          address.copy(line1 = " Authority  Plaza").possibleLocalAuthorityAddress mustBe true
          address.copy(line2 = Some("COUNCIL OFFICE")).possibleLocalAuthorityAddress mustBe true
          address.copy(line3 = Some("  The District")).possibleLocalAuthorityAddress mustBe true
          address.copy(line4 = Some("metropolitan")).possibleLocalAuthorityAddress mustBe true
          address.copy(line5 = Some("civic borough")).possibleLocalAuthorityAddress mustBe true
        }

        "must be false when any line contains a keyword as part of a larger word, but there no standalone keywords" in {

          address.copy(line1 = "1 CouncilStreet").possibleLocalAuthorityAddress mustBe false
          address.copy(line1 = " AuthorityPlaza").possibleLocalAuthorityAddress mustBe false
          address.copy(line2 = Some("COUNCILOFFICE")).possibleLocalAuthorityAddress mustBe false
          address.copy(line3 = Some("  TheDistrict")).possibleLocalAuthorityAddress mustBe false
          address.copy(line4 = Some("Knaresborough")).possibleLocalAuthorityAddress mustBe false
          address.copy(line5 = Some("civicborough")).possibleLocalAuthorityAddress mustBe false
        }
      }

      "with a non-GB country code" - {

        val address = NPSAddress("line1", None, None, None, None, None, Some(Gen.oneOf(Country.internationalCountries).sample.value))

        "must be false regardless of whether any line contains a keyword as a standalone word or as part of another word" in {

          address.copy(line1 = "1 The    council  street").possibleLocalAuthorityAddress mustBe false
          address.copy(line1 = " Authority  Plaza").possibleLocalAuthorityAddress mustBe false
          address.copy(line2 = Some("COUNCIL OFFICE")).possibleLocalAuthorityAddress mustBe false
          address.copy(line3 = Some("  The District")).possibleLocalAuthorityAddress mustBe false
          address.copy(line4 = Some("metropolitan")).possibleLocalAuthorityAddress mustBe false
          address.copy(line5 = Some("civic borough")).possibleLocalAuthorityAddress mustBe false
          address.copy(line1 = "1 CouncilStreet").possibleLocalAuthorityAddress mustBe false
          address.copy(line1 = " AuthorityPlaza").possibleLocalAuthorityAddress mustBe false
          address.copy(line2 = Some("COUNCILOFFICE")).possibleLocalAuthorityAddress mustBe false
          address.copy(line3 = Some("  TheDistrict")).possibleLocalAuthorityAddress mustBe false
          address.copy(line4 = Some("Knaresborough")).possibleLocalAuthorityAddress mustBe false
          address.copy(line5 = Some("civicborough")).possibleLocalAuthorityAddress mustBe false
        }
      }
    }
  }
}
