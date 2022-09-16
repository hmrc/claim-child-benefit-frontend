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

import models.Index
import org.scalacheck.Arbitrary
import pages.{child, _}
import pages.applicant._
import pages.child._
import pages.income._
import pages.partner._
import pages.payments._

trait PageGenerators {

  implicit lazy val arbitraryPartnerIsHmfOrCivilServantPage: Arbitrary[PartnerIsHmfOrCivilServantPage.type] =
    Arbitrary(PartnerIsHmfOrCivilServantPage)

  implicit lazy val arbitraryApplicantIsHmfOrCivilServantPage: Arbitrary[ApplicantIsHmfOrCivilServantPage.type] =
    Arbitrary(ApplicantIsHmfOrCivilServantPage)

  implicit lazy val arbitraryAdoptingChildPage: Arbitrary[AdoptingThroughLocalAuthorityPage] =
    Arbitrary(child.AdoptingThroughLocalAuthorityPage(Index(0)))

  implicit lazy val arbitraryRemoveChildPreviousNamePage: Arbitrary[RemoveChildPreviousNamePage] =
    Arbitrary(child.RemoveChildPreviousNamePage(Index(0), Index(0)))

  implicit lazy val arbitraryRemoveChildPage: Arbitrary[RemoveChildPage] =
    Arbitrary(child.RemoveChildPage(Index(0)))

  implicit lazy val arbitraryPreviousClaimantNamePage: Arbitrary[PreviousClaimantNamePage] =
    Arbitrary(child.PreviousClaimantNamePage(Index(0)))

  implicit lazy val arbitraryPreviousClaimantAddressPage: Arbitrary[PreviousClaimantUkAddressPage] =
    Arbitrary(child.PreviousClaimantUkAddressPage(Index(0)))

  implicit lazy val arbitraryChildScottishBirthCertificateDetailsPage: Arbitrary[ChildScottishBirthCertificateDetailsPage] =
    Arbitrary(child.ChildScottishBirthCertificateDetailsPage(Index(0)))

  implicit lazy val arbitraryChildPreviousNamePage: Arbitrary[ChildPreviousNamePage] =
    Arbitrary(child.ChildPreviousNamePage(Index(0), Index(0)))

  implicit lazy val arbitraryChildNameChangedByDeedPollPage: Arbitrary[ChildNameChangedByDeedPollPage] =
    Arbitrary(child.ChildNameChangedByDeedPollPage(Index(0)))

  implicit lazy val arbitraryChildNamePage: Arbitrary[ChildNamePage] =
    Arbitrary(child.ChildNamePage(Index(0)))

  implicit lazy val arbitraryChildHasPreviousNamePage: Arbitrary[ChildHasPreviousNamePage] =
    Arbitrary(child.ChildHasPreviousNamePage(Index(0)))

  implicit lazy val arbitraryChildDateOfBirthPage: Arbitrary[ChildDateOfBirthPage] =
    Arbitrary(child.ChildDateOfBirthPage(Index(0)))

  implicit lazy val arbitraryChildBirthRegistrationCountryPage: Arbitrary[ChildBirthRegistrationCountryPage] =
    Arbitrary(child.ChildBirthRegistrationCountryPage(Index(0)))

  implicit lazy val arbitraryChildBirthCertificateSystemNumberPage: Arbitrary[ChildBirthCertificateSystemNumberPage] =
    Arbitrary(child.ChildBirthCertificateSystemNumberPage(Index(0)))

  implicit lazy val arbitraryChildBiologicalSexPage: Arbitrary[ChildBiologicalSexPage] =
    Arbitrary(child.ChildBiologicalSexPage(Index(0)))

  implicit lazy val arbitraryApplicantRelationshipToChildPage: Arbitrary[ApplicantRelationshipToChildPage] =
    Arbitrary(ApplicantRelationshipToChildPage(Index(0)))

  implicit lazy val arbitraryAnyoneClaimedForChildBeforePage: Arbitrary[AnyoneClaimedForChildBeforePage] =
    Arbitrary(child.AnyoneClaimedForChildBeforePage(Index(0)))

  implicit lazy val arbitraryAddChildPreviousNamePage: Arbitrary[AddChildPreviousNamePage] =
    Arbitrary(child.AddChildPreviousNamePage(Index(0)))

  implicit lazy val arbitraryAddChildPage: Arbitrary[AddChildPage.type] =
    Arbitrary(AddChildPage)

  implicit lazy val arbitraryPartnerWaitingForEntitlementDecisionPage: Arbitrary[PartnerWaitingForEntitlementDecisionPage.type] =
    Arbitrary(PartnerWaitingForEntitlementDecisionPage)

  implicit lazy val arbitraryPartnerNinoKnownPage: Arbitrary[PartnerNinoKnownPage.type] =
    Arbitrary(PartnerNinoKnownPage)

  implicit lazy val arbitraryPartnerNinoPage: Arbitrary[PartnerNinoPage.type] =
    Arbitrary(PartnerNinoPage)

  implicit lazy val arbitraryPartnerNationalityPage: Arbitrary[PartnerNationalityPage.type] =
    Arbitrary(PartnerNationalityPage)

  implicit lazy val arbitraryPartnerNamePage: Arbitrary[PartnerNamePage.type] =
    Arbitrary(PartnerNamePage)

  implicit lazy val arbitraryPartnerEntitledToChildBenefitPage: Arbitrary[PartnerClaimingChildBenefitPage.type] =
    Arbitrary(PartnerClaimingChildBenefitPage)

  implicit lazy val arbitraryPartnerEmploymentStatusPage: Arbitrary[PartnerEmploymentStatusPage.type] =
    Arbitrary(PartnerEmploymentStatusPage)

  implicit lazy val arbitraryPartnerEldestChildNamePage: Arbitrary[PartnerEldestChildNamePage.type] =
    Arbitrary(PartnerEldestChildNamePage)

  implicit lazy val arbitraryPartnerEldestChildDateOfBirthPage: Arbitrary[PartnerEldestChildDateOfBirthPage.type] =
    Arbitrary(PartnerEldestChildDateOfBirthPage)

  implicit lazy val arbitraryPartnerDateOfBirthPage: Arbitrary[PartnerDateOfBirthPage.type] =
    Arbitrary(PartnerDateOfBirthPage)

  implicit lazy val arbitraryBestTimeToContactPage: Arbitrary[BestTimeToContactPage.type] =
    Arbitrary(BestTimeToContactPage)

  implicit lazy val arbitraryApplicantPreviousFamilyNamePage: Arbitrary[ApplicantPreviousFamilyNamePage] =
    Arbitrary(applicant.ApplicantPreviousFamilyNamePage(Index(0)))

  implicit lazy val arbitraryApplicantPreviousAddressPage: Arbitrary[ApplicantPreviousUkAddressPage.type] =
    Arbitrary(ApplicantPreviousUkAddressPage)

  implicit lazy val arbitraryApplicantPhoneNumberPage: Arbitrary[ApplicantPhoneNumberPage.type] =
    Arbitrary(ApplicantPhoneNumberPage)

  implicit lazy val arbitraryApplicantNinoKnownPage: Arbitrary[ApplicantNinoKnownPage.type] =
    Arbitrary(ApplicantNinoKnownPage)

  implicit lazy val arbitraryApplicantNinoPage: Arbitrary[ApplicantNinoPage.type] =
    Arbitrary(ApplicantNinoPage)

  implicit lazy val arbitraryApplicantNationalityPage: Arbitrary[ApplicantNationalityPage.type] =
    Arbitrary(ApplicantNationalityPage)

  implicit lazy val arbitraryApplicantNamePage: Arbitrary[ApplicantNamePage.type] =
    Arbitrary(ApplicantNamePage)

  implicit lazy val arbitraryApplicantLivedAtCurrentAddressOneYearPage: Arbitrary[ApplicantLivedAtCurrentAddressOneYearPage.type] =
    Arbitrary(ApplicantLivedAtCurrentAddressOneYearPage)

  implicit lazy val arbitraryApplicantHasPreviousFamilyNamePage: Arbitrary[ApplicantHasPreviousFamilyNamePage.type] =
    Arbitrary(ApplicantHasPreviousFamilyNamePage)

  implicit lazy val arbitraryApplicantEmploymentStatusPage: Arbitrary[ApplicantEmploymentStatusPage.type] =
    Arbitrary(ApplicantEmploymentStatusPage)

  implicit lazy val arbitraryApplicantDateOfBirthPage: Arbitrary[ApplicantDateOfBirthPage.type] =
    Arbitrary(ApplicantDateOfBirthPage)

  implicit lazy val arbitraryApplicantCurrentAddressPage: Arbitrary[ApplicantCurrentUkAddressPage.type] =
    Arbitrary(ApplicantCurrentUkAddressPage)

  implicit lazy val arbitraryAddApplicantPreviousFamilyNamePage: Arbitrary[AddApplicantPreviousFamilyNamePage.type] =
    Arbitrary(AddApplicantPreviousFamilyNamePage)

  implicit lazy val arbitraryWantToBePaidWeeklyPage: Arbitrary[PaymentFrequencyPage.type] =
    Arbitrary(PaymentFrequencyPage)

  implicit lazy val arbitraryWantToBePaidToExistingAccountPage: Arbitrary[WantToBePaidToExistingAccountPage.type] =
    Arbitrary(WantToBePaidToExistingAccountPage)

  implicit lazy val arbitraryWantToBePaidPage: Arbitrary[WantToBePaidPage.type] =
    Arbitrary(WantToBePaidPage)

  implicit lazy val arbitraryEldestChildNamePage: Arbitrary[EldestChildNamePage.type] =
    Arbitrary(EldestChildNamePage)

  implicit lazy val arbitraryEldestChildDateOfBirthPage: Arbitrary[EldestChildDateOfBirthPage.type] =
    Arbitrary(EldestChildDateOfBirthPage)

  implicit lazy val arbitraryCurrentlyReceivingChildBenefitPage: Arbitrary[CurrentlyReceivingChildBenefitPage.type] =
    Arbitrary(CurrentlyReceivingChildBenefitPage)

  implicit lazy val arbitraryBankAccountDetailsPage: Arbitrary[BankAccountDetailsPage.type] =
    Arbitrary(BankAccountDetailsPage)

  implicit lazy val arbitraryApplicantHasSuitableAccountPage: Arbitrary[ApplicantHasSuitableAccountPage.type] =
    Arbitrary(ApplicantHasSuitableAccountPage)

  implicit lazy val arbitraryApplicantOrPartnerBenefitsPage: Arbitrary[ApplicantOrPartnerBenefitsPage.type] =
    Arbitrary(ApplicantOrPartnerBenefitsPage)

  implicit lazy val arbitraryApplicantBenefitsPage: Arbitrary[ApplicantBenefitsPage.type] =
    Arbitrary(ApplicantBenefitsPage)

  implicit lazy val arbitraryRelationshipStatusPage: Arbitrary[RelationshipStatusPage.type] =
    Arbitrary(RelationshipStatusPage)

  implicit lazy val arbitraryRelationshipStatusDatePage: Arbitrary[CohabitationDatePage.type] =
    Arbitrary(CohabitationDatePage)

  implicit lazy val arbitraryLivedOrWorkedAbroadPage: Arbitrary[LivedOrWorkedAbroadPage.type] =
    Arbitrary(LivedOrWorkedAbroadPage)

  implicit lazy val arbitraryAnyChildLivedWithOthersPage: Arbitrary[AnyChildLivedWithOthersPage.type] =
    Arbitrary(AnyChildLivedWithOthersPage)
}
