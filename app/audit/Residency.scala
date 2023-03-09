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

package audit

import play.api.libs.json.{Json, Writes}

import java.time.LocalDate

trait Residency

object Residency {

  case object AlwaysLivedInUk extends Residency

  def build(residency: models.JourneyModel.Residency): Residency =
    residency match {
      case models.JourneyModel.Residency.AlwaysLivedInUk =>
        Residency.AlwaysLivedInUk

      case models.JourneyModel.Residency.LivedInUkAndAbroad(usualCountry, arrivalDate, employmentStatus, countriesWorked, countriesReceivedBenefits) =>
        Residency.LivedInUkAndAbroad(usualCountry.map(_.name), arrivalDate, employmentStatus.map(_.toString), countriesWorked.map(_.name), countriesReceivedBenefits.map(_.name))

      case models.JourneyModel.Residency.AlwaysLivedAbroad(usualCountry, employmentStatus, countriesWorked, countriesReceivedBenefits) =>
        Residency.AlwaysLivedAbroad(usualCountry.name, employmentStatus.map(_.toString), countriesWorked.map(_.name), countriesReceivedBenefits.map(_.name))
    }

  final case class LivedInUkAndAbroad(usualCountryOfResidence: Option[String], arrivalDate: Option[LocalDate], employmentStatus: Set[String], countriesWorked: List[String], countriesReceivedBenefits: List[String]) extends Residency

  object LivedInUkAndAbroad {
    implicit lazy val writes: Writes[LivedInUkAndAbroad] = Writes { a =>

      val arrivalDateJson = a.arrivalDate.map(d => Json.obj("arrivalDate" -> d)).getOrElse(Json.obj())
      val countryJson = a.usualCountryOfResidence.map(c => Json.obj("usualCountryOfResidence" -> c, "usuallyLivesInUk" -> false)).getOrElse(Json.obj("usuallyLivesInUk" -> true))
      val countriesWorkedJson = if (a.countriesWorked.nonEmpty) Json.obj("countriesRecentlyWorked" -> a.countriesWorked) else Json.obj()
      val countreisReceivedBenefitsJson = if (a.countriesReceivedBenefits.nonEmpty) Json.obj("countriesRecentlyReceivedBenefits" -> a.countriesReceivedBenefits) else Json.obj()
      val employmentJson = if (a.employmentStatus.nonEmpty) Json.obj("employmentStatus" -> a.employmentStatus) else Json.obj()

      Json.obj(
        "alwaysLivedInUk" -> false
      ) ++ arrivalDateJson ++ countryJson ++ countriesWorkedJson ++ countreisReceivedBenefitsJson ++ employmentJson
    }
  }

  final case class AlwaysLivedAbroad(usualCountryOfResidence: String, employmentStatus: Set[String], countriesWorked: List[String], countriesReceivedBenefits: List[String]) extends Residency

  object AlwaysLivedAbroad {
    implicit lazy val writes: Writes[AlwaysLivedAbroad] = Writes { a =>

      val countriesWorkedJson = if (a.countriesWorked.nonEmpty) Json.obj("countriesRecentlyWorked" -> a.countriesWorked) else Json.obj()
      val countreisReceivedBenefitsJson = if (a.countriesReceivedBenefits.nonEmpty) Json.obj("countriesRecentlyReceivedBenefits" -> a.countriesReceivedBenefits) else Json.obj()
      val employmentJson = if (a.employmentStatus.nonEmpty) Json.obj("employmentStatus" -> a.employmentStatus) else Json.obj()

      Json.obj(
        "alwaysLivedInUk" -> false,
        "usuallyLivesInUk" -> false,
        "usualCountryOfResidence" -> a.usualCountryOfResidence
      ) ++ countriesWorkedJson ++ countreisReceivedBenefitsJson ++ employmentJson
    }
  }

  implicit lazy val writes: Writes[Residency] = Writes {
    case AlwaysLivedInUk => Json.obj("alwaysLivedInUk" -> true)
    case a: LivedInUkAndAbroad => Json.toJson(a)(LivedInUkAndAbroad.writes)
    case a: AlwaysLivedAbroad => Json.toJson(a)(AlwaysLivedAbroad.writes)
  }
}
