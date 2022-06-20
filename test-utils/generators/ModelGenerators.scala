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
import uk.gov.hmrc.domain.Nino

trait ModelGenerators {

  implicit lazy val arbitraryPreviousClaimantName: Arbitrary[PreviousClaimantName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
      } yield PreviousClaimantName(firstName, lastName)
    }

  implicit lazy val arbitraryPreviousClaimantAddress: Arbitrary[PreviousClaimantAddress] =
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- arbitrary[String]
      } yield PreviousClaimantAddress(line1, line2)
    }

  implicit lazy val arbitraryIncludedDocuments: Arbitrary[IncludedDocuments] =
    Arbitrary {
      Gen.oneOf(IncludedDocuments.values)
    }

  implicit lazy val arbitraryChildScottishBirthCertificateDetails: Arbitrary[ChildScottishBirthCertificateDetails] =
    Arbitrary {
      for {
        district <- arbitrary[String]
        year <- arbitrary[String]
      } yield ChildScottishBirthCertificateDetails(district, year)
    }

  implicit lazy val arbitraryChildPreviousName: Arbitrary[ChildPreviousName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
      } yield ChildPreviousName(firstName, lastName)
    }

  implicit lazy val arbitraryChildName: Arbitrary[ChildName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
      } yield ChildName(firstName, lastName)
    }

  implicit lazy val arbitraryChildBirthRegistrationCountry: Arbitrary[ChildBirthRegistrationCountry] =
    Arbitrary {
      Gen.oneOf(ChildBirthRegistrationCountry.values.toSeq)
    }

  implicit lazy val arbitraryChildBiologicalSex: Arbitrary[ChildBiologicalSex] =
    Arbitrary {
      Gen.oneOf(ChildBiologicalSex.values.toSeq)
    }

  implicit lazy val arbitraryApplicantRelationshipToChild: Arbitrary[ApplicantRelationshipToChild] =
    Arbitrary {
      Gen.oneOf(ApplicantRelationshipToChild.values.toSeq)
    }

  implicit lazy val arbitraryAnyoneClaimedForChildBefore: Arbitrary[AnyoneClaimedForChildBefore] =
    Arbitrary {
      Gen.oneOf(AnyoneClaimedForChildBefore.values.toSeq)
    }

  implicit lazy val arbitraryPartnerName: Arbitrary[PartnerName] =
    Arbitrary {
      for {
        title <- Gen.option(arbitrary[String])
        firstName <- arbitrary[String]
        middleNames <- Gen.option(arbitrary[String])
        lastName <- arbitrary[String]
      } yield PartnerName(title, firstName, middleNames, lastName)
    }

  implicit lazy val arbitraryPartnerEmploymentStatus: Arbitrary[PartnerEmploymentStatus] =
    Arbitrary {
      Gen.oneOf(PartnerEmploymentStatus.values)
    }

  implicit lazy val arbitraryPartnerEldestChildName: Arbitrary[PartnerEldestChildName] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        middleNames <- Gen.option(arbitrary[String])
        lastName <- arbitrary[String]
      } yield PartnerEldestChildName(firstName, middleNames, lastName)
    }

  implicit lazy val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    for {
      firstChar <- Gen.oneOf('A', 'C', 'E', 'H', 'J', 'L', 'M', 'O', 'P', 'R', 'S', 'W', 'X', 'Y').map(_.toString)
      secondChar <- Gen.oneOf('A', 'B', 'C', 'E', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z').map(_.toString)
      digits <- Gen.listOfN(6, Gen.numChar)
      lastChar <- Gen.oneOf('A', 'B', 'C', 'D')
    } yield Nino(firstChar ++ secondChar ++ digits :+ lastChar)
  }

  implicit lazy val arbitraryApplicantPreviousAddress: Arbitrary[ApplicantPreviousAddress] =
    Arbitrary {
      for {
        line1    <- arbitrary[String]
        line2    <- Gen.option(arbitrary[String])
        line3    <- Gen.option(arbitrary[String])
        postcode <- arbitrary[String]
      } yield ApplicantPreviousAddress(line1, line2, line3, postcode)
    }

  implicit lazy val arbitraryApplicantName: Arbitrary[ApplicantName] =
    Arbitrary {
      for {
        title       <- Gen.option(arbitrary[String])
        firstName   <- arbitrary[String]
        middleNames <- Gen.option(arbitrary[String])
        lastName    <- arbitrary[String]
      } yield ApplicantName(title, firstName, middleNames, lastName)
    }

  implicit lazy val arbitraryApplicantEmploymentStatus: Arbitrary[ApplicantEmploymentStatus] =
    Arbitrary {
      Gen.oneOf(ApplicantEmploymentStatus.values)
    }

  implicit lazy val arbitraryApplicantCurrentAddress: Arbitrary[ApplicantCurrentAddress] =
    Arbitrary {
      for {
        line1    <- arbitrary[String]
        line2    <- Gen.option(arbitrary[String])
        line3    <- Gen.option(arbitrary[String])
        postcode <- arbitrary[String]
      } yield ApplicantCurrentAddress(line1, line2, line3, postcode)
    }

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
