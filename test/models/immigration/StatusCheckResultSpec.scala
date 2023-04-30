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

package models.immigration

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

import java.time.LocalDate

class StatusCheckResultSpec extends AnyFreeSpec with Matchers with OptionValues {

  ".settledStatusStartDate" - {

    "must be the oldest date of any EUS ILR records that have not expired" in {

      val expiredDate = LocalDate.of(2000, 1, 1)
      val oldNonExpiredDate = LocalDate.of(2010, 1, 1)
      val recentNonExpiredDate = LocalDate.of(2020, 1, 1)

      val statuses = List(
        ImmigrationStatus(
          statusStartDate = recentNonExpiredDate,
          statusEndDate = None,
          productType = "EUS",
          immigrationStatus = "ILR",
          noRecourseToPublicFunds = false
        ),
        ImmigrationStatus(
          statusStartDate = oldNonExpiredDate,
          statusEndDate = Some(LocalDate.now.plusDays(1)),
          productType = "EUS",
          immigrationStatus = "ILR",
          noRecourseToPublicFunds = false
        ),
        ImmigrationStatus(
          statusStartDate = expiredDate,
          statusEndDate = Some(LocalDate.now.minusDays(1)),
          productType = "EUS",
          immigrationStatus = "ILR",
          noRecourseToPublicFunds = false
        )
      )

      val checkResult = StatusCheckResult(
        fullName = "foo",
        dateOfBirth = LocalDate.now,
        nationality = "foo",
        statuses = statuses
      )

      checkResult.settledStatusStartDate.value mustEqual oldNonExpiredDate
    }

    "must ignore records that are not EUS ILR" in {

      val eusNonIlrDate = LocalDate.of(2000, 1, 1)
      val nonEusIlrDate = LocalDate.of(2010, 1, 1)
      val eusIlrDate = LocalDate.of(2020, 1, 1)

      val statuses = List(
        ImmigrationStatus(
          statusStartDate = eusIlrDate,
          statusEndDate = None,
          productType = "EUS",
          immigrationStatus = "ILR",
          noRecourseToPublicFunds = false
        ),
        ImmigrationStatus(
          statusStartDate = nonEusIlrDate,
          statusEndDate = None,
          productType = "foo",
          immigrationStatus = "ILR",
          noRecourseToPublicFunds = false
        ),
        ImmigrationStatus(
          statusStartDate = eusNonIlrDate,
          statusEndDate = None,
          productType = "EUS",
          immigrationStatus = "foo",
          noRecourseToPublicFunds = false
        )
      )

      val checkResult = StatusCheckResult(
        fullName = "foo",
        dateOfBirth = LocalDate.now,
        nationality = "foo",
        statuses = statuses
      )

      checkResult.settledStatusStartDate.value mustEqual eusIlrDate
    }

    "must be None when the only EUS ILR records are expired" in {

      val nonEusIlrDate = LocalDate.of(2010, 1, 1)
      val eusIlrDate = LocalDate.of(2020, 1, 1)

      val statuses = List(
        ImmigrationStatus(
          statusStartDate = eusIlrDate,
          statusEndDate = Some(LocalDate.now.minusDays(1)),
          productType = "EUS",
          immigrationStatus = "ILR",
          noRecourseToPublicFunds = false
        ),
        ImmigrationStatus(
          statusStartDate = nonEusIlrDate,
          statusEndDate = None,
          productType = "foo",
          immigrationStatus = "bar",
          noRecourseToPublicFunds = false
        )
      )

      val checkResult = StatusCheckResult(
        fullName = "foo",
        dateOfBirth = LocalDate.now,
        nationality = "foo",
        statuses = statuses
      )

      checkResult.settledStatusStartDate must not be defined
    }

    "must be None when there are no EUS ILR records" in {

      val nonEusIlrDate = LocalDate.of(2010, 1, 1)

      val statuses = List(

        ImmigrationStatus(
          statusStartDate = nonEusIlrDate,
          statusEndDate = None,
          productType = "foo",
          immigrationStatus = "bar",
          noRecourseToPublicFunds = false
        )
      )

      val checkResult = StatusCheckResult(
        fullName = "foo",
        dateOfBirth = LocalDate.now,
        nationality = "foo",
        statuses = statuses
      )

      checkResult.settledStatusStartDate must not be defined
    }
  }
}

/*

lazy val settledStatusStartDate: Option[LocalDate] =
  statuses
    .flatMap(_.settledStatusStartDate)
    .sorted(Ordering.by[LocalDate, Long](_.toEpochDay))
    .headOption
 */