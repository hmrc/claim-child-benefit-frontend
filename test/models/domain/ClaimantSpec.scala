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

import generators.ModelGenerators
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino

class ClaimantSpec extends AnyFreeSpec with Matchers with ModelGenerators with OptionValues {

  ".writes" - {

    "must write a UK/CTA claimant who has always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = UkCtaClaimantAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "UK_OR_CTA",
        "alwaysLivedInUK" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a UK/CTA claimant who has not always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = UkCtaClaimantNotAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        last3MonthsInUK = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "UK_OR_CTA",
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a non-UK/CTA claimant who has always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = NonUkCtaClaimantAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        nationality = Nationality.Eea,
        rightToReside = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "EEA",
        "alwaysLivedInUK" -> true,
        "rightToReside" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a non-UK/CTA claimant who has not always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = NonUkCtaClaimantNotAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        last3MonthsInUK = true,
        nationality = Nationality.Eea,
        rightToReside = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "EEA",
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> true,
        "rightToReside" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }
  }
}
