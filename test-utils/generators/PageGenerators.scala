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
import pages._

trait PageGenerators {

  implicit lazy val arbitraryBestTimeToContactPage: Arbitrary[BestTimeToContactPage.type] =
    Arbitrary(BestTimeToContactPage)

  implicit lazy val arbitraryApplicantPreviousFamilyNamePage: Arbitrary[ApplicantPreviousFamilyNamePage] =
    Arbitrary(ApplicantPreviousFamilyNamePage(Index(0)))

  implicit lazy val arbitraryApplicantPreviousAddressPage: Arbitrary[ApplicantPreviousAddressPage.type] =
    Arbitrary(ApplicantPreviousAddressPage)

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

  implicit lazy val arbitraryApplicantCurrentAddressPage: Arbitrary[ApplicantCurrentAddressPage.type] =
    Arbitrary(ApplicantCurrentAddressPage)

  implicit lazy val arbitraryAddApplicantPreviousFamilyNamePage: Arbitrary[AddApplicantPreviousFamilyNamePage.type] =
    Arbitrary(AddApplicantPreviousFamilyNamePage)

  implicit lazy val arbitraryWantToBePaidWeeklyPage: Arbitrary[WantToBePaidWeeklyPage.type] =
    Arbitrary(WantToBePaidWeeklyPage)

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

  implicit lazy val arbitraryCurrentlyEntitledToChildBenefitPage: Arbitrary[CurrentlyEntitledToChildBenefitPage.type] =
    Arbitrary(CurrentlyEntitledToChildBenefitPage)

  implicit lazy val arbitraryClaimedChildBenefitBeforePage: Arbitrary[ClaimedChildBenefitBeforePage.type] =
    Arbitrary(ClaimedChildBenefitBeforePage)

  implicit lazy val arbitraryBuildingSocietyAccountDetailsPage: Arbitrary[BuildingSocietyAccountDetailsPage.type] =
    Arbitrary(BuildingSocietyAccountDetailsPage)

  implicit lazy val arbitraryBankAccountTypePage: Arbitrary[BankAccountTypePage.type] =
    Arbitrary(BankAccountTypePage)

  implicit lazy val arbitraryBankAccountDetailsPage: Arbitrary[BankAccountDetailsPage.type] =
    Arbitrary(BankAccountDetailsPage)

  implicit lazy val arbitraryApplicantHasSuitableAccountPage: Arbitrary[ApplicantHasSuitableAccountPage.type] =
    Arbitrary(ApplicantHasSuitableAccountPage)

  implicit lazy val arbitraryAccountIsJointPage: Arbitrary[AccountIsJointPage.type] =
    Arbitrary(AccountIsJointPage)

  implicit lazy val arbitraryAccountInApplicantsNamePage: Arbitrary[AccountInApplicantsNamePage.type] =
    Arbitrary(AccountInApplicantsNamePage)

  implicit lazy val arbitraryAccountHolderNamesPage: Arbitrary[AccountHolderNamesPage.type] =
    Arbitrary(AccountHolderNamesPage)

  implicit lazy val arbitraryAccountHolderNamePage: Arbitrary[AccountHolderNamePage.type] =
    Arbitrary(AccountHolderNamePage)

  implicit lazy val arbitraryApplicantOrPartnerIncomeOver60kPage: Arbitrary[ApplicantOrPartnerIncomeOver60kPage.type] =
    Arbitrary(ApplicantOrPartnerIncomeOver60kPage)

  implicit lazy val arbitraryApplicantOrPartnerIncomeOver50kPage: Arbitrary[ApplicantOrPartnerIncomeOver50kPage.type] =
    Arbitrary(ApplicantOrPartnerIncomeOver50kPage)

  implicit lazy val arbitraryApplicantOrPartnerBenefitsPage: Arbitrary[ApplicantOrPartnerBenefitsPage.type] =
    Arbitrary(ApplicantOrPartnerBenefitsPage)

  implicit lazy val arbitraryApplicantIncomeOver60kPage: Arbitrary[ApplicantIncomeOver60kPage.type] =
    Arbitrary(ApplicantIncomeOver60kPage)

  implicit lazy val arbitraryApplicantIncomeOver50kPage: Arbitrary[ApplicantIncomeOver50kPage.type] =
    Arbitrary(ApplicantIncomeOver50kPage)

  implicit lazy val arbitraryApplicantBenefitsPage: Arbitrary[ApplicantBenefitsPage.type] =
    Arbitrary(ApplicantBenefitsPage)

  implicit lazy val arbitraryRelationshipStatusPage: Arbitrary[RelationshipStatusPage.type] =
    Arbitrary(RelationshipStatusPage)

  implicit lazy val arbitraryRelationshipStatusDatePage: Arbitrary[RelationshipStatusDatePage.type] =
    Arbitrary(RelationshipStatusDatePage)

  implicit lazy val arbitraryEverLivedOrWorkedAbroadPage: Arbitrary[EverLivedOrWorkedAbroadPage.type] =
    Arbitrary(EverLivedOrWorkedAbroadPage)

  implicit lazy val arbitraryAnyChildLivedWithOthersPage: Arbitrary[AnyChildLivedWithOthersPage.type] =
    Arbitrary(AnyChildLivedWithOthersPage)
}
