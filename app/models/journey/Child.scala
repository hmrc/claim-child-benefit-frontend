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

import models.ApplicantRelationshipToChild.AdoptedChild
import models.ChildBirthRegistrationCountry._
import models.DocumentType.{AdoptionCertificate, BirthCertificate, TravelDocument}
import models.{ApplicantRelationshipToChild, BirthCertificateNumber, BirthRegistrationMatchingResult, ChildBiologicalSex, ChildBirthRegistrationCountry, ChildName, DocumentType, InternationalAddress}

import java.time.LocalDate

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
    case England | Scotland | Wales => (None, None)
    case _ if previousClaimant.isDefined => (None, None)
    case NorthernIreland => (Some(BirthCertificate), None)
    case _ => (Some(BirthCertificate), Some(TravelDocument))
  }

  val requiredDocuments: Seq[DocumentType] =
    Seq(birthCertificate, travelDocument, adoptionCertificate).flatten

  val possiblyLivedAbroadSeparately: Boolean =
    if (dateChildStartedLivingWithApplicant.exists(_.isAfter(LocalDate.now.minusMonths(3)))) {
      previousGuardian.exists { previousGuardian =>
        previousGuardian.address.forall {
          case _: InternationalAddress => true
          case _ => false
        }
      }
    } else {
      false
    }

  val possiblyRecentlyCaredForByLocalAuthority: Boolean =
    if (dateChildStartedLivingWithApplicant.exists(_.isAfter(LocalDate.now.minusMonths(3)))) {
      previousGuardian.exists(_.address.exists(_.possibleLocalAuthorityAddress))
    } else {
      false
    }
}
