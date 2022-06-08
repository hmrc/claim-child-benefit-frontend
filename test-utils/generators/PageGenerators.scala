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

import org.scalacheck.Arbitrary
import pages._

trait PageGenerators {

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
