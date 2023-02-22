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
import cats.data.NonEmptyList
import generators.ModelGenerators
import models.AdditionalInformation.Information
import models.BirthRegistrationMatchingResult.NotAttempted
import models.{ApplicantPreviousName, BirthCertificateSystemNumber, Country, CurrentlyReceivingChildBenefit, EmploymentStatus, JourneyModel, Nationality, PartnerClaimingChildBenefit}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ModelGenerators with OptionValues {

  val mockAuditConnector: AuditConnector = mock[AuditConnector]
  val configuration: Configuration = Configuration(
    "auditing.downloadEventName" -> "downloadAuditEvent",
    "auditing.validateBankDetailsEventName" -> "validateBankDetailsEventName"
  )
  val service = new AuditService(mockAuditConnector, configuration)

  ".auditDownload" - {

    "must call the audit connector with the correct payload" in {

      val now = LocalDate.now
      val applicantNino = arbitrary[Nino].sample.value.toString
      val partnerNino   = arbitrary[Nino].sample.value.toString

      val model = JourneyModel(
        applicant = JourneyModel.Applicant(
          name = models.AdultName(title = Some("title"), firstName = "applicant first", middleNames = Some("applicant middle"), lastName = "applicant last"),
          previousFamilyNames = List(ApplicantPreviousName("previous family name")),
          dateOfBirth = now,
          nationalInsuranceNumber = Some(applicantNino),
          currentAddress = models.UkAddress("current line 1", Some("current line 2"), "current town", Some("current county"), "current postcode"),
          previousAddress = Some(models.UkAddress("previous line 1", Some("previous line 2"), "previous town", Some("previous county"), "previous postcode")),
          telephoneNumber = "07777 777777",
          nationalities = NonEmptyList(Nationality.allNationalities.head, Nil),
          residency = JourneyModel.Residency.LivedInUkAndAbroad(Some(Country.internationalCountries.head), Some(LocalDate.now), EmploymentStatus.activeStatuses, List(Country("ES", "Spain")), List(Country("ES", "Spain"))),
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
          changedDesignatoryDetails = Some(true),
          correspondenceAddress = Some(models.UkAddress("corr 1", Some("corr 2"), "corr town", Some("corr county"), "corr postcode"))
        ),
        relationship = JourneyModel.Relationship(
          status = models.RelationshipStatus.Cohabiting,
          since = Some(now),
          partner = Some(models.JourneyModel.Partner(
            name = models.AdultName(title = Some("title"), firstName = "partner first", middleNames = Some("partner middle"), lastName = "partner last"),
            dateOfBirth = now,
            nationalities = NonEmptyList(Nationality.allNationalities.head, Nil),
            nationalInsuranceNumber = Some(partnerNino),
            memberOfHMForcesOrCivilServantAbroad = false,
            currentlyClaimingChildBenefit = PartnerClaimingChildBenefit.GettingPayments,
            eldestChild = Some(JourneyModel.EldestChild(
              name        = models.ChildName("partner eldest child first", Some("partner eldest child middle"), "partner eldest child last"),
              dateOfBirth = now
            )),
            countriesWorked = List(Country("ES", "Spain")),
            countriesReceivedBenefits = List(Country("ES", "Spain")),
            employmentStatus = EmploymentStatus.activeStatuses
          ))
        ),
        children = NonEmptyList(
          JourneyModel.Child(
            name = models.ChildName("child 1 first", Some("child 1 middle"), "child 1 last"),
            nameChangedByDeedPoll = Some(true),
            previousNames = List(models.ChildName("child 1 previous first", Some("child 1 previous middle"), "child 1 previous last")),
            biologicalSex = models.ChildBiologicalSex.Female,
            dateOfBirth = now,
            countryOfRegistration = models.ChildBirthRegistrationCountry.England,
            birthCertificateNumber = Some(BirthCertificateSystemNumber("000000000")),
            birthCertificateDetailsMatched = NotAttempted,
            relationshipToApplicant = models.ApplicantRelationshipToChild.BirthChild,
            adoptingThroughLocalAuthority = true,
            previousClaimant = Some(JourneyModel.PreviousClaimant(
              name    = Some(models.AdultName(title = Some("title"), "previous claimant first", Some("previous claimant middle"), "previous claimant last")),
              address = Some(models.UkAddress("previous claimant line 1", Some("previous claimant line 2"), "previous claimant town", Some("previous claimant county"), "previous claimant postcode"))
            ),
              ),
            guardian = Some(JourneyModel.Guardian(
              name    = Some(models.AdultName(title = Some("title"), "guardian first", Some("guardian middle"), "guardian last")),
              address = Some(models.UkAddress("guardian line 1", Some("guardian line 2"), "guardian town", Some("guardian county"), "guardian postcode"))
            )),
            previousGuardian = Some(JourneyModel.PreviousGuardian(
              name        = Some(models.AdultName(title = Some("title"), "previous guardian first", Some("previous guardian middle"), "previous guardian last")),
              address     = Some(models.UkAddress("previous guardian line 1", Some("previous guardian line 2"), "previous guardian town", Some("previous guardian county"), "previous guardian postcode")),
              phoneNumber = Some("previous guardian phone")
            )),
            dateChildStartedLivingWithApplicant = Some(LocalDate.now)
          ), Nil
        ),
        benefits = Some(Set(models.Benefits.IncomeSupport, models.Benefits.JobseekersAllowance)),
        paymentPreference = JourneyModel.PaymentPreference.Weekly(
          accountDetails = Some(JourneyModel.BankAccountWithHolder(
            holder = models.BankAccountHolder.Applicant,
            details = models.BankAccountDetails("first", "last", "000000", "00000000")
          )),
          eldestChild = Some(JourneyModel.EldestChild(
            name        = models.ChildName("applicant eldest first", Some("applicant eldest middle"), "applicant eldest last"),
            dateOfBirth = now
          ))
        ),
        additionalInformation = Information("info")
      )

      val expectedAuditEvent: DownloadAuditEvent = DownloadAuditEvent(
        applicant = Applicant(
          name = AdultName(title = Some("title"), firstName = "applicant first", middleNames = Some("applicant middle"), lastName = "applicant last"),
          previousFamilyNames = List("previous family name"),
          dateOfBirth = now,
          nationalInsuranceNumber = Some(applicantNino),
          currentAddress = UkAddress("current line 1", Some("current line 2"), "current town", Some("current county"), "current postcode"),
          previousAddress = Some(UkAddress("previous line 1", Some("previous line 2"), "previous town", Some("previous county"), "previous postcode")),
          telephoneNumber = "07777 777777",
          nationalities = Seq(Nationality.allNationalities.head.name),
          residency = Residency.LivedInUkAndAbroad(Some(Country.internationalCountries.head.name), Some(LocalDate.now), EmploymentStatus.activeStatuses.map(_.toString), List("Spain"), List("Spain")),
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyClaimingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming.toString,
          changedDesignatoryDetails = Some(true),
          correspondenceAddress = Some(UkAddress("corr 1", Some("corr 2"), "corr town", Some("corr county"), "corr postcode"))
        ),
        relationship = Relationship(
          status = "cohabiting",
          since = Some(now),
          partner = Some(Partner(
            name = AdultName(title = Some("title"), firstName = "partner first", middleNames = Some("partner middle"), lastName = "partner last"),
            dateOfBirth = now,
            nationalities = Seq(Nationality.allNationalities.head.name),
            nationalInsuranceNumber = Some(partnerNino),
            memberOfHMForcesOrCivilServantAbroad = false,
            currentlyClaimingChildBenefit = PartnerClaimingChildBenefit.GettingPayments.toString,
            eldestChild = Some(EldestChild(
              name        = ChildName("partner eldest child first", Some("partner eldest child middle"), "partner eldest child last"),
              dateOfBirth = now
            )),
            countriesWorked = Seq("Spain"),
            countriesReceivedBenefits = Seq("Spain"),
            employmentStatus = EmploymentStatus.activeStatuses.map(_.toString)
          ))
        ),
        children = List(
          Child(
            name = ChildName("child 1 first", Some("child 1 middle"), "child 1 last"),
            nameChangedByDeedPoll = Some(true),
            previousNames = List(ChildName("child 1 previous first", Some("child 1 previous middle"), "child 1 previous last")),
            biologicalSex = "female",
            dateOfBirth = now,
            birthRegistrationCountry = "england",
            birthCertificateNumber = Some("000000000"),
            birthCertificateDetailsMatched = "notAttempted",
            relationshipToApplicant = "birthChild",
            adoptingThroughLocalAuthority = true,
            previousClaimant = Some(PreviousClaimant(
              name    = Some(AdultName(title = Some("title"), "previous claimant first", Some("previous claimant middle"), "previous claimant last")),
              address = Some(UkAddress("previous claimant line 1", Some("previous claimant line 2"), "previous claimant town", Some("previous claimant county"), "previous claimant postcode"))
            )),
            guardian = Some(Guardian(
              name    = Some(AdultName(title = Some("title"), "guardian first", Some("guardian middle"), "guardian last")),
              address = Some(UkAddress("guardian line 1", Some("guardian line 2"), "guardian town", Some("guardian county"), "guardian postcode"))
            )),
            previousGuardian = Some(PreviousGuardian(
              name        = Some(AdultName(title = Some("title"), "previous guardian first", Some("previous guardian middle"), "previous guardian last")),
              address     = Some(UkAddress("previous guardian line 1", Some("previous guardian line 2"), "previous guardian town", Some("previous guardian county"), "previous guardian postcode")),
              phoneNumber = Some("previous guardian phone")
            )),
            dateChildStartedLivingWithApplicant = Some(LocalDate.now)
          )
        ),
        benefits = Some(Set("incomeSupport", "jobseekersAllowance")),
        paymentPreference = Weekly(
          bankAccount = Some(BankAccount("applicant", "first", "last", "000000", "00000000")),
          eldestChild = Some(EldestChild(
            name        = ChildName("applicant eldest first", Some("applicant eldest middle"), "applicant eldest last"),
            dateOfBirth = now
          ))
        ),
        "info"
      )

      val hc = HeaderCarrier()
      service.auditDownload(model)(hc)

      verify(mockAuditConnector, times(1)).sendExplicitAudit(eqTo("downloadAuditEvent"), eqTo(expectedAuditEvent))(eqTo(hc), any(), any())
    }
  }
}
