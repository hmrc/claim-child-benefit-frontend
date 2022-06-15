/*
 * Copyright 2022 HM Revenue & Customs
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

package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

trait ModelGenerators {

  implicit lazy val arbitraryEldestChildName: Arbitrary[EldestChildName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        middleNames <- Gen.option(arbitrary[String])
        lastName <- arbitrary[String]
      } yield EldestChildName(firstName, middleNames, lastName)
    }

  implicit lazy val arbitraryBuildingSocietyAccountDetails: Arbitrary[BuildingSocietyAccountDetails] =
    Arbitrary {
      for {
        buildingSocietyName <- arbitrary[String]
        accountNumber <- arbitrary[String]
        sortCode <- arbitrary[String]
        rollNumber <- Gen.option(arbitrary[String])
      } yield BuildingSocietyAccountDetails(buildingSocietyName, accountNumber, sortCode, rollNumber)
    }

  implicit lazy val arbitraryBankAccountType: Arbitrary[BankAccountType] =
    Arbitrary {
      Gen.oneOf(BankAccountType.values)
    }

  implicit lazy val arbitraryBankAccountDetails: Arbitrary[BankAccountDetails] =
    Arbitrary {
      for {
        bankName <- arbitrary[String]
        accountNumber <- arbitrary[String]
        sortCode <- arbitrary[String]
      } yield BankAccountDetails(bankName, accountNumber, sortCode)
    }

  implicit lazy val arbitraryAccountHolderNames: Arbitrary[AccountHolderNames] =
    Arbitrary {
      for {
        name1 <- arbitrary[String]
        name2 <- arbitrary[String]
      } yield AccountHolderNames(name1, name2)
    }

  implicit lazy val arbitraryBenefits: Arbitrary[Benefits] =
    Arbitrary {
      Gen.oneOf(Benefits.values)
    }

  implicit lazy val arbitraryRelationshipStatus: Arbitrary[RelationshipStatus] =
    Arbitrary {
      Gen.oneOf(RelationshipStatus.values)
    }
}
