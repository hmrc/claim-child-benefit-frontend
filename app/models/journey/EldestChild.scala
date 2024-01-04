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

package models.journey

import cats.data._
import cats.implicits._
import models.PartnerClaimingChildBenefit._
import models.{ChildName, UserAnswers}
import pages.applicant.{EldestChildDateOfBirthPage, EldestChildNamePage}
import pages.partner.{PartnerClaimingChildBenefitPage, PartnerEldestChildDateOfBirthPage, PartnerEldestChildNamePage}
import queries.Query

import java.time.LocalDate

final case class EldestChild(name: ChildName, dateOfBirth: LocalDate)

object EldestChild {

  def buildPartnerEldestChild(answers: UserAnswers): IorNec[Query, Option[EldestChild]] =
    answers.getIor(PartnerClaimingChildBenefitPage).flatMap {
      case GettingPayments | NotGettingPayments | WaitingToHear =>
        (
          answers.getIor(PartnerEldestChildNamePage),
          answers.getIor(PartnerEldestChildDateOfBirthPage)
        ).parMapN { (name, dateOfBirth) => Some(EldestChild(name, dateOfBirth)) }

      case NotClaiming =>
        Ior.Right(None)
    }

  def buildApplicantEldestChild(answers: UserAnswers): IorNec[Query, EldestChild] =
    (
      answers.getIor(EldestChildNamePage),
      answers.getIor(EldestChildDateOfBirthPage)
    ).parMapN(EldestChild.apply)
}
