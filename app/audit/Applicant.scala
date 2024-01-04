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

package audit

import models.journey
import play.api.libs.json.{Json, Writes}

import java.time.LocalDate

final case class Applicant(
                            name: AdultName,
                            previousFamilyNames: List[String],
                            dateOfBirth: LocalDate,
                            nationalInsuranceNumber: Option[String],
                            currentAddress: Address,
                            previousAddress: Option[Address],
                            telephoneNumber: String,
                            nationalities: Seq[String],
                            residency: Residency,
                            memberOfHMForcesOrCivilServantAbroad: Boolean,
                            currentlyClaimingChildBenefit: String,
                            changedDesignatoryDetails: Option[Boolean],
                            correspondenceAddress: Option[Address]
                          )

object Applicant {

  implicit lazy val writes: Writes[Applicant] = Json.writes

  def build(applicant: journey.Applicant): Applicant =
    Applicant(
      name = AdultName.build(applicant.name),
      previousFamilyNames = applicant.previousFamilyNames.map(_.lastName),
      dateOfBirth = applicant.dateOfBirth,
      nationalInsuranceNumber = applicant.nationalInsuranceNumber,
      currentAddress = Address.build(applicant.currentAddress),
      previousAddress = applicant.previousAddress.map(Address.build),
      telephoneNumber = applicant.telephoneNumber,
      nationalities = applicant.nationalities.toList.map(_.name),
      residency = Residency.build(applicant.residency),
      memberOfHMForcesOrCivilServantAbroad = applicant.memberOfHMForcesOrCivilServantAbroad,
      currentlyClaimingChildBenefit = applicant.currentlyReceivingChildBenefit.toString,
      changedDesignatoryDetails = applicant.changedDesignatoryDetails,
      correspondenceAddress = applicant.correspondenceAddress.map(Address.build)
    )
}
