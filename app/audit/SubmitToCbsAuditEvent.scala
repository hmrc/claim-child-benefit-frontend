package audit

import models.JourneyModel
import models.domain.Claim
import play.api.libs.json.{Json, OWrites}

import java.util.UUID

final case class SubmitToCbsAuditEvent(
                                        applicant: Applicant,
                                        relationship: Relationship,
                                        children: List[Child],
                                        benefits: Option[Set[String]],
                                        paymentPreference: PaymentPreference,
                                        additionalInformation: Option[String],
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
      additionalInformation = model.additionalInformation,
      otherEligibilityFailReasons = model.otherEligibilityFailureReasons.map(_.toString).toList,
      claim = claim,
      correlationId = correlationId.toString
    )
}
