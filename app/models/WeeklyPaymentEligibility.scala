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

package models

import models.RelationshipStatus._
import pages.partner.RelationshipStatusPage
import pages.payments.ApplicantOrPartnerBenefitsPage
import play.api.libs.json.Reads
import play.api.libs.functional.syntax._

final case class WeeklyPaymentEligibility(relationshipStatus: RelationshipStatus, benefits: Option[Set[Benefits]]) {

  private lazy val qualifyingStatuses = Seq(Single, Separated, Divorced, Widowed)

  lazy val eligible: Boolean =
    qualifyingStatuses.contains(relationshipStatus) ||
      benefits.exists(Benefits.qualifyingBenefits.intersect(_).nonEmpty)
}

object WeeklyPaymentEligibility {

  implicit lazy val reads: Reads[WeeklyPaymentEligibility] = {
    (
      RelationshipStatusPage.path.read[RelationshipStatus] ~
        ApplicantOrPartnerBenefitsPage.path.readNullable[Set[Benefits]]
      )(WeeklyPaymentEligibility.apply _)
  }
}
