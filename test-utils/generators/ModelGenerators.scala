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

package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

trait ModelGenerators {

  def ukPostcode: Gen[String] =
    for {
      firstChars <- Gen.choose(1, 2)
      first <- Gen.listOfN(firstChars, Gen.alphaUpperChar).map(_.mkString)
      second <- Gen.numChar.map(_.toString)
      third <- Gen.oneOf(Gen.alphaUpperChar, Gen.numChar).map(_.toString)
      fourth <- Gen.numChar.map(_.toString)
      fifth <- Gen.listOfN(2, Gen.alphaUpperChar).map(_.mkString)
    } yield s"$first$second$third$fourth$fifth"

  def genUkCtaNationality: Gen[Nationality] =
    Gen.oneOf(Nationality.allNationalities.filter(_.group == NationalityGroup.UkCta))

  def genEeaNationality: Gen[Nationality] =
    Gen.oneOf(Nationality.allNationalities.filter(_.group == NationalityGroup.Eea))

  def genNonEeaNationality: Gen[Nationality] =
    Gen.oneOf(Nationality.allNationalities.filter(_.group == NationalityGroup.NonEea))

  implicit lazy val arbitraryNationality: Arbitrary[Nationality] =
    Arbitrary{
      Gen.oneOf(Nationality.allNationalities)
    }

  implicit lazy val arbitraryEmploymentStatus: Arbitrary[EmploymentStatus] =
    Arbitrary {
      Gen.oneOf(EmploymentStatus.values)
    }

  implicit lazy val arbitraryScottishBirthCertificateDetails: Arbitrary[ScottishBirthCertificateDetails] =
    Arbitrary {
      for {
        district <- Gen.choose(100, 999)
        year <- Gen.choose(LocalDate.now.getYear - 20, LocalDate.now.getYear)
        entry <- Gen.choose(1, 999)
      } yield ScottishBirthCertificateDetails(district, year, entry)
    }

  implicit lazy val arbitraryCountry: Arbitrary[Country] =
    Arbitrary {
      Gen.oneOf(Country.internationalCountries)
    }

  implicit lazy val arbitraryPartnerClaimingChildBenefit: Arbitrary[PartnerClaimingChildBenefit] =
    Arbitrary {
      Gen.oneOf(PartnerClaimingChildBenefit.values)
    }

  implicit lazy val arbitraryBankAccountHolder: Arbitrary[BankAccountHolder] =
    Arbitrary {
      Gen.oneOf(BankAccountHolder.values)
    }

  implicit lazy val arbitraryIncome: Arbitrary[Income] =
    Arbitrary {
      Gen.oneOf(Income.values)
    }

  implicit lazy val arbitraryPaymentFrequency: Arbitrary[PaymentFrequency] =
    Arbitrary {
      Gen.oneOf(PaymentFrequency.values)
    }

  implicit lazy val arbitraryChildName: Arbitrary[ChildName] =
    Arbitrary {
      for {
        firstName <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        middleNames <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        lastName <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
      } yield ChildName(firstName, middleNames, lastName)
    }

  implicit lazy val arbitraryChildBirthRegistrationCountry: Arbitrary[ChildBirthRegistrationCountry] =
    Arbitrary {
      Gen.oneOf(ChildBirthRegistrationCountry.values)
    }

  implicit lazy val arbitraryChildBiologicalSex: Arbitrary[ChildBiologicalSex] =
    Arbitrary {
      Gen.oneOf(ChildBiologicalSex.values)
    }

  implicit lazy val arbitraryApplicantRelationshipToChild: Arbitrary[ApplicantRelationshipToChild] =
    Arbitrary {
      Gen.oneOf(ApplicantRelationshipToChild.values)
    }

  implicit lazy val arbitraryNino: Arbitrary[Nino] = Arbitrary {
    for {
      firstChar <- Gen.oneOf('A', 'C', 'E', 'H', 'J', 'L', 'M', 'O', 'P', 'R', 'S', 'W', 'X', 'Y').map(_.toString)
      secondChar <- Gen.oneOf('A', 'B', 'C', 'E', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z').map(_.toString)
      digits <- Gen.listOfN(6, Gen.numChar)
      lastChar <- Gen.oneOf('A', 'B', 'C', 'D')
    } yield Nino(firstChar ++ secondChar ++ (digits :+ lastChar).mkString)
  }

  implicit lazy val arbitraryAdultName: Arbitrary[AdultName] =
    Arbitrary {
      for {
        title <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        firstName <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        middleNames <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        lastName <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
      } yield AdultName(title, firstName, middleNames, lastName)
    }

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] =
    Arbitrary {
      for {
        line1    <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        line2    <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        town     <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        county   <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        postcode <- ukPostcode
      } yield UkAddress(line1, line2, town, county, postcode)
    }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] =
    Arbitrary {
      for {
        line1    <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        line2    <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        town     <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        state    <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        postcode <- Gen.option(Gen.listOfN(8, Gen.alphaChar).map(_.mkString))
        country  <- Gen.oneOf(Country.internationalCountries)
      } yield InternationalAddress(line1, line2, town, state, postcode, country)
    }

  implicit lazy val arbitraruNPSAddress: Arbitrary[NPSAddress] =
    Arbitrary {
      for {
        line1 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        line2 <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        line3 <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        line4 <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        line5 <- Gen.option(Gen.listOfN(35, Gen.alphaChar).map(_.mkString))
        postcode <- Gen.option(Gen.listOfN(8, Gen.alphaChar).map(_.mkString))
        country <- Gen.option(Gen.oneOf(Country.internationalCountries))
      } yield NPSAddress(line1, line2, line3, line4, line5, postcode, country)
    }

  implicit lazy val arbitraryBankAccountDetails: Arbitrary[BankAccountDetails] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
        accountNumber <- arbitrary[String]
        sortCode <- arbitrary[String]
      } yield BankAccountDetails(firstName, lastName, accountNumber, sortCode)
    }

  implicit lazy val arbitraryBuildingSocietyDetails: Arbitrary[BuildingSocietyDetails] =
    Arbitrary {
      for {
        firstName <- arbitrary[String]
        lastName <- arbitrary[String]
        buildingSociety <- Gen.oneOf(BuildingSociety.allBuildingSocieties)
        rollNumber <- arbitrary[String]
      } yield BuildingSocietyDetails(firstName, lastName, buildingSociety, rollNumber)
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
