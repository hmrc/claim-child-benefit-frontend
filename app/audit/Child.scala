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

import models.journey.JourneyModel
import play.api.libs.json.{Json, Writes}

import java.time.LocalDate

final case class Child(
                        name: ChildName,
                        nameChangedByDeedPoll: Option[Boolean],
                        previousNames: List[ChildName],
                        biologicalSex: String,
                        dateOfBirth: LocalDate,
                        birthRegistrationCountry: String,
                        birthCertificateNumber: Option[String],
                        birthCertificateDetailsMatched: String,
                        relationshipToApplicant: String,
                        adoptingThroughLocalAuthority: Boolean,
                        previousClaimant: Option[PreviousClaimant],
                        guardian: Option[Guardian],
                        previousGuardian: Option[PreviousGuardian],
                        dateChildStartedLivingWithApplicant: Option[LocalDate]
                      )

object Child {

  implicit lazy val writes: Writes[Child] = Json.writes

  def build(child: JourneyModel.Child): Child =
    Child(
      name = ChildName.build(child.name),
      nameChangedByDeedPoll = child.nameChangedByDeedPoll,
      previousNames = child.previousNames.map(ChildName.build),
      biologicalSex = child.biologicalSex.toString,
      dateOfBirth = child.dateOfBirth,
      birthRegistrationCountry = child.countryOfRegistration.toString,
      birthCertificateNumber = child.birthCertificateNumber.map(_.display),
      birthCertificateDetailsMatched = child.birthCertificateDetailsMatched.toString,
      relationshipToApplicant = child.relationshipToApplicant.toString,
      adoptingThroughLocalAuthority = child.adoptingThroughLocalAuthority,
      previousClaimant = child.previousClaimant.map(PreviousClaimant.build),
      guardian = child.guardian.map(Guardian.build),
      previousGuardian = child.previousGuardian.map(PreviousGuardian.build),
      dateChildStartedLivingWithApplicant = child.dateChildStartedLivingWithApplicant
    )
}
