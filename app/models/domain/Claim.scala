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

import models.JourneyModel
import play.api.libs.json.{Json, OWrites, Writes}

import java.time.LocalDate

final case class Claim(
                        dateOfClaim: LocalDate,
                        claimant: Claimant,
                        partner: Option[Partner],
                        payment: Option[Payment],
                        children: List[Child],
                        otherEligibilityFailure: Boolean
                      )

object Claim extends CbsDateFormats {

  implicit lazy val writes: OWrites[Claim] = Json.writes

  def build(nino: String, model: JourneyModel): Claim =
    Claim(
      dateOfClaim = LocalDate.now,
      claimant = Claimant.build(nino, model),
      partner = model.relationship.partner.flatMap(Partner.build),
      payment = Payment.build(model.paymentPreference),
      children = model.children.toList.map(Child.build),
      otherEligibilityFailure = model.additionalInformation.nonEmpty
    )
}
