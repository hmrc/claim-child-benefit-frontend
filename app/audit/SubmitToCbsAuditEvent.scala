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

import models.domain.Claim
import models.journey.JourneyModel
import play.api.libs.json.{Json, OWrites}

import java.util.UUID

final case class SubmitToCbsAuditEvent(
                                        applicant: Applicant,
                                        relationship: Relationship,
                                        children: List[Child],
                                        benefits: Option[Set[String]],
                                        paymentPreference: PaymentPreference,
                                        otherEligibilityFailReasons: List[String],
                                        claim: Claim,
                                        correlationId: String
                                      )

object SubmitToCbsAuditEvent {

  implicit lazy val writes: OWrites[SubmitToCbsAuditEvent] = Json.writes

  def from(model: JourneyModel, claim: Claim, correlationId: UUID): SubmitToCbsAuditEvent =
    SubmitToCbsAuditEvent(
      applicant = Applicant.build(model.applicant),
      relationship = Relationship.build(model.relationship),
      children = model.children.toList.map(Child.build),
      benefits = model.benefits.map(_.map(_.toString)),
      paymentPreference = PaymentPreference.build(model.paymentPreference),
      otherEligibilityFailReasons = model.otherEligibilityFailureReasons.map(_.toString).toList,
      claim = claim,
      correlationId = correlationId.toString
    )
}
