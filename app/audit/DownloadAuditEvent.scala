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

package audit

import audit.DownloadAuditEvent._
import models.{Country, JourneyModel, PaymentFrequency}
import play.api.libs.json.{JsString, Json, Writes}

import java.time.LocalDate

final case class DownloadAuditEvent(
                                     applicant: Applicant,
                                     relationship: Relationship,
                                     children: List[Child],
                                     benefits: Option[Set[String]],
                                     paymentPreference: PaymentPreference,
                                     additionalInformation: String
                                   )

object DownloadAuditEvent {

  def from(model: JourneyModel): DownloadAuditEvent =
    DownloadAuditEvent(
      applicant = Applicant(
        name                                 = convertAdultName(model.applicant.name),
        previousFamilyNames                  = model.applicant.previousFamilyNames.map(_.lastName),
        dateOfBirth                          = model.applicant.dateOfBirth,
        nationalInsuranceNumber              = model.applicant.nationalInsuranceNumber,
        currentAddress                       = convertAddress(model.applicant.currentAddress),
        previousAddress                      = model.applicant.previousAddress.map(convertAddress),
        telephoneNumber                      = model.applicant.telephoneNumber,
        nationalities                        = model.applicant.nationalities.toList.map(_.name),
        residency                            = convertResidency(model.applicant.residency),
        memberOfHMForcesOrCivilServantAbroad = model.applicant.memberOfHMForcesOrCivilServantAbroad,
        currentlyClaimingChildBenefit        = model.applicant.currentlyReceivingChildBenefit.toString
      ),
      relationship = Relationship(
        status  = model.relationship.status.toString,
        since   = model.relationship.since,
        partner = model.relationship.partner.map {
          partner => Partner(
            name                                 = convertAdultName(partner.name),
            dateOfBirth                          = partner.dateOfBirth,
            nationalities                        = partner.nationalities.toList.map(_.name),
            nationalInsuranceNumber              = partner.nationalInsuranceNumber,
            currentlyClaimingChildBenefit        = partner.currentlyClaimingChildBenefit.toString,
            memberOfHMForcesOrCivilServantAbroad = partner.memberOfHMForcesOrCivilServantAbroad,
            eldestChild                          = partner.eldestChild.map(convertEldestChild),
            countriesWorked                      = partner.countriesWorked.map(_.name),
            countriesReceivedBenefits            = partner.countriesReceivedBenefits.map(_.name),
            employmentStatus                     = partner.employmentStatus.map(_.toString)
          )
        }
      ),
      children = model.children.toList.map { child =>
        Child(
          name                            = convertChildName(child.name),
          nameChangedByDeedPoll           = child.nameChangedByDeedPoll,
          previousNames                   = child.previousNames.map(convertChildName),
          biologicalSex                   = child.biologicalSex.toString,
          dateOfBirth                     = child.dateOfBirth,
          birthRegistrationCountry        = child.countryOfRegistration.toString,
          birthCertificateNumber          = child.birthCertificateNumber.map(_.display),
          birthCertificateDetailsMatched = child.birthCertificateDetailsMatched.toString,
          relationshipToApplicant         = child.relationshipToApplicant.toString,
          adoptingThroughLocalAuthority   = child.adoptingThroughLocalAuthority,
          previousClaimant                = child.previousClaimant.map { claimant =>
            PreviousClaimant(
              name    = claimant.name.map(convertAdultName),
              address = claimant.address.map(convertAddress)
            )
          },
          guardian = child.guardian.map { guardian =>
            Guardian(
              name    = guardian.name.map(convertAdultName),
              address = guardian.address.map(convertAddress)
            )
          },
          previousGuardian = child.previousGuardian.map { previousGuardian =>
            PreviousGuardian(
              name        = previousGuardian.name.map(convertAdultName),
              address     = previousGuardian.address.map(convertAddress),
              phoneNumber = previousGuardian.phoneNumber
            )
          },
          dateChildStartedLivingWithApplicant = child.dateChildStartedLivingWithApplicant
        )
      },
      benefits          = model.benefits.map(_.map(_.toString)),
      paymentPreference = model.paymentPreference match {
        case JourneyModel.PaymentPreference.Weekly(bankAccount, eldestChild) =>
          Weekly(
            bankAccount.map(convertAccountDetails),
            eldestChild.map(convertEldestChild)
          )

        case JourneyModel.PaymentPreference.EveryFourWeeks(bankAccount, eldestChild) =>
          EveryFourWeeks(
            bankAccount.map(convertAccountDetails),
            eldestChild.map(convertEldestChild)
          )

        case JourneyModel.PaymentPreference.ExistingAccount(eldestChild, frequency) =>
          ExistingAccount(convertEldestChild(eldestChild), frequency.toString)

        case JourneyModel.PaymentPreference.DoNotPay(eldestChild) =>
          DoNotPay(eldestChild.map(convertEldestChild))
      },
      additionalInformation = model.additionalInformation.toString
    )

  private def convertUkAddress(address: models.UkAddress): UkAddress =
    UkAddress(
      line1      = address.line1,
      line2      = address.line2,
      townOrCity = address.townOrCity,
      county     = address.county,
      postcode   = address.postcode
    )

  private def convertInternationalAddress(address: models.InternationalAddress): InternationalAddress =
    InternationalAddress(
      line1         = address.line1,
      line2         = address.line2,
      townOrCity    = address.townOrCity,
      stateOrRegion = address.stateOrRegion,
      postcode      = address.postcode,
      country       = address.country
    )

  private def convertAddress(address: models.Address): Address = {
    address match {
      case u: models.UkAddress            => convertUkAddress(u)
      case i: models.InternationalAddress => convertInternationalAddress(i)
    }
  }

  private def convertAccountDetails(details: JourneyModel.AccountDetailsWithHolder): AccountDetails =
    details match {
      case bankAccount: JourneyModel.BankAccountWithHolder =>
        BankAccount(
          holder = bankAccount.holder.toString,
          firstName = bankAccount.details.firstName,
          lastName = bankAccount.details.lastName,
          sortCode = bankAccount.details.sortCode,
          accountNumber = bankAccount.details.accountNumber
        )

      case buildingSociety: JourneyModel.BuildingSocietyWithHolder =>
        BuildingSociety(
          holder = buildingSociety.holder.toString,
          firstName = buildingSociety.details.firstName,
          lastName = buildingSociety.details.lastName,
          buildingSociety = buildingSociety.details.buildingSociety.name,
          rollNumber = buildingSociety.details.rollNumber
        )
    }

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
      lastName    = name.lastName
    )

  private def convertEldestChild(eldestChild: models.JourneyModel.EldestChild): EldestChild =
    EldestChild(
      name        = convertChildName(eldestChild.name),
      dateOfBirth = eldestChild.dateOfBirth
    )

  private def convertResidency(residency: models.JourneyModel.Residency): Residency =
    residency match {
      case models.JourneyModel.Residency.AlwaysLivedInUk =>
        Residency.AlwaysLivedInUk

      case models.JourneyModel.Residency.LivedInUkAndAbroad(usualCountry, arrivalDate, employmentStatus, countriesWorked, countriesReceivedBenefits) =>
        Residency.LivedInUkAndAbroad(usualCountry.map(_.name), arrivalDate, employmentStatus.map(_.toString), countriesWorked.map(_.name), countriesReceivedBenefits.map(_.name))

      case models.JourneyModel.Residency.AlwaysLivedAbroad(usualCountry, employmentStatus, countriesWorked, countriesReceivedBenefits) =>
        Residency.AlwaysLivedAbroad(usualCountry.name, employmentStatus.map(_.toString), countriesWorked.map(_.name), countriesReceivedBenefits.map(_.name))
    }

  private[audit] final case class AdultName(title: Option[String], firstName: String, middleNames: Option[String], lastName: String)
  object AdultName {
    implicit lazy val writes: Writes[AdultName] = Json.writes
  }

  private[audit] final case class ChildName(firstName: String, middleNames: Option[String], lastName: String)
  object ChildName {
    implicit lazy val writes: Writes[ChildName] = Json.writes
  }

  private[audit] final case class EldestChild(name: ChildName, dateOfBirth: LocalDate)
  object EldestChild {
    implicit lazy val writes: Writes[EldestChild] = Json.writes
  }

  private[audit] sealed trait Address
  object Address {
    implicit val writes: Writes[Address] = Writes {
      case u: UkAddress            => Json.toJson(u)(UkAddress.writes)
      case i: InternationalAddress => Json.toJson(i)(InternationalAddress.writes)
    }
  }

  private[audit] final case class UkAddress(line1: String, line2: Option[String], townOrCity: String, county: Option[String], postcode: String) extends Address
  object UkAddress {
    implicit lazy val writes: Writes[UkAddress] = Json.writes
  }

  private[audit] final case class InternationalAddress(line1: String, line2: Option[String], townOrCity: String, stateOrRegion: Option[String], postcode: Option[String], country: Country) extends Address
  object InternationalAddress {
    implicit lazy val writes: Writes[InternationalAddress] = Json.writes
  }

  private[audit] trait Residency
  private[audit] object Residency {

    case object AlwaysLivedInUk extends Residency

    final case class LivedInUkAndAbroad(usualCountryOfResidence: Option[String], arrivalDate: Option[LocalDate], employmentStatus: Set[String], countriesWorked: List[String], countriesReceivedBenefits: List[String]) extends Residency
    object LivedInUkAndAbroad {
      implicit lazy val writes: Writes[LivedInUkAndAbroad] = Writes { a =>

        val arrivalDateJson = a.arrivalDate.map(d => Json.obj("arrivalDate" -> d)).getOrElse(Json.obj())
        val countryJson = a.usualCountryOfResidence.map(c => Json.obj("usualCountryOfResidence" -> c, "usuallyLivesInUk" -> false)).getOrElse(Json.obj("usuallyLivesInUk" -> true))
        val countriesWorkedJson = if(a.countriesWorked.nonEmpty) Json.obj("countriesRecentlyWorked" -> a.countriesWorked) else Json.obj()
        val countreisReceivedBenefitsJson = if(a.countriesReceivedBenefits.nonEmpty) Json.obj("countriesRecentlyReceivedBenefits" -> a.countriesReceivedBenefits) else Json.obj()
        val employmentJson = if(a.employmentStatus.nonEmpty) Json.obj("employmentStatus" -> a.employmentStatus) else Json.obj()

        Json.obj(
          "alwaysLivedInUk" -> false
        ) ++ arrivalDateJson ++ countryJson ++ countriesWorkedJson ++ countreisReceivedBenefitsJson ++ employmentJson
      }
    }

    final case class AlwaysLivedAbroad(usualCountryOfResidence: String, employmentStatus: Set[String], countriesWorked: List[String], countriesReceivedBenefits: List[String]) extends Residency
    object AlwaysLivedAbroad {
      implicit lazy val writes: Writes[AlwaysLivedAbroad] = Writes { a =>

        val countriesWorkedJson = if (a.countriesWorked.nonEmpty) Json.obj("countriesRecentlyWorked" -> a.countriesWorked) else Json.obj()
        val countreisReceivedBenefitsJson = if (a.countriesReceivedBenefits.nonEmpty) Json.obj("countriesRecentlyReceivedBenefits" -> a.countriesReceivedBenefits) else Json.obj()
        val employmentJson = if(a.employmentStatus.nonEmpty) Json.obj("employmentStatus" -> a.employmentStatus) else Json.obj()

        Json.obj(
          "alwaysLivedInUk" -> false,
          "usuallyLivesInUk" -> false,
          "usualCountryOfResidence" -> a.usualCountryOfResidence
        ) ++ countriesWorkedJson ++ countreisReceivedBenefitsJson ++ employmentJson
      }
    }

    implicit lazy val writes: Writes[Residency] = Writes {
      case AlwaysLivedInUk       => Json.obj("alwaysLivedInUk" -> true)
      case a: LivedInUkAndAbroad => Json.toJson(a)(LivedInUkAndAbroad.writes)
      case a: AlwaysLivedAbroad  => Json.toJson(a)(AlwaysLivedAbroad.writes)
    }
  }

  private[audit] final case class Applicant(
                                             name: AdultName,
                                             previousFamilyNames: List[String],
                                             dateOfBirth: LocalDate,
                                             nationalInsuranceNumber: Option[String],
                                             currentAddress: Address,
                                             previousAddress: Option[Address],
                                             telephoneNumber: String,
                                             nationalities: Seq[String],
                                             residency: Residency,
                                             memberOfHMForcesOrCivilServantAbroad: Option[Boolean],
                                             currentlyClaimingChildBenefit: String
                                           )
  object Applicant {
    implicit lazy val writes: Writes[Applicant] = Json.writes
  }

  private[audit] final case class Partner(
                                           name: AdultName,
                                           dateOfBirth: LocalDate,
                                           nationalities: Seq[String],
                                           currentlyClaimingChildBenefit: String,
                                           nationalInsuranceNumber: Option[String],
                                           memberOfHMForcesOrCivilServantAbroad: Option[Boolean],
                                           eldestChild: Option[EldestChild],
                                           countriesWorked: Seq[String],
                                           countriesReceivedBenefits: Seq[String],
                                           employmentStatus: Set[String]
                                         )
  object Partner {
    implicit lazy val writes: Writes[Partner] = Json.writes
  }

  private[audit] final case class Relationship(status: String, since: Option[LocalDate], partner: Option[Partner])
  object Relationship {
    implicit lazy val writes: Writes[Relationship] = Json.writes
  }

  private[audit] final case class PreviousClaimant(name: Option[AdultName], address: Option[Address])

  object PreviousClaimant {
    implicit lazy val writes: Writes[PreviousClaimant] = Json.writes
  }

  private[audit] final case class Guardian(name: Option[AdultName], address: Option[Address])
  object Guardian {
    implicit lazy val writes: Writes[Guardian] = Json.writes
  }

  private[audit] final case class PreviousGuardian(name: Option[AdultName], address: Option[Address], phoneNumber: Option[String])
  object PreviousGuardian {
    implicit lazy val writes: Writes[PreviousGuardian] = Json.writes
  }

  private[audit] final case class Child(
                                         name: ChildName,
                                         nameChangedByDeedPoll: Option[Boolean],
                                         previousNames: List[ChildName],
                                         biologicalSex: String,
                                         dateOfBirth: LocalDate,
                                         birthRegistrationCountry: String,
                                         birthCertificateNumber: Option[String],
                                         birthCertificateDetailsMatched: String,
                                         relationshipToApplicant: String,
                                         adoptingThroughLocalAuthority: Boolean,
                                         previousClaimant: Option[PreviousClaimant],
                                         guardian: Option[Guardian],
                                         previousGuardian: Option[PreviousGuardian],
                                         dateChildStartedLivingWithApplicant: Option[LocalDate]
                                       )
  object Child {
    implicit lazy val writes: Writes[Child] = Json.writes
  }

  private[audit] sealed trait AccountDetails
  object AccountDetails {
    implicit lazy val writes: Writes[AccountDetails] = Writes {
      case x: BankAccount => Json.toJson(x)(BankAccount.writes)
      case x: BuildingSociety => Json.toJson(x)(BuildingSociety.writes)
    }
  }

  private[audit] final case class BuildingSociety(holder: String, firstName: String, lastName: String, buildingSociety: String, rollNumber: String) extends AccountDetails
  object BuildingSociety {
    implicit lazy val writes: Writes[BuildingSociety] = Json.writes
  }

  private[audit] final case class BankAccount(holder: String, firstName: String, lastName: String, sortCode: String, accountNumber: String) extends AccountDetails
  object BankAccount {
    implicit lazy val writes: Writes[BankAccount] = Json.writes
  }

  private[audit] sealed trait PaymentPreference

  private[audit] final case class Weekly(bankAccount: Option[AccountDetails], eldestChild: Option[EldestChild]) extends PaymentPreference
  object Weekly {
    implicit lazy val writes: Writes[Weekly] = Writes {
      x =>

        val accountJsonValue = x.bankAccount.map(Json.toJson(_)).getOrElse(JsString("no suitable account"))
        val accountJson      = Json.obj("account" -> accountJsonValue)

        val eldestChildJson =
          x.eldestChild
            .map(child => Json.obj("eldestChild" -> Json.toJson(child)))
            .getOrElse(Json.obj())

        Json.obj(
          "wantsToBePaid" -> true,
          "frequency"     -> PaymentFrequency.Weekly.toString
        ) ++ accountJson ++ eldestChildJson
    }
  }

  private[audit] final case class EveryFourWeeks(bankAccount: Option[AccountDetails], eldestChild: Option[EldestChild]) extends PaymentPreference
  object EveryFourWeeks {
    implicit lazy val writes: Writes[EveryFourWeeks] = Writes {
      x =>

        val accountJsonValue = x.bankAccount.map(Json.toJson(_)).getOrElse(JsString("no suitable account"))
        val accountJson      = Json.obj("account" -> accountJsonValue)

        val eldestChildJson =
          x.eldestChild
            .map(child => Json.obj("eldestChild" -> Json.toJson(child)))
            .getOrElse(Json.obj())

        Json.obj(
          "wantsToBePaid" -> true,
          "frequency"     -> PaymentFrequency.EveryFourWeeks.toString
        ) ++ accountJson ++ eldestChildJson
    }
  }

  private[audit] final case class  ExistingAccount(eldestChild: EldestChild, frequency: String) extends PaymentPreference
  object ExistingAccount {
    implicit lazy val writes: Writes[ExistingAccount] = Writes {
      x =>
        Json.obj(
          "wantsToBePaid" -> true,
          "frequency"     -> x.frequency,
          "account"       -> "use existing account",
          "eldestChild"   -> Json.toJson(x.eldestChild)
        )
    }
  }

  private[audit] final case class DoNotPay(eldestChild: Option[EldestChild]) extends PaymentPreference
  object DoNotPay {
    implicit lazy val writes: Writes[DoNotPay] = Writes {
      x =>

      val eldestChildJson = x.eldestChild
        .map(child => Json.obj("eldestChild" -> Json.toJson(child)))
        .getOrElse(Json.obj())

        Json.obj("wantsToBePaid" -> false) ++ eldestChildJson
    }
  }

  object PaymentPreference {

    implicit val writes: Writes[PaymentPreference] = Writes {
      case weekly: Weekly                   => Json.toJson(weekly)(Weekly.writes)
      case everyFourWeeks: EveryFourWeeks   => Json.toJson(everyFourWeeks)(EveryFourWeeks.writes)
      case existingAccount: ExistingAccount => Json.toJson(existingAccount)(ExistingAccount.writes)
      case doNotPay: DoNotPay               => Json.toJson(doNotPay)(DoNotPay.writes)
    }
  }

  implicit lazy val writes: Writes[DownloadAuditEvent] = Json.writes
}