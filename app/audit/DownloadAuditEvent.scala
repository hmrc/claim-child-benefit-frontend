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

package audit

import audit.DownloadAuditEvent._
import models.JourneyModel
import play.api.libs.json.{Format, JsString, Json, Writes}

import java.time.LocalDate

final case class DownloadAuditEvent(
                                     applicant: Applicant,
                                     relationship: Relationship,
                                     children: List[Child],
                                     benefits: Set[String],
                                     paymentPreference: PaymentPreference
                                   )

object DownloadAuditEvent {

  def from(model: JourneyModel): DownloadAuditEvent =
    DownloadAuditEvent(
      applicant = Applicant(
        name                    = convertAdultName(model.applicant.name),
        previousFamilyNames     = model.applicant.previousFamilyNames,
        dateOfBirth             = model.applicant.dateOfBirth,
        nationalInsuranceNumber = model.applicant.nationalInsuranceNumber,
        currentAddress          = convertAddress(model.applicant.currentAddress),
        previousAddress         = model.applicant.previousAddress.map(convertAddress),
        telephoneNumber         = model.applicant.telephoneNumber,
        bestTimeToContact       = model.applicant.bestTimeToContact.map(_.toString),
        nationality             = model.applicant.nationality,
        employmentStatus        = model.applicant.employmentStatus.map(_.toString)
      ),
      relationship = Relationship(
        status  = model.relationship.status.toString,
        since   = model.relationship.since,
        partner = model.relationship.partner.map {
          partner => Partner(
            name                            = convertAdultName(partner.name),
            dateOfBirth                     = partner.dateOfBirth,
            nationality                     = partner.nationality,
            nationalInsuranceNumber         = partner.nationalInsuranceNumber,
            currentlyEntitledToChildBenefit = partner.currentlyEntitledToChildBenefit,
            waitingToHearAboutEntitlement   = partner.waitingToHearAboutEntitlement,
            eldestChild                     = partner.eldestChild.map(convertEldestChild)
          )
        }
      ),
      children = model.children.toList.map { child =>
        Child(
          name                          = convertChildName(child.name),
          nameChangedByDeedPoll         = child.nameChangedByDeedPoll,
          previousNames                 = child.previousNames.map(convertChildName),
          biologicalSex                 = child.biologicalSex.toString,
          birthRegistrationCountry      = child.countryOfRegistration.toString,
          birthCertificateNumber        = child.birtCertificateNumber,
          relationshipToApplicant       = child.relationshipToApplicant.toString,
          adoptingThroughLocalAuthority = child.adoptingThroughLocalAuthority,
          previousClaimant              = child.previousClaimant.map { claimant =>
            PreviousClaimant(
              name    = convertAdultName(claimant.name),
              address = convertAddress(claimant.address)
            )
          },
          documents = child.documents.map(_.name)
        )
      },
      benefits          = model.benefits.map(_.toString),
      paymentPreference = model.paymentPreference match {
        case JourneyModel.PaymentPreference.Weekly(bankAccountDetails) =>
          Weekly(bankAccountDetails.map(convertBankAccountDetails))

        case JourneyModel.PaymentPreference.EveryFourWeeks(bankAccountDetails) =>
          EveryFourWeeks(bankAccountDetails.map(convertBankAccountDetails))

        case JourneyModel.PaymentPreference.ExistingAccount(eldestChild) =>
          ExistingAccount(convertEldestChild(eldestChild))

        case JourneyModel.PaymentPreference.ExistingFrequency(bankAccountDetails, eldestChild) =>
          ExistingFrequency(bankAccountDetails.map(convertBankAccountDetails), convertEldestChild(eldestChild))

        case JourneyModel.PaymentPreference.DoNotPay =>
          DoNotPay
      }
    )

  private def convertAddress(address: models.Address): Address =
    Address(
      line1      = address.line1,
      line2      = address.line2,
      townOrCity = address.townOrCity,
      county     = address.county,
      postcode   = address.postcode
    )

  private def convertBankAccountDetails(details: models.BankAccountDetails): BankAccountDetails =
    BankAccountDetails(
      accountName   = details.accountName,
      sortCode      = details.sortCode,
      accountNumber = details.accountNumber,
      rollNumber    = details.rollNumber
    )

  private def convertAdultName(name: models.AdultName): AdultName =
    AdultName(
      title       = name.title,
      firstName   = name.firstName,
      middleNames = name.middleNames,
      lastName    = name.lastName
    )

  private def convertChildName(name: models.ChildName): ChildName =
    ChildName(
      firstName   = name.firstName,
      middleNames = name.middleNames,
      lastName    =name.lastName
    )

  private def convertEldestChild(eldestChild: models.JourneyModel.EldestChild): EldestChild =
    EldestChild(
      name        = convertChildName(eldestChild.name),
      dateOfBirth = eldestChild.dateOfBirth
    )

  private[audit] final case class AdultName(title: Option[String], firstName: String, middleNames: Option[String], lastName: String)
  object AdultName {
    implicit lazy val formats: Format[AdultName] = Json.format
  }

  private[audit] final case class ChildName(firstName: String, middleNames: Option[String], lastName: String)
  object ChildName {
    implicit lazy val formats: Format[ChildName] = Json.format
  }

  private[audit] final case class EldestChild(name: ChildName, dateOfBirth: LocalDate)
  object EldestChild {
    implicit lazy val formats: Format[EldestChild] = Json.format
  }

  private[audit] final case class Address(line1: String, line2: Option[String], townOrCity: String, county: Option[String], postcode: String)
  object Address {
    implicit lazy val formats: Format[Address] = Json.format
  }

  private[audit] final case class Applicant(
                                             name: AdultName,
                                             previousFamilyNames: List[String],
                                             dateOfBirth: LocalDate,
                                             nationalInsuranceNumber: Option[String],
                                             currentAddress: Address,
                                             previousAddress: Option[Address],
                                             telephoneNumber: String,
                                             bestTimeToContact: Set[String],
                                             nationality: String,
                                             employmentStatus: Set[String]
                                           )
  object Applicant {
    implicit lazy val formats: Format[Applicant] = Json.format
  }

  private[audit] final case class Partner(
                                           name: AdultName,
                                           dateOfBirth: LocalDate,
                                           nationality: String,
                                           currentlyEntitledToChildBenefit: Boolean,
                                           nationalInsuranceNumber: Option[String],
                                           waitingToHearAboutEntitlement: Option[Boolean],
                                           eldestChild: Option[EldestChild]
                                         )
  object Partner {
    implicit lazy val formats: Format[Partner] = Json.format
  }

  private[audit] final case class Relationship(status: String, since: Option[LocalDate], partner: Option[Partner])
  object Relationship {
    implicit lazy val formats: Format[Relationship] = Json.format
  }

  private[audit] final case class PreviousClaimant(name: AdultName, address: Address)
  object PreviousClaimant {
    implicit lazy val formats: Format[PreviousClaimant] = Json.format
  }

  private[audit] final case class Child(
                                         name: ChildName,
                                         nameChangedByDeedPoll: Option[Boolean],
                                         previousNames: List[ChildName],
                                         biologicalSex: String,
                                         birthRegistrationCountry: String,
                                         birthCertificateNumber: Option[String],
                                         relationshipToApplicant: String,
                                         adoptingThroughLocalAuthority: Option[Boolean],
                                         previousClaimant: Option[PreviousClaimant],
                                         documents: Set[String]
                                       )
  object Child {
    implicit lazy val formats: Format[Child] = Json.format
  }

  private[audit] final case class BankAccountDetails(accountName: String, sortCode: String, accountNumber: String, rollNumber: Option[String])
  object BankAccountDetails {
    implicit lazy val formats: Format[BankAccountDetails] = Json.format
  }

  private[audit] sealed trait PaymentPreference

  private[audit] final case class Weekly(bankAccountDetails: Option[BankAccountDetails]) extends PaymentPreference
  object Weekly {
    implicit lazy val writes: Writes[Weekly] = Writes {
      x =>

        val accountJsonValue = x.bankAccountDetails.map(Json.toJson(_)).getOrElse(JsString("no suitable account"))
        val accountJson      = Json.obj("account" -> accountJsonValue)

        Json.obj(
          "frequency" -> "weekly"
        ) ++ accountJson
    }
  }

  private[audit] final case class EveryFourWeeks(bankAccountDetails: Option[BankAccountDetails]) extends PaymentPreference
  object EveryFourWeeks {
    implicit lazy val writes: Writes[EveryFourWeeks] = Writes {
      x =>

        val accountJsonValue = x.bankAccountDetails.map(Json.toJson(_)).getOrElse(JsString("no suitable account"))
        val accountJson      = Json.obj("account" -> accountJsonValue)

        Json.obj(
          "frequency" -> "every four weeks"
        ) ++ accountJson
    }
  }

  private[audit] final case class  ExistingAccount(eldestChild: EldestChild) extends PaymentPreference
  object ExistingAccount {
    implicit lazy val writes: Writes[ExistingAccount] = Writes {
      x =>
        Json.obj(
          "frequency"   -> "use existing frequency",
          "account"     -> "use existing account",
          "eldestChild" -> Json.toJson(x.eldestChild)
        )
    }
  }

  private[audit] final case class  ExistingFrequency(bankAccountDetails: Option[BankAccountDetails], eldestChild: EldestChild) extends PaymentPreference
  object ExistingFrequency {
    implicit lazy val writes: Writes[ExistingFrequency] = Writes {
      x =>
        val accountJsonValue = x.bankAccountDetails.map(Json.toJson(_)).getOrElse(JsString("no suitable account"))
        val accountJson      = Json.obj("account" -> accountJsonValue)

        Json.obj(
          "frequency"   -> "use existing frequency",
          "eldestChild" -> Json.toJson(x.eldestChild)
        ) ++ accountJson
    }
  }

  private[audit] final case object DoNotPay extends PaymentPreference

  object PaymentPreference {

    implicit val writes: Writes[PaymentPreference] = Writes {
      case weekly: Weekly                       => Json.toJson(weekly)(Weekly.writes)
      case everyFourWeeks: EveryFourWeeks       => Json.toJson(everyFourWeeks)(EveryFourWeeks.writes)
      case existingAccount: ExistingAccount     => Json.toJson(existingAccount)(ExistingAccount.writes)
      case existingFrequency: ExistingFrequency => Json.toJson(existingFrequency)(ExistingFrequency.writes)
      case DoNotPay                             => JsString("Do not pay")
    }
  }

  implicit lazy val writes: Writes[DownloadAuditEvent] = Json.writes
}