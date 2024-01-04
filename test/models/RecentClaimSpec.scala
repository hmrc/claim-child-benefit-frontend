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

import generators.ModelGenerators
import models.Income._
import models.RelationshipStatus._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.partner.RelationshipStatusPage
import pages.payments.{ApplicantIncomePage, PartnerIncomePage, WantToBePaidPage}
import play.api.libs.json._
import uk.gov.hmrc.domain.Nino

import java.time.Instant

class RecentClaimSpec extends AnyFreeSpec with Matchers with OptionValues with ModelGenerators with TryValues {

  private val now = Instant.now

  ".reads" - {

    "must read when a Tax Charge Choice is not present" in {

      val json = Json.obj(
        "nino" -> "nino",
        "created" -> Json.toJson(now)
      )

      json.validate[RecentClaim] mustEqual JsSuccess(RecentClaim("nino", now, TaxChargeChoice.NotRecorded))
    }

    "must read when a Tax Charge Choice is present" in {

      val json = Json.obj(
        "nino" -> "nino",
        "created" -> Json.toJson(now),
        "taxChargeChoice" -> "optedOut"
      )

      json.validate[RecentClaim] mustEqual JsSuccess(RecentClaim("nino", now, TaxChargeChoice.OptedOut))
    }
  }

  ".apply" - {

    val nino = arbitrary[Nino].sample.value.nino
    val now  = Instant.now

    "must have a tax charge choice of Does Not Apply" - {

      "when the user does not have a partner and their income is below the lower threshold" in {

        val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

        val answers =
          UserAnswers("id", nino = Some(nino))
            .set(RelationshipStatusPage, relationship).success.value
            .set(ApplicantIncomePage, BelowLowerThreshold).success.value
            .set(WantToBePaidPage, true).success.value

        val recentClaim = RecentClaim.build(nino, answers, now).value
        recentClaim.taxChargeChoice mustEqual TaxChargeChoice.DoesNotApply
      }

      "when the user has a partner and both of their incomes are below the lower threshold" in {

        val relationship = Gen.oneOf(Married, Cohabiting).sample.value

        val answers =
          UserAnswers("id", nino = Some(nino))
            .set(RelationshipStatusPage, relationship).success.value
            .set(ApplicantIncomePage, BelowLowerThreshold).success.value
            .set(PartnerIncomePage, BelowLowerThreshold).success.value
            .set(WantToBePaidPage, true).success.value

        val recentClaim = RecentClaim.build(nino, answers, now).value
        recentClaim.taxChargeChoice mustEqual TaxChargeChoice.DoesNotApply
      }
    }
    
    "must have a tax charge of Opted Out" - {
      
      val income = Gen.oneOf(BetweenThresholds, AboveUpperThreshold).sample.value
      
      "when the user does not have a partner and chose not to be paid" in {

        val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value

        val answers =
          UserAnswers("id", nino = Some(nino))
            .set(RelationshipStatusPage, relationship).success.value
            .set(ApplicantIncomePage, income).success.value
            .set(WantToBePaidPage, false).success.value

        val recentClaim = RecentClaim.build(nino, answers, now).value
        recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedOut
      }
      
      "when the user has a partner and chose not to be paid" in {

        val relationship = Gen.oneOf(Married, Cohabiting).sample.value

        val answers =
          UserAnswers("id", nino = Some(nino))
            .set(RelationshipStatusPage, relationship).success.value
            .set(ApplicantIncomePage, income).success.value
            .set(PartnerIncomePage, income).success.value
            .set(WantToBePaidPage, false).success.value

        val recentClaim = RecentClaim.build(nino, answers, now).value
        recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedOut
      }
    }
    
    "must have a tax charge choice of Opted In (Applicant)" - {

      "when the applicant does not have a partner, earns above the lower threshold and chose to be paid" in {
        
        val relationship = Gen.oneOf(Single, Separated, Divorced, Widowed).sample.value
        val income = Gen.oneOf(BetweenThresholds, AboveUpperThreshold).sample.value
        
        val answers =
          UserAnswers("id", nino = Some(nino))
            .set(RelationshipStatusPage, relationship).success.value
            .set(ApplicantIncomePage, income).success.value
            .set(WantToBePaidPage, true).success.value

        val recentClaim = RecentClaim.build(nino, answers, now).value
        recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedIn(TaxChargePayer.Applicant)
      }

      "when the applicant has a partner and chose to be paid" - {
        
        "and they earn above the lower threshold and their partner earns below the lower threshold" in {
          
          val relationship = Gen.oneOf(Married, Cohabiting).sample.value
          val applicantIncome = Gen.oneOf(BetweenThresholds, AboveUpperThreshold).sample.value
          
          val answers =
            UserAnswers("id", nino = Some(nino))
              .set(RelationshipStatusPage, relationship).success.value
              .set(ApplicantIncomePage, applicantIncome).success.value
              .set(PartnerIncomePage, BelowLowerThreshold).success.value
              .set(WantToBePaidPage, true).success.value

          val recentClaim = RecentClaim.build(nino, answers, now).value
          recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedIn(TaxChargePayer.Applicant)
        }
        
        "and they earn above the upper threshold and their partner earns between the thresholds" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            UserAnswers("id", nino = Some(nino))
              .set(RelationshipStatusPage, relationship).success.value
              .set(ApplicantIncomePage, AboveUpperThreshold).success.value
              .set(PartnerIncomePage, BetweenThresholds).success.value
              .set(WantToBePaidPage, true).success.value

          val recentClaim = RecentClaim.build(nino, answers, now).value
          recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedIn(TaxChargePayer.Applicant)
        }
      }
    }

    "must have a tax charge choice of Opted In (Partner)" - {

      "when the applicant has a partner and chose to be paid" - {

        "and they earn below the lower threshold and their partner earns above the lower threshold" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value
          val partnerIncome = Gen.oneOf(BetweenThresholds, AboveUpperThreshold).sample.value

          val answers =
            UserAnswers("id", nino = Some(nino))
              .set(RelationshipStatusPage, relationship).success.value
              .set(ApplicantIncomePage, BelowLowerThreshold).success.value
              .set(PartnerIncomePage, partnerIncome).success.value
              .set(WantToBePaidPage, true).success.value

          val recentClaim = RecentClaim.build(nino, answers, now).value
          recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedIn(TaxChargePayer.Partner)
        }

        "and they earn between the threshold and their partner earns above the upper thresholds" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            UserAnswers("id", nino = Some(nino))
              .set(RelationshipStatusPage, relationship).success.value
              .set(ApplicantIncomePage, BetweenThresholds).success.value
              .set(PartnerIncomePage, AboveUpperThreshold).success.value
              .set(WantToBePaidPage, true).success.value

          val recentClaim = RecentClaim.build(nino, answers, now).value
          recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedIn(TaxChargePayer.Partner)
        }
      }
    }

    "must have a tax charge choice of Opted In (Applicant Or Partner)" - {

      "when the applicant has a partner and chose to be paid" - {

        "and they both earn between the thresholds" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            UserAnswers("id", nino = Some(nino))
              .set(RelationshipStatusPage, relationship).success.value
              .set(ApplicantIncomePage, BetweenThresholds).success.value
              .set(PartnerIncomePage, BetweenThresholds).success.value
              .set(WantToBePaidPage, true).success.value

          val recentClaim = RecentClaim.build(nino, answers, now).value
          recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedIn(TaxChargePayer.ApplicantOrPartner)
        }

        "and they both earn above the upper thresholds" in {

          val relationship = Gen.oneOf(Married, Cohabiting).sample.value

          val answers =
            UserAnswers("id", nino = Some(nino))
              .set(RelationshipStatusPage, relationship).success.value
              .set(ApplicantIncomePage, AboveUpperThreshold).success.value
              .set(PartnerIncomePage, AboveUpperThreshold).success.value
              .set(WantToBePaidPage, true).success.value

          val recentClaim = RecentClaim.build(nino, answers, now).value
          recentClaim.taxChargeChoice mustEqual TaxChargeChoice.OptedIn(TaxChargePayer.ApplicantOrPartner)
        }
      }
    }
  }
}
