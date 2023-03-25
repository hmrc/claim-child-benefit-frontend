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

package models.journey

sealed trait PaymentPreference {
  val accountDetails: Option[AccountDetailsWithHolder]
}

object PaymentPreference {

  final case class Weekly(accountDetails: Option[AccountDetailsWithHolder], eldestChild: Option[EldestChild]) extends PaymentPreference

  final case class EveryFourWeeks(accountDetails: Option[AccountDetailsWithHolder], eldestChild: Option[EldestChild]) extends PaymentPreference

  final case class ExistingAccount(eldestChild: EldestChild) extends PaymentPreference {
    override val accountDetails: Option[AccountDetailsWithHolder] = None
  }

  final case class DoNotPay(eldestChild: Option[EldestChild]) extends PaymentPreference {
    override val accountDetails: Option[AccountDetailsWithHolder] = None
  }
}
