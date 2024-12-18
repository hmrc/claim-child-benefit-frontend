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

package models.immigration

import generators.Generators
import models.immigration.ImmigrationStatus.{eus, ilr}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.LocalDate

class ImmigrationStatusSpec
    extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues {

  ".settledStatusStartDate" - {

    "must be the start date when product type is EUS, immigration status is ILR and there is no end date" in {

      forAll(datesBetween(LocalDate.now.minusYears(2), LocalDate.now)) { date =>
        ImmigrationStatus(
          statusStartDate = date,
          statusEndDate = None,
          productType = eus,
          immigrationStatus = ilr,
          noRecourseToPublicFunds = false
        ).settledStatusStartDate.value `mustEqual` date
      }
    }
    "must be the start date when product type is EUS, immigration status is ILR and the end date is today or in the future" in {

      forAll(
        datesBetween(LocalDate.now.minusYears(2), LocalDate.now),
        datesBetween(LocalDate.now, LocalDate.now.plusYears(1))
      ) { case (startDate, endDate) =>
        ImmigrationStatus(
          statusStartDate = startDate,
          statusEndDate = Some(endDate),
          productType = eus,
          immigrationStatus = ilr,
          noRecourseToPublicFunds = false
        ).settledStatusStartDate.value `mustEqual` startDate
      }
    }

    "must be None when product type is EUS, immigration status is ILR and there is an end date in the past" in {

      forAll(
        datesBetween(LocalDate.now.minusYears(2), LocalDate.now),
        datesBetween(LocalDate.now.minusYears(1), LocalDate.now.minusDays(1))
      ) { case (startDate, endDate) =>
        ImmigrationStatus(
          statusStartDate = startDate,
          statusEndDate = Some(endDate),
          productType = eus,
          immigrationStatus = ilr,
          noRecourseToPublicFunds = false
        ).settledStatusStartDate `must` not `be` defined
      }
    }

    "must be None when product type is EUS, and immigration status is not ILR" in {

      ImmigrationStatus(
        statusStartDate = LocalDate.now,
        statusEndDate = None,
        productType = eus,
        immigrationStatus = "foo",
        noRecourseToPublicFunds = false
      ).settledStatusStartDate `must` not `be` defined
    }

    "must be None when product type is not EUS" in {

      ImmigrationStatus(
        statusStartDate = LocalDate.now,
        statusEndDate = None,
        productType = "foo",
        immigrationStatus = ilr,
        noRecourseToPublicFunds = false
      ).settledStatusStartDate `must` not `be` defined
    }
  }
}
