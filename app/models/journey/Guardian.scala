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
import models.{Address, AdultName, Index, UserAnswers}
import pages.child._
import queries.Query

final case class Guardian(name: Option[AdultName], address: Option[Address])

object Guardian {

  def build(answers: UserAnswers, index: Index): IorNec[Query, Option[Guardian]] =
    answers.getIor(ChildLivesWithApplicantPage(index)).flatMap {
      case true =>
        Ior.Right(None)

      case false =>
        val name = answers.getIor(GuardianNameKnownPage(index)).flatMap {
          case true => answers.getIor(GuardianNamePage(index)).map(Some(_))
          case false => Ior.Right(None)
        }
        val address = answers.getIor(GuardianNameKnownPage(index)).flatMap {
          case true => getGuardianAddress(answers, index)
          case false => Ior.Right(None)
        }
        (
          name,
          address
        ).parMapN(Guardian.apply).map(Some(_))
    }

  private def getGuardianAddress(answers: UserAnswers, index: Index): IorNec[Query, Option[Address]] = {
    answers.getIor(GuardianAddressKnownPage(index)).flatMap {
      case true =>
        answers.getIor(GuardianAddressInUkPage(index)).flatMap {
          case true => answers.getIor(GuardianUkAddressPage(index)).map(Some(_))
          case false => answers.getIor(GuardianInternationalAddressPage(index)).map(Some(_))
        }

      case false =>
        Ior.Right(None)
    }
  }
}
