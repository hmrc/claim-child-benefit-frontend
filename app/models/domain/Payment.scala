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

import models.journey.JourneyModel
import models.journey
import play.api.libs.json.{Json, OWrites}

final case class Payment(
                          paymentFrequency: PaymentFrequency,
                          paymentDetails: Option[PaymentDetails]
                        )

object Payment {

  def build(paymentPreference: journey.PaymentPreference): Option[Payment] = paymentPreference match {
    case journey.PaymentPreference.DoNotPay(_) =>
      None

    case journey.PaymentPreference.ExistingAccount(_) =>
      None

    case x: journey.PaymentPreference.Weekly =>
      Some(Payment(PaymentFrequency.Weekly, PaymentDetails.build(x)))

    case x: journey.PaymentPreference.EveryFourWeeks =>
      Some(Payment(PaymentFrequency.EveryFourWeeks, PaymentDetails.build(x)))
  }

  implicit lazy val writes: OWrites[Payment] = Json.writes
}
