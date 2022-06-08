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
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

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
