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

package models.journey

import models.{Country, EmploymentStatus}

import java.time.LocalDate

sealed trait Residency

object Residency {

  case object AlwaysLivedInUk extends Residency

  final case class LivedInUkAndAbroad(
                                       usualCountryOfResidence: Option[Country],
                                       arrivalDate: Option[LocalDate],
                                       employmentStatus: Set[EmploymentStatus],
                                       countriesWorked: List[Country],
                                       countriesReceivedBenefits: List[Country]
                                     ) extends Residency

  final case class AlwaysLivedAbroad(
                                      usualCountryOfResidence: Country,
                                      employmentStatus: Set[EmploymentStatus],
                                      countriesWorked: List[Country],
                                      countriesReceivedBenefits: List[Country]
                                    ) extends Residency
}
