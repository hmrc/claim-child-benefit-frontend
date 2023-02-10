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

package models

import cats.data.NonEmptyList
import models.ApplicantRelationshipToChild.AdoptedChild
import models.ChildBirthRegistrationCountry._
import models.DocumentType.{AdoptionCertificate, BirthCertificate, TravelDocument}
import models.JourneyModel._

import java.time.LocalDate

final case class JourneyModel(
                               applicant: Applicant,
                               relationship: Relationship,
                               children: NonEmptyList[Child],
                               benefits: Option[Set[Benefits]],
                               paymentPreference: PaymentPreference,
                               additionalInformation: AdditionalInformation
                             ) {

  val allRequiredDocuments: List[RequiredDocument] =
    children.toList.flatMap { child =>
      child.requiredDocuments.map(doc => RequiredDocument(child.name, doc))
    }

  val anyDocumentsRequired: Boolean = allRequiredDocuments.nonEmpty
}

object JourneyModel {

  final case class Relationship(status: RelationshipStatus, since: Option[LocalDate], partner: Option[Partner])
  final case class EldestChild(name: ChildName, dateOfBirth: LocalDate)

  sealed trait AccountDetailsWithHolder
  final case class BankAccountWithHolder(holder: BankAccountHolder, details: BankAccountDetails) extends AccountDetailsWithHolder
  final case class BuildingSocietyWithHolder(holder: BankAccountHolder, details: BuildingSocietyDetails) extends AccountDetailsWithHolder

  sealed trait PaymentPreference

  object PaymentPreference {

    final case class Weekly(accountDetails: Option[AccountDetailsWithHolder], eldestChild: Option[EldestChild]) extends PaymentPreference
    final case class EveryFourWeeks(accountDetails: Option[AccountDetailsWithHolder], eldestChild: Option[EldestChild]) extends PaymentPreference
    final case class ExistingAccount(eldestChild: EldestChild, frequency: PaymentFrequency) extends PaymentPreference
    final case class DoNotPay(eldestChild: Option[EldestChild]) extends PaymentPreference
  }

  sealed trait Residency

  object Residency {
    case object AlwaysLivedInUk extends Residency
    final case class LivedInUkAndAbroad(usualCountryOfResidence: Option[Country], arrivalDate: Option[LocalDate], employmentStatus: Set[EmploymentStatus], countriesWorked: List[Country], countriesReceivedBenefits: List[Country]) extends Residency
    final case class AlwaysLivedAbroad(usualCountryOfResidence: Country, employmentStatus: Set[EmploymentStatus], countriesWorked: List[Country], countriesReceivedBenefits: List[Country]) extends Residency
  }

  final case class Applicant(
                              name: AdultName,
                              previousFamilyNames: List[ApplicantPreviousName],
                              dateOfBirth: LocalDate,
                              nationalInsuranceNumber: Option[String],
                              currentAddress: Address,
                              previousAddress: Option[Address],
                              telephoneNumber: String,
                              nationalities: NonEmptyList[Nationality],
                              residency: Residency,
                              memberOfHMForcesOrCivilServantAbroad: Option[Boolean],
                              currentlyReceivingChildBenefit: CurrentlyReceivingChildBenefit
                            )

  final case class Partner(
                            name: AdultName,
                            dateOfBirth: LocalDate,
                            nationalities: NonEmptyList[Nationality],
                            nationalInsuranceNumber: Option[String],
                            memberOfHMForcesOrCivilServantAbroad: Option[Boolean],
                            currentlyClaimingChildBenefit: PartnerClaimingChildBenefit,
                            eldestChild: Option[EldestChild],
                            countriesWorked: List[Country],
                            countriesReceivedBenefits: List[Country],
                            employmentStatus: Set[EmploymentStatus]
                          )

  final case class Child(
                          name: ChildName,
                          nameChangedByDeedPoll: Option[Boolean],
                          previousNames: List[ChildName],
                          biologicalSex: ChildBiologicalSex,
                          dateOfBirth: LocalDate,
                          countryOfRegistration: ChildBirthRegistrationCountry,
                          birthCertificateNumber: Option[BirthCertificateNumber],
                          birthCertificateDetailsMatched: BirthRegistrationMatchingResult,
                          relationshipToApplicant: ApplicantRelationshipToChild,
                          adoptingThroughLocalAuthority: Boolean,
                          previousClaimant: Option[PreviousClaimant],
                          guardian: Option[Guardian],
                          previousGuardian: Option[PreviousGuardian],
                          dateChildStartedLivingWithApplicant: Option[LocalDate]
                        ) {

    private val adoptionCertificate =
      if (relationshipToApplicant == AdoptedChild) Some(AdoptionCertificate) else None

    private val (birthCertificate, travelDocument) = countryOfRegistration match {
      case England | Scotland | Wales      => (None, None)
      case _ if previousClaimant.isDefined => (None, None)
      case NorthernIreland                 => (Some(BirthCertificate), None)
      case _                               => (Some(BirthCertificate), Some(TravelDocument))
    }

    val requiredDocuments: Seq[DocumentType] =
      Seq(birthCertificate, travelDocument, adoptionCertificate).flatten
  }

  final case class PreviousClaimant(name: Option[AdultName], address: Option[Address])

  final case class Guardian(name: Option[AdultName], address: Option[Address])

  final case class PreviousGuardian(name: Option[AdultName], address: Option[Address], phoneNumber: Option[String])
}
