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
import cats.data.NonEmptyList
import generators.ModelGenerators
import models.JourneyModel
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
          name = models.AdultName(firstName = "applicant first", middleNames = Some("applicant middle"), lastName = "applicant last"),
          previousFamilyNames = List("previous family name"),
          dateOfBirth = now,
          nationalInsuranceNumber = Some(applicantNino),
          currentAddress = models.UkAddress("current line 1", Some("current line 2"), "current town", Some("current county"), "current postcode"),
          previousAddress = Some(models.UkAddress("previous line 1", Some("previous line 2"), "previous town", Some("previous county"), "previous postcode")),
          telephoneNumber = "07777 777777",
          bestTimeToContact = Set(models.BestTimeToContact.Morning, models.BestTimeToContact.Afternoon, models.BestTimeToContact.Evening),
          nationality = "applicant nationality",
          employmentStatus = Set(models.EmploymentStatus.Employed, models.EmploymentStatus.SelfEmployed),
          memberOfHMForcesOrCivilServantAbroad = false
        ),
        relationship = JourneyModel.Relationship(
          status = models.RelationshipStatus.Cohabiting,
          since = Some(now),
          partner = Some(models.JourneyModel.Partner(
            name = models.AdultName(firstName = "partner first", middleNames = Some("partner middle"), lastName = "partner last"),
            dateOfBirth = now,
            nationality = "partner nationality",
            nationalInsuranceNumber = Some(partnerNino),
            employmentStatus = Set(models.EmploymentStatus.Employed, models.EmploymentStatus.SelfEmployed),
            memberOfHMForcesOrCivilServantAbroad = false,
            currentlyEntitledToChildBenefit = false,
            waitingToHearAboutEntitlement = Some(true),
            eldestChild = Some(JourneyModel.EldestChild(
              name        = models.ChildName("partner eldest child first", Some("partner eldest child middle"), "partner eldest child last"),
              dateOfBirth = now
            ))
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
            birthCertificateNumber = Some("000000000"),
            relationshipToApplicant = models.ApplicantRelationshipToChild.BirthChild,
            adoptingThroughLocalAuthority = true,
            previousClaimant = Some(JourneyModel.PreviousClaimant(
              name    = models.AdultName("previous claimant first", Some("previous claimant middle"), "previous claimant last"),
              address = models.UkAddress("previous claimant line 1", Some("previous claimant line 2"), "previous claimant town", Some("previous claimant county"), "previous claimant postcode")
            ))
          ), Nil
        ),
        benefits = Set(models.Benefits.IncomeSupport, models.Benefits.JobseekersAllowance),
        paymentPreference = JourneyModel.PaymentPreference.ExistingFrequency(
          bankAccount = Some(JourneyModel.BankAccount(
            holder = models.BankAccountHolder.Applicant,
            details = models.BankAccountDetails("name on account", "bank name", "000000", "00000000", Some("roll number"))
          )),
          eldestChild        = JourneyModel.EldestChild(
            name        = models.ChildName("applicant eldest first", Some("applicant eldest middle"), "applicant eldest last"),
            dateOfBirth = now
          )
        )
      )

      val expectedAuditEvent: DownloadAuditEvent = DownloadAuditEvent(
        applicant = Applicant(
          name = AdultName(firstName = "applicant first", middleNames = Some("applicant middle"), lastName = "applicant last"),
          previousFamilyNames = List("previous family name"),
          dateOfBirth = now,
          nationalInsuranceNumber = Some(applicantNino),
          currentAddress = UkAddress("current line 1", Some("current line 2"), "current town", Some("current county"), "current postcode"),
          previousAddress = Some(UkAddress("previous line 1", Some("previous line 2"), "previous town", Some("previous county"), "previous postcode")),
          telephoneNumber = "07777 777777",
          bestTimeToContact = Set("morning", "afternoon", "evening"),
          nationality = "applicant nationality",
          employmentStatus = Set("employed", "selfEmployed"),
          memberOfHMForcesOrCivilServantAbroad = false
        ),
        relationship = Relationship(
          status = "cohabiting",
          since = Some(now),
          partner = Some(Partner(
            name = AdultName(firstName = "partner first", middleNames = Some("partner middle"), lastName = "partner last"),
            dateOfBirth = now,
            nationality = "partner nationality",
            nationalInsuranceNumber = Some(partnerNino),
            employmentStatus = Set("employed", "selfEmployed"),
            memberOfHMForcesOrCivilServantAbroad = false,
            currentlyEntitledToChildBenefit = false,
            waitingToHearAboutEntitlement = Some(true),
            eldestChild = Some(EldestChild(
              name        = ChildName("partner eldest child first", Some("partner eldest child middle"), "partner eldest child last"),
              dateOfBirth = now
            ))
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
            relationshipToApplicant = "birthChild",
            adoptingThroughLocalAuthority = true,
            previousClaimant = Some(PreviousClaimant(
              name    = AdultName( "previous claimant first", Some("previous claimant middle"), "previous claimant last"),
              address = UkAddress("previous claimant line 1", Some("previous claimant line 2"), "previous claimant town", Some("previous claimant county"), "previous claimant postcode")
            ))
          )
        ),
        benefits = Set("incomeSupport", "jobseekersAllowance"),
        paymentPreference = ExistingFrequency(
          bankAccount = Some(BankAccount("applicant", "name on account", "bank name", "000000", "00000000", Some("roll number"))),
          eldestChild        = EldestChild(
            name        = ChildName("applicant eldest first", Some("applicant eldest middle"), "applicant eldest last"),
            dateOfBirth = now
          )
        )
      )

      val hc = HeaderCarrier()
      service.auditDownload(model)(hc)

      verify(mockAuditConnector, times(1)).sendExplicitAudit(eqTo("downloadAuditEvent"), eqTo(expectedAuditEvent))(eqTo(hc), any(), any())
    }
  }
}
