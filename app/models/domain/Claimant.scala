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
import models.domain.Nationality.UkCta
import play.api.libs.json.{Json, OWrites}

sealed trait Claimant

object Claimant {

  implicit lazy val writes: OWrites[Claimant] = OWrites {
    case x: UkCtaClaimantAlwaysResident => Json.toJsObject(x)(UkCtaClaimantAlwaysResident.writes)
    case x: UkCtaClaimantNotAlwaysResident => Json.toJsObject(x)(UkCtaClaimantNotAlwaysResident.writes)
    case x: NonUkCtaClaimantAlwaysResident => Json.toJsObject(x)(NonUkCtaClaimantAlwaysResident.writes)
    case x: NonUkCtaClaimantNotAlwaysResident => Json.toJsObject(x)(NonUkCtaClaimantNotAlwaysResident.writes)
  }

  def build(nino: String, journeyModel: JourneyModel): Claimant = {
    val ukCta = journeyModel.applicant.nationalities.exists(_.group == models.NationalityGroup.UkCta)
    val alwaysResident = journeyModel.applicant.residency == JourneyModel.Residency.AlwaysLivedInUk
    val hicbcOptOut = journeyModel.paymentPreference match {
      case _: JourneyModel.PaymentPreference.DoNotPay => true
      case _ => false
    }

    (ukCta, alwaysResident) match {
      case (true, true) =>
        UkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = journeyModel.applicant.memberOfHMForcesOrCivilServantAbroad,
          hicbcOptOut = hicbcOptOut
        )
    }
  }
}

final case class UkCtaClaimantAlwaysResident(
                                           nino: String,
                                           hmfAbroad: Boolean,
                                           hicbcOptOut: Boolean
                                         ) extends Claimant {

  val nationality: Nationality = UkCta
}

object UkCtaClaimantAlwaysResident {

  val writes: OWrites[UkCtaClaimantAlwaysResident] = OWrites {
    claimant =>
      Json.obj(
        "nino" -> claimant.nino,
        "hmfAbroad" -> claimant.hmfAbroad,
        "hicbcOptOut" -> claimant.hicbcOptOut,
        "nationality" -> claimant.nationality,
        "alwaysLivedInUK" -> true
      )
  }
}

final case class UkCtaClaimantNotAlwaysResident(
                                           nino: String,
                                           hmfAbroad: Boolean,
                                           last3MonthsInUK: Boolean,
                                           hicbcOptOut: Boolean
                                         ) extends Claimant {

  val nationality: Nationality = UkCta
}

object UkCtaClaimantNotAlwaysResident {

  val writes: OWrites[UkCtaClaimantNotAlwaysResident] = OWrites {
    claimant =>
      Json.obj(
        "nino" -> claimant.nino,
        "hmfAbroad" -> claimant.hmfAbroad,
        "hicbcOptOut" -> claimant.hicbcOptOut,
        "nationality" -> claimant.nationality,
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> claimant.last3MonthsInUK
      )
  }
}

final case class NonUkCtaClaimantAlwaysResident(
                                                 nino: String,
                                                 hmfAbroad: Boolean,
                                                 hicbcOptOut: Boolean,
                                                 nationality: Nationality,
                                                 rightToReside: Boolean
                                               ) extends Claimant

object NonUkCtaClaimantAlwaysResident {

  val writes: OWrites[NonUkCtaClaimantAlwaysResident] = OWrites {
    claimant =>
      Json.obj(
        "nino" -> claimant.nino,
        "hmfAbroad" -> claimant.hmfAbroad,
        "hicbcOptOut" -> claimant.hicbcOptOut,
        "nationality" -> claimant.nationality,
        "alwaysLivedInUK" -> true,
        "rightToReside" -> claimant.rightToReside
      )
  }
}

final case class NonUkCtaClaimantNotAlwaysResident(
                                                 nino: String,
                                                 hmfAbroad: Boolean,
                                                 hicbcOptOut: Boolean,
                                                 nationality: Nationality,
                                                 rightToReside: Boolean,
                                                 last3MonthsInUK: Boolean
                                               ) extends Claimant

object NonUkCtaClaimantNotAlwaysResident {

  val writes: OWrites[NonUkCtaClaimantNotAlwaysResident] = OWrites {
    claimant =>
      Json.obj(
        "nino" -> claimant.nino,
        "hmfAbroad" -> claimant.hmfAbroad,
        "hicbcOptOut" -> claimant.hicbcOptOut,
        "nationality" -> claimant.nationality,
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> claimant.last3MonthsInUK,
        "rightToReside" -> claimant.rightToReside
      )
  }
}
