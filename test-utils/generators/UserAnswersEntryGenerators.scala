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

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.applicant._
import pages.child._
import pages.income._
import pages.partner._
import pages.payments._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryAdoptingChildUserAnswersEntry: Arbitrary[(AdoptingChildPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AdoptingChildPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviousClaimantNameUserAnswersEntry: Arbitrary[(PreviousClaimantNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousClaimantNamePage]
        value <- arbitrary[PreviousClaimantName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviousClaimantAddressUserAnswersEntry: Arbitrary[(PreviousClaimantAddressPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousClaimantAddressPage]
        value <- arbitrary[PreviousClaimantAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIncludedDocumentsUserAnswersEntry: Arbitrary[(IncludedDocumentsPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IncludedDocumentsPage]
        value <- arbitrary[IncludedDocuments].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildScottishBirthCertificateDetailsUserAnswersEntry: Arbitrary[(ChildScottishBirthCertificateDetailsPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildScottishBirthCertificateDetailsPage]
        value <- arbitrary[ChildScottishBirthCertificateDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildPreviousNameUserAnswersEntry: Arbitrary[(ChildPreviousNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildPreviousNamePage]
        value <- arbitrary[ChildName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildNameChangedByDeedPollUserAnswersEntry: Arbitrary[(ChildNameChangedByDeedPollPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildNameChangedByDeedPollPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildNameUserAnswersEntry: Arbitrary[(ChildNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildNamePage]
        value <- arbitrary[ChildName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildHasPreviousNameUserAnswersEntry: Arbitrary[(ChildHasPreviousNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildHasPreviousNamePage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildDateOfBirthUserAnswersEntry: Arbitrary[(ChildDateOfBirthPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildDateOfBirthPage]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildBirthRegistrationCountryUserAnswersEntry: Arbitrary[(ChildBirthRegistrationCountryPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildBirthRegistrationCountryPage]
        value <- arbitrary[ChildBirthRegistrationCountry].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildBirthCertificateSystemNumberUserAnswersEntry: Arbitrary[(ChildBirthCertificateSystemNumberPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildBirthCertificateSystemNumberPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildBiologicalSexUserAnswersEntry: Arbitrary[(ChildBiologicalSexPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildBiologicalSexPage]
        value <- arbitrary[ChildBiologicalSex].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantRelationshipToChildUserAnswersEntry: Arbitrary[(ApplicantRelationshipToChildPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantRelationshipToChildPage]
        value <- arbitrary[ApplicantRelationshipToChild].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAnyoneClaimedForChildBeforeUserAnswersEntry: Arbitrary[(AnyoneClaimedForChildBeforePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AnyoneClaimedForChildBeforePage]
        value <- arbitrary[AnyoneClaimedForChildBefore].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddChildPreviousNameUserAnswersEntry: Arbitrary[(AddChildPreviousNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddChildPreviousNamePage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddChildUserAnswersEntry: Arbitrary[(AddChildPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddChildPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerWaitingForEntitlementDecisionUserAnswersEntry: Arbitrary[(PartnerWaitingForEntitlementDecisionPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerWaitingForEntitlementDecisionPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerNinoKnownUserAnswersEntry: Arbitrary[(PartnerNinoKnownPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerNinoKnownPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerNinoUserAnswersEntry: Arbitrary[(PartnerNinoPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerNinoPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerNationalityUserAnswersEntry: Arbitrary[(PartnerNationalityPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerNationalityPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerNameUserAnswersEntry: Arbitrary[(PartnerNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerNamePage.type]
        value <- arbitrary[PartnerName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerEntitledToChildBenefitUserAnswersEntry: Arbitrary[(PartnerEntitledToChildBenefitPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerEntitledToChildBenefitPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerEmploymentStatusUserAnswersEntry: Arbitrary[(PartnerEmploymentStatusPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerEmploymentStatusPage.type]
        value <- arbitrary[PartnerEmploymentStatus].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerEldestChildNameUserAnswersEntry: Arbitrary[(PartnerEldestChildNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerEldestChildNamePage.type]
        value <- arbitrary[PartnerEldestChildName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerEldestChildDateOfBirthUserAnswersEntry: Arbitrary[(PartnerEldestChildDateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerEldestChildDateOfBirthPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartnerDateOfBirthUserAnswersEntry: Arbitrary[(PartnerDateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartnerDateOfBirthPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBestTimeToContactUserAnswersEntry: Arbitrary[(BestTimeToContactPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BestTimeToContactPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantPreviousFamilyNameUserAnswersEntry: Arbitrary[(ApplicantPreviousFamilyNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantPreviousFamilyNamePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantPreviousAddressUserAnswersEntry: Arbitrary[(ApplicantPreviousAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantPreviousAddressPage.type]
        value <- arbitrary[ApplicantPreviousAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantPhoneNumberUserAnswersEntry: Arbitrary[(ApplicantPhoneNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantPhoneNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantNinoKnownUserAnswersEntry: Arbitrary[(ApplicantNinoKnownPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantNinoKnownPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantNinoUserAnswersEntry: Arbitrary[(ApplicantNinoPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantNinoPage.type]
        value <- arbitrary[Nino].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantNationalityUserAnswersEntry: Arbitrary[(ApplicantNationalityPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantNationalityPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantNameUserAnswersEntry: Arbitrary[(ApplicantNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantNamePage.type]
        value <- arbitrary[ApplicantName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantLivedAtCurrentAddressOneYearUserAnswersEntry: Arbitrary[(ApplicantLivedAtCurrentAddressOneYearPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantLivedAtCurrentAddressOneYearPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantHasPreviousFamilyNameUserAnswersEntry: Arbitrary[(ApplicantHasPreviousFamilyNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantHasPreviousFamilyNamePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantEmploymentStatusUserAnswersEntry: Arbitrary[(ApplicantEmploymentStatusPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantEmploymentStatusPage.type]
        value <- arbitrary[ApplicantEmploymentStatus].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantDateOfBirthUserAnswersEntry: Arbitrary[(ApplicantDateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantDateOfBirthPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantCurrentAddressUserAnswersEntry: Arbitrary[(ApplicantCurrentAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantCurrentAddressPage.type]
        value <- arbitrary[ApplicantCurrentAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddApplicantPreviousFamilyNameUserAnswersEntry: Arbitrary[(AddApplicantPreviousFamilyNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddApplicantPreviousFamilyNamePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWantToBePaidWeeklyUserAnswersEntry: Arbitrary[(WantToBePaidWeeklyPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WantToBePaidWeeklyPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWantToBePaidToExistingAccountUserAnswersEntry: Arbitrary[(WantToBePaidToExistingAccountPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WantToBePaidToExistingAccountPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWantToBePaidUserAnswersEntry: Arbitrary[(WantToBePaidPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WantToBePaidPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEldestChildNameUserAnswersEntry: Arbitrary[(EldestChildNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EldestChildNamePage.type]
        value <- arbitrary[EldestChildName].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEldestChildDateOfBirthUserAnswersEntry: Arbitrary[(EldestChildDateOfBirthPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EldestChildDateOfBirthPage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCurrentlyReceivingChildBenefitUserAnswersEntry: Arbitrary[(CurrentlyReceivingChildBenefitPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CurrentlyReceivingChildBenefitPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCurrentlyEntitledToChildBenefitUserAnswersEntry: Arbitrary[(CurrentlyEntitledToChildBenefitPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CurrentlyEntitledToChildBenefitPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryClaimedChildBenefitBeforeUserAnswersEntry: Arbitrary[(ClaimedChildBenefitBeforePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ClaimedChildBenefitBeforePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBuildingSocietyAccountDetailsUserAnswersEntry: Arbitrary[(BuildingSocietyAccountDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BuildingSocietyAccountDetailsPage.type]
        value <- arbitrary[BuildingSocietyAccountDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBankAccountTypeUserAnswersEntry: Arbitrary[(BankAccountTypePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BankAccountTypePage.type]
        value <- arbitrary[BankAccountType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBankAccountDetailsUserAnswersEntry: Arbitrary[(BankAccountDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BankAccountDetailsPage.type]
        value <- arbitrary[BankAccountDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantHasSuitableAccountUserAnswersEntry: Arbitrary[(ApplicantHasSuitableAccountPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantHasSuitableAccountPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAccountIsJointUserAnswersEntry: Arbitrary[(AccountIsJointPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AccountIsJointPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAccountInApplicantsNameUserAnswersEntry: Arbitrary[(AccountInApplicantsNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AccountInApplicantsNamePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAccountHolderNamesUserAnswersEntry: Arbitrary[(AccountHolderNamesPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AccountHolderNamesPage.type]
        value <- arbitrary[AccountHolderNames].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAccountHolderNameUserAnswersEntry: Arbitrary[(AccountHolderNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AccountHolderNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantOrPartnerIncomeOver60kUserAnswersEntry: Arbitrary[(ApplicantOrPartnerIncomeOver60kPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantOrPartnerIncomeOver60kPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantOrPartnerIncomeOver50kUserAnswersEntry: Arbitrary[(ApplicantOrPartnerIncomeOver50kPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantOrPartnerIncomeOver50kPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantOrPartnerBenefitsUserAnswersEntry: Arbitrary[(ApplicantOrPartnerBenefitsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantOrPartnerBenefitsPage.type]
        value <- arbitrary[Benefits].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantIncomeOver60kUserAnswersEntry: Arbitrary[(ApplicantIncomeOver60kPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantIncomeOver60kPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantIncomeOver50kUserAnswersEntry: Arbitrary[(ApplicantIncomeOver50kPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantIncomeOver50kPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryApplicantBenefitsUserAnswersEntry: Arbitrary[(ApplicantBenefitsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ApplicantBenefitsPage.type]
        value <- arbitrary[Benefits].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRelationshipStatusUserAnswersEntry: Arbitrary[(RelationshipStatusPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RelationshipStatusPage.type]
        value <- arbitrary[RelationshipStatus].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRelationshipStatusDateUserAnswersEntry: Arbitrary[(RelationshipStatusDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RelationshipStatusDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEverLivedOrWorkedAbroadUserAnswersEntry: Arbitrary[(EverLivedOrWorkedAbroadPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EverLivedOrWorkedAbroadPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAnyChildLivedWithOthersUserAnswersEntry: Arbitrary[(AnyChildLivedWithOthersPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AnyChildLivedWithOthersPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }
}
