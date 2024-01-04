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

final case class PreviousGuardian(name: Option[AdultName], address: Option[Address], phoneNumber: Option[String])

object PreviousGuardian {

  def build(answers: UserAnswers, index: Index): IorNec[Query, Option[PreviousGuardian]] = {

    answers.getIor(ChildLivesWithApplicantPage(index)).flatMap {
      case true =>
        answers.getIor(ChildLivedWithAnyoneElsePage(index)).flatMap {
          case true =>

            val name = answers.getIor(PreviousGuardianNameKnownPage(index)).flatMap {
              case true => answers.getIor(PreviousGuardianNamePage(index)).map(Some(_))
              case false => Ior.Right(None)
            }
            val address = answers.getIor(PreviousGuardianNameKnownPage(index)).flatMap {
              case true => getPreviousGuardianAddress(answers, index)
              case false => Ior.Right(None)
            }
            val phoneNumber = answers.getIor(PreviousGuardianNameKnownPage(index)).flatMap {
              case true => getPreviousGuardianPhoneNumber(answers, index)
              case false => Ior.Right(None)
            }

            (
              name,
              address,
              phoneNumber
            ).parMapN(PreviousGuardian.apply).map(Some(_))

          case false =>
            Ior.Right(None)
        }

      case false =>
        Ior.Right(None)
    }
  }

  private def getPreviousGuardianAddress(answers: UserAnswers, index: Index): IorNec[Query, Option[Address]] = {
    answers.getIor(PreviousGuardianAddressKnownPage(index)).flatMap {
      case true =>
        answers.getIor(PreviousGuardianAddressInUkPage(index)).flatMap {
          case true => answers.getIor(PreviousGuardianUkAddressPage(index)).map(Some(_))
          case false => answers.getIor(PreviousGuardianInternationalAddressPage(index)).map(Some(_))
        }

      case false =>
        Ior.Right(None)
    }
  }

  private def getPreviousGuardianPhoneNumber(answers: UserAnswers, index: Index): IorNec[Query, Option[String]] = {
    answers.getIor(PreviousGuardianPhoneNumberKnownPage(index)).flatMap {
      case true =>
        answers.getIor(PreviousGuardianPhoneNumberPage(index)).map(Some(_))

      case false =>
        Ior.Right(None)
    }
  }
}