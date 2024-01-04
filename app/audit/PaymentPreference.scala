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

package audit

import models.PaymentFrequency
import models.journey
import play.api.libs.json.{JsString, Json, Writes}

sealed trait PaymentPreference

final case class Weekly(bankAccount: Option[AccountDetails], eldestChild: Option[EldestChild]) extends PaymentPreference

object Weekly {
  implicit lazy val writes: Writes[Weekly] = Writes {
    x =>

      val accountJsonValue = x.bankAccount.map(Json.toJson(_)).getOrElse(JsString("no suitable account"))
      val accountJson = Json.obj("account" -> accountJsonValue)

      val eldestChildJson =
        x.eldestChild
          .map(child => Json.obj("eldestChild" -> Json.toJson(child)))
          .getOrElse(Json.obj())

      Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.Weekly.toString
      ) ++ accountJson ++ eldestChildJson
  }
}

final case class EveryFourWeeks(bankAccount: Option[AccountDetails], eldestChild: Option[EldestChild]) extends PaymentPreference

object EveryFourWeeks {
  implicit lazy val writes: Writes[EveryFourWeeks] = Writes {
    x =>

      val accountJsonValue = x.bankAccount.map(Json.toJson(_)).getOrElse(JsString("no suitable account"))
      val accountJson = Json.obj("account" -> accountJsonValue)

      val eldestChildJson =
        x.eldestChild
          .map(child => Json.obj("eldestChild" -> Json.toJson(child)))
          .getOrElse(Json.obj())

      Json.obj(
        "wantsToBePaid" -> true,
        "frequency" -> PaymentFrequency.EveryFourWeeks.toString
      ) ++ accountJson ++ eldestChildJson
  }
}

final case class ExistingAccount(eldestChild: Option[EldestChild]) extends PaymentPreference

object ExistingAccount {
  implicit lazy val writes: Writes[ExistingAccount] = Writes {
    x =>
      val eldestChildJson =
        x.eldestChild
          .map(child => Json.obj("eldestChild" -> Json.toJson(child)))
          .getOrElse(Json.obj())

      Json.obj(
        "wantsToBePaid" -> true,
        "account" -> "use existing account"
      ) ++ eldestChildJson
  }
}

final case class DoNotPay(eldestChild: Option[EldestChild]) extends PaymentPreference

object DoNotPay {
  implicit lazy val writes: Writes[DoNotPay] = Writes {
    x =>

      val eldestChildJson = x.eldestChild
        .map(child => Json.obj("eldestChild" -> Json.toJson(child)))
        .getOrElse(Json.obj())

      Json.obj("wantsToBePaid" -> false) ++ eldestChildJson
  }
}

object PaymentPreference {

  implicit val writes: Writes[PaymentPreference] = Writes {
    case weekly: Weekly => Json.toJson(weekly)(Weekly.writes)
    case everyFourWeeks: EveryFourWeeks => Json.toJson(everyFourWeeks)(EveryFourWeeks.writes)
    case existingAccount: ExistingAccount => Json.toJson(existingAccount)(ExistingAccount.writes)
    case doNotPay: DoNotPay => Json.toJson(doNotPay)(DoNotPay.writes)
  }

  def build(paymentPreference: journey.PaymentPreference): PaymentPreference =
    paymentPreference match {
      case journey.PaymentPreference.Weekly(bankAccount, eldestChild) =>
        Weekly(
          bankAccount.map(AccountDetails.build),
          eldestChild.map(EldestChild.build)
        )

      case journey.PaymentPreference.EveryFourWeeks(bankAccount, eldestChild) =>
        EveryFourWeeks(
          bankAccount.map(AccountDetails.build),
          eldestChild.map(EldestChild.build)
        )

      case journey.PaymentPreference.ExistingAccount(eldestChild) =>
        ExistingAccount(eldestChild.map(EldestChild.build))

      case journey.PaymentPreference.DoNotPay(eldestChild) =>
        DoNotPay(eldestChild.map(EldestChild.build))
    }
}
