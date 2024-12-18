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
import models.ChildBirthRegistrationCountry._
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate

class BirthRegistrationMatchingRequestSpec extends AnyFreeSpec with Matchers with ModelGenerators with OptionValues {

  ".apply" - {

    val minimalChildName = ChildName("first", None, "last")
    val fullChildName = ChildName("first", Some("middle"), "last")
    val dateOfBirth = LocalDate.now

    "must build a request model" - {

      "for children who have the minimum data required" - {

        "born in England" in {

          val result = BirthRegistrationMatchingRequest(None, minimalChildName, dateOfBirth, England)

          result.value `mustEqual` BirthRegistrationMatchingRequest(
            birthReferenceNumber = None,
            firstName = "first",
            additionalNames = None,
            lastName = "last",
            dateOfBirth = dateOfBirth,
            whereBirthRegistered = "england"
          )
        }

        "born in Wales" in {

          val result = BirthRegistrationMatchingRequest(None, minimalChildName, dateOfBirth, Wales)

          result.value `mustEqual` BirthRegistrationMatchingRequest(
            birthReferenceNumber = None,
            firstName = "first",
            additionalNames = None,
            lastName = "last",
            dateOfBirth = dateOfBirth,
            whereBirthRegistered = "wales"
          )
        }

        "born in Northern Ireland" in {

          val result = BirthRegistrationMatchingRequest(None, minimalChildName, dateOfBirth, NorthernIreland)

          result.value `mustEqual` BirthRegistrationMatchingRequest(
            birthReferenceNumber = None,
            firstName = "first",
            additionalNames = None,
            lastName = "last",
            dateOfBirth = dateOfBirth,
            whereBirthRegistered = "northern ireland"
          )
        }
      }

      "for children who have middle names and birth certificate numbers" in {

        val result = BirthRegistrationMatchingRequest(
          Some(BirthCertificateSystemNumber("123456789")),
          fullChildName,
          dateOfBirth,
          Scotland
        )

        result.value `mustEqual` BirthRegistrationMatchingRequest(
          birthReferenceNumber = Some("123456789"),
          firstName = "first",
          additionalNames = Some("middle"),
          lastName = "last",
          dateOfBirth = dateOfBirth,
          whereBirthRegistered = "scotland"
        )
      }

      "with spaces stripped for children who have Scottish birth certificate numbers with spaces" in {

        val result = BirthRegistrationMatchingRequest(
          Some(ScottishBirthCertificateDetails(123, 2022, 12)),
          minimalChildName,
          dateOfBirth,
          Scotland
        )

        result.value `mustEqual` BirthRegistrationMatchingRequest(
          birthReferenceNumber = Some("2022123012"),
          firstName = "first",
          additionalNames = None,
          lastName = "last",
          dateOfBirth = dateOfBirth,
          whereBirthRegistered = "scotland"
        )
      }
    }

    "must not build a request model" - {

      "for children born outside the UK" in {

        val result = BirthRegistrationMatchingRequest(None, minimalChildName, dateOfBirth, OtherCountry)

        result `must` `not` `be` defined
      }

      "for children whose country of birth registration is unknown" in {

        val result = BirthRegistrationMatchingRequest(None, minimalChildName, dateOfBirth, UnknownCountry)

        result `must` `not` `be` defined
      }
    }
  }
}
