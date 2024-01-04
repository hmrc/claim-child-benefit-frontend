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

package models

import models.Income._
import models.IncomeOrdering._
import models.RelationshipStatus._
import models.TaxChargeChoice._
import pages.partner.RelationshipStatusPage
import pages.payments.{ApplicantIncomePage, PartnerIncomePage, WantToBePaidPage}
import play.api.libs.functional.syntax._
import play.api.libs.json._

import java.time.Instant

final case class RecentClaim(
                              nino: String,
                              created: Instant,
                              taxChargeChoice: TaxChargeChoice
                            )

object RecentClaim {

  implicit lazy val reads: Reads[RecentClaim] =
    (
      (__ \ "nino").read[String] and
      (__ \ "created").read[Instant] and
      (__ \ "taxChargeChoice").read[TaxChargeChoice].orElse(Reads.pure(TaxChargeChoice.NotRecorded))
    )(RecentClaim.apply _)

  implicit lazy val writes: OWrites[RecentClaim] = Json.writes

  //scalastyle:off
  def build(nino: String, answers: UserAnswers, created: Instant): Option[RecentClaim] =
    answers.get(WantToBePaidPage).flatMap { wantToBePaid =>
      answers.get(RelationshipStatusPage).flatMap {
        case Married | Cohabiting =>
          answers.get(ApplicantIncomePage).flatMap { applicantIncome =>
            answers.get(PartnerIncomePage).map { partnerIncome =>
              if (applicantIncome == BelowLowerThreshold && partnerIncome == BelowLowerThreshold) {
                RecentClaim(nino, created, DoesNotApply)
              } else {
                val taxChargePayer = {
                  if (applicantIncome < partnerIncome)       TaxChargePayer.Partner
                  else if (applicantIncome == partnerIncome) TaxChargePayer.ApplicantOrPartner
                  else                                       TaxChargePayer.Applicant
                }

                if (wantToBePaid) RecentClaim(nino, created, OptedIn(taxChargePayer))
                else              RecentClaim(nino, created, OptedOut)
              }
            }
          }

        case _ =>
          answers.get(ApplicantIncomePage).map {
            case BelowLowerThreshold =>
              RecentClaim(nino, created, DoesNotApply)

            case _ =>
              if (wantToBePaid) RecentClaim(nino, created, OptedIn(TaxChargePayer.Applicant))
              else              RecentClaim(nino, created, OptedOut)
          }
      }
    }
}
