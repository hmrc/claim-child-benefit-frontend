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

final case class Partner(
                          name: AdultName,
                          dateOfBirth: LocalDate,
                          nationalities: Seq[String],
                          currentlyClaimingChildBenefit: String,
                          nationalInsuranceNumber: Option[String],
                          memberOfHMForcesOrCivilServantAbroad: Boolean,
                          eldestChild: Option[EldestChild],
                          countriesWorked: Seq[String],
                          countriesReceivedBenefits: Seq[String],
                          employmentStatus: Set[String]
                        )

object Partner {

  implicit lazy val writes: Writes[Partner] = Json.writes

  def build(partner: JourneyModel.Partner): Partner =
    Partner(
      name = AdultName.build(partner.name),
      dateOfBirth = partner.dateOfBirth,
      nationalities = partner.nationalities.toList.map(_.name),
      nationalInsuranceNumber = partner.nationalInsuranceNumber.map(_.value),
      currentlyClaimingChildBenefit = partner.currentlyClaimingChildBenefit.toString,
      memberOfHMForcesOrCivilServantAbroad = partner.memberOfHMForcesOrCivilServantAbroad,
      eldestChild = partner.eldestChild.map(EldestChild.build),
      countriesWorked = partner.countriesWorked.map(_.name),
      countriesReceivedBenefits = partner.countriesReceivedBenefits.map(_.name),
      employmentStatus = partner.employmentStatus.map(_.toString)
    )
}
