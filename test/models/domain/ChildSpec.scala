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
import play.api.libs.json.Json

import java.time.LocalDate

class ChildSpec extends AnyFreeSpec with Matchers {

  ".writes" - {

    "must write a child" in {

      val dateOfBirth = LocalDate.of(2022, 1, 2)
      val child = Child(
        name = ChildName("first", Some("middle"), "last"),
        gender = BiologicalSex.Female,
        dateOfBirth = dateOfBirth,
        birthRegistrationNumber = Some("123456789"),
        crn = None,
        countryOfRegistration = CountryOfRegistration.EnglandWales,
        dateOfBirthVerified = false,
        livingWithClaimant = true,
        claimantIsParent = true,
        adoptionStatus = false
      )

      val expectedJson = Json.obj(
        "name" -> Json.obj(
          "forenames" -> "first",
          "middleNames" -> "middle",
          "surname" -> "last"
        ),
        "gender" -> "FEMALE",
        "dateOfBirth" -> "02/01/2022",
        "birthRegistrationNumber" -> "123456789",
        "countryOfRegistration" -> "ENGLAND_WALES",
        "dateOfBirthVerified" -> false,
        "livingWithClaimant" -> true,
        "claimantIsParent" -> true,
        "adoptionStatus" -> false
      )

      Json.toJson(child) mustEqual expectedJson
    }
  }
}
