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

import models.domain.Nationality.UkCta
import models.journey
import models.journey.Residency.{AlwaysLivedAbroad, AlwaysLivedInUk, LivedInUkAndAbroad}
import play.api.libs.json.{Json, OWrites}

import java.time.LocalDate

sealed trait Claimant

object Claimant {

  implicit lazy val writes: OWrites[Claimant] = OWrites {
    case x: UkCtaClaimantAlwaysResident => Json.toJsObject(x)(UkCtaClaimantAlwaysResident.writes)
    case x: UkCtaClaimantNotAlwaysResident => Json.toJsObject(x)(UkCtaClaimantNotAlwaysResident.writes)
    case x: NonUkCtaClaimantAlwaysResident => Json.toJsObject(x)(NonUkCtaClaimantAlwaysResident.writes)
    case x: NonUkCtaClaimantNotAlwaysResident => Json.toJsObject(x)(NonUkCtaClaimantNotAlwaysResident.writes)
  }

  def build(nino: String, journeyModel: journey.JourneyModel, hasSettledStatus: Option[Boolean]): Claimant = {
    val nationalityToUse: Nationality =
      Nationality.fromNationalityGroup(journeyModel.applicant.nationalityGroupToUse)

    val hicbcOptOut = journeyModel.paymentPreference match {
      case _: journey.PaymentPreference.DoNotPay => true
      case _ => false
    }

    (nationalityToUse, journeyModel.applicant.residency) match {
      case (UkCta, AlwaysLivedInUk) =>
        UkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = journeyModel.applicant.memberOfHMForcesOrCivilServantAbroad,
          hicbcOptOut = hicbcOptOut
        )

      case (UkCta, _: AlwaysLivedAbroad) =>
        UkCtaClaimantNotAlwaysResident(
          nino = nino,
          hmfAbroad = journeyModel.applicant.memberOfHMForcesOrCivilServantAbroad,
          hicbcOptOut = hicbcOptOut,
          last3MonthsInUK = false
        )

      case (UkCta, residency: LivedInUkAndAbroad) =>
        val last3MonthsInUK = residency.arrivalDate.exists(_.isBefore(LocalDate.now.minusMonths(3)))

        UkCtaClaimantNotAlwaysResident(
          nino = nino,
          hmfAbroad = journeyModel.applicant.memberOfHMForcesOrCivilServantAbroad,
          last3MonthsInUK = last3MonthsInUK,
          hicbcOptOut = hicbcOptOut
        )

      case (nationality, AlwaysLivedInUk) =>

        NonUkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = journeyModel.applicant.memberOfHMForcesOrCivilServantAbroad,
          hicbcOptOut = hicbcOptOut,
          nationality = nationality,
          rightToReside = hasSettledStatus.getOrElse(false)
        )

      case (nationality, _: AlwaysLivedAbroad) =>
        NonUkCtaClaimantNotAlwaysResident(
          nino = nino,
          hmfAbroad = journeyModel.applicant.memberOfHMForcesOrCivilServantAbroad,
          hicbcOptOut = hicbcOptOut,
          nationality = nationality,
          rightToReside = hasSettledStatus.getOrElse(false),
          last3MonthsInUK = false
        )

      case (nationality, residency: LivedInUkAndAbroad) =>
        val last3MonthsInUK = residency.arrivalDate.exists(_.isBefore(LocalDate.now.minusMonths(3)))

        NonUkCtaClaimantNotAlwaysResident(
          nino = nino,
          hmfAbroad = journeyModel.applicant.memberOfHMForcesOrCivilServantAbroad,
          hicbcOptOut = hicbcOptOut,
          nationality = nationality,
          rightToReside = hasSettledStatus.getOrElse(false),
          last3MonthsInUK = last3MonthsInUK
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
