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

import cats.data.NonEmptyList
import generators.ModelGenerators
import models.RelationshipStatus._
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{EitherValues, OptionValues, TryValues}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class JourneyModelSpec
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with EitherValues
    with OptionValues
    with ModelGenerators {

  private val adultName = AdultName(None, "first", None, "last")
  private val ukAddress = UkAddress("line 1", None, "town", None, "AA11 1AA")
  private val phoneNumber = "07777 777777"
  private val nationality = Gen.oneOf(Nationality.allNationalities).sample.value

  private val childName = ChildName("first", None, "last")
  private val relationshipToChild = ApplicantRelationshipToChild.BirthChild
  private val systemNumber = BirthCertificateSystemNumber("000000000")

  private val nino = arbitrary[Nino].sample.value

  ".allRequiredDocuments" - {

    "must be a list of all documents required for all the children in the claim" in {

      val journeyModel = JourneyModel(
        Applicant(
          name = adultName,
          previousFamilyNames = Nil,
          dateOfBirth = LocalDate.now,
          nationalInsuranceNumber = Some(nino.nino),
          currentAddress = ukAddress,
          previousAddress = None,
          telephoneNumber = phoneNumber,
          nationalities = NonEmptyList(nationality, Nil),
          residency = Residency.AlwaysLivedInUk,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
          changedDesignatoryDetails = None,
          correspondenceAddress = None
        ),
        Relationship(Single, None, None),
        NonEmptyList(Child(
          name = childName,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = ChildBiologicalSex.Female,
          dateOfBirth = LocalDate.now,
          countryOfRegistration = ChildBirthRegistrationCountry.England,
          birthCertificateNumber = Some(systemNumber),
          birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
          relationshipToApplicant = relationshipToChild,
          adoptingThroughLocalAuthority = false,
          previousClaimant = None,
          guardian = None,
          previousGuardian = None,
          dateChildStartedLivingWithApplicant = None
        ), List(
          Child(
            name = ChildName("child 2 first", None, "child 2 last"),
            nameChangedByDeedPoll = None,
            previousNames = Nil,
            biologicalSex = ChildBiologicalSex.Female,
            dateOfBirth = LocalDate.now,
            countryOfRegistration = ChildBirthRegistrationCountry.OtherCountry,
            birthCertificateNumber = None,
            birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
            relationshipToApplicant = relationshipToChild,
            adoptingThroughLocalAuthority = false,
            previousClaimant = None,
            guardian = None,
            previousGuardian = None,
            dateChildStartedLivingWithApplicant = None
          ),
          Child(
            name = ChildName("child 3 first", None, "child 3 last"),
            nameChangedByDeedPoll = None,
            previousNames = Nil,
            biologicalSex = ChildBiologicalSex.Female,
            dateOfBirth = LocalDate.now,
            countryOfRegistration = ChildBirthRegistrationCountry.OtherCountry,
            birthCertificateNumber = None,
            birthCertificateDetailsMatched = BirthRegistrationMatchingResult.NotAttempted,
            relationshipToApplicant = ApplicantRelationshipToChild.AdoptedChild,
            adoptingThroughLocalAuthority = false,
            previousClaimant = None,
            guardian = None,
            previousGuardian = None,
            dateChildStartedLivingWithApplicant = None
          )
        )),
        None,
        PaymentPreference.DoNotPay(None),
        true,
        Nil,
        Nil
      )

      journeyModel.allRequiredDocuments must contain theSameElementsInOrderAs List(
        RequiredDocument(ChildName("child 2 first", None, "child 2 last"), DocumentType.BirthCertificate),
        RequiredDocument(ChildName("child 2 first", None, "child 2 last"), DocumentType.TravelDocument),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.BirthCertificate),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.TravelDocument),
        RequiredDocument(ChildName("child 3 first", None, "child 3 last"), DocumentType.AdoptionCertificate),
      )
    }
  }
}
