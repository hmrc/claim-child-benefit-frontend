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

import cats.data._
import cats.implicits._
import models.CurrentlyReceivingChildBenefit.{GettingPayments, NotGettingPayments}
import models.{PaymentFrequency, UserAnswers}
import pages.applicant.CurrentlyReceivingChildBenefitPage
import pages.payments.{PaymentFrequencyPage, WantToBePaidPage}
import queries.Query

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

  def build(answers: UserAnswers): IorNec[Query, PaymentPreference] =
    answers.getIor(CurrentlyReceivingChildBenefitPage).flatMap {
      case GettingPayments =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            EldestChild
              .buildApplicantEldestChild(answers)
              .map(ExistingAccount)

          case false =>
            EldestChild
              .buildApplicantEldestChild(answers)
              .map(x => DoNotPay(Some(x)))
        }

      case NotGettingPayments =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            answers
              .get(PaymentFrequencyPage)
              .getOrElse(PaymentFrequency.EveryFourWeeks) match {
                case PaymentFrequency.Weekly =>
                  (
                    AccountDetailsWithHolder.build(answers),
                    EldestChild.buildApplicantEldestChild(answers)
                  ).parMapN((bank, child) => Weekly(bank, Some(child)))

                case PaymentFrequency.EveryFourWeeks =>
                  (
                    AccountDetailsWithHolder.build(answers),
                    EldestChild.buildApplicantEldestChild(answers)
                  ).parMapN((bank, child) => EveryFourWeeks(bank, Some(child)))
              }

          case false =>
            EldestChild
              .buildApplicantEldestChild(answers)
              .map(child => DoNotPay(Some(child)))
        }

      case _ =>
        answers.getIor(WantToBePaidPage).flatMap {
          case true =>
            answers.get(PaymentFrequencyPage).getOrElse(PaymentFrequency.EveryFourWeeks) match {
              case PaymentFrequency.Weekly =>
                AccountDetailsWithHolder
                  .build(answers)
                  .map(bank => Weekly(bank, None))

              case PaymentFrequency.EveryFourWeeks =>
                AccountDetailsWithHolder
                  .build(answers)
                  .map(bank => EveryFourWeeks(bank, None))
            }

          case false =>
            Ior.Right(DoNotPay(None))
        }
    }
}
