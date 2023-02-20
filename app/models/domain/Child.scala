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

import models.ChildBirthRegistrationCountry.{England, Scotland, Wales}
import models.JourneyModel
import play.api.libs.json.{Json, OWrites, Writes}

import java.time.LocalDate

final case class Child(
                        name: ChildName,
                        gender: BiologicalSex,
                        dateOfBirth: LocalDate,
                        birthRegistrationNumber: Option[String],
                        crn: Option[String],
                        countryOfRegistration: CountryOfRegistration,
                        dateOfBirthVerified: Option[Boolean],
                        livingWithClaimant: Boolean,
                        claimantIsParent: Boolean,
                        adoptionStatus: Boolean
                      )

object Child {

  def build(child: JourneyModel.Child): Child = {

    val adoptionStatus =
      child.adoptingThroughLocalAuthority ||
        child.relationshipToApplicant == models.ApplicantRelationshipToChild.AdoptedChild

    val dateOfBirthVerified = child.countryOfRegistration match {
      case England | Scotland | Wales => None
      case _ => Some(false)
    }

    Child(
      name = ChildName.build(child.name),
      gender = BiologicalSex.build(child.biologicalSex),
      dateOfBirth = child.dateOfBirth,
      birthRegistrationNumber = child.birthCertificateNumber.map(_.brmsFormat),
      crn = None,
      countryOfRegistration = CountryOfRegistration.build(child.countryOfRegistration),
      dateOfBirthVerified = dateOfBirthVerified,
      livingWithClaimant = child.guardian.isEmpty,
      claimantIsParent = child.relationshipToApplicant != models.ApplicantRelationshipToChild.Other,
      adoptionStatus = adoptionStatus
    )
  }

  private implicit val dateWrites: Writes[LocalDate] = CbsDateFormats.localDateWrites

  implicit lazy val writes: OWrites[Child] = Json.writes
}
