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

final case class DownloadAuditEvent(
                                     applicant: Applicant,
                                     relationship: Relationship,
                                     children: List[Child],
                                     benefits: Option[Set[String]],
                                     paymentPreference: PaymentPreference,
                                     additionalInformation: Option[String],
                                     userAuthenticated: Boolean,
                                     reasonsNotToSubmit: List[String],
                                     otherEligibilityFailReasons: List[String]
                                   )

object DownloadAuditEvent {

  def from(model: JourneyModel): DownloadAuditEvent =
    DownloadAuditEvent(
      applicant = Applicant.build(model.applicant),
      relationship = Relationship.build(model.relationship),
      children = model.children.toList.map(Child.build),
      benefits = model.benefits.map(_.map(_.toString)),
      paymentPreference = PaymentPreference.build(model.paymentPreference),
      additionalInformation = model.additionalInformation,
      userAuthenticated = model.userAuthenticated,
      reasonsNotToSubmit = model.reasonsNotToSubmit.map(_.toString).toList,
      otherEligibilityFailReasons = model.otherEligibilityFailureReasons.map(_.toString).toList
    )

  implicit lazy val writes: Writes[DownloadAuditEvent] = Json.writes
}