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

import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.TryValues
import pages._
import pages.applicant._
import pages.child._
import pages.income._
import pages.partner._
import pages.payments._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersGenerator extends TryValues {
  self: Generators =>

  val generators: Seq[Gen[(QuestionPage[_], JsValue)]] =
    arbitrary[(PartnerIsHmfOrCivilServantPage.type, JsValue)] ::
    arbitrary[(ApplicantIsHmfOrCivilServantPage.type, JsValue)] ::
    arbitrary[(AdoptingThroughLocalAuthorityPage, JsValue)] ::
    arbitrary[(PreviousClaimantNamePage, JsValue)] ::
    arbitrary[(PreviousClaimantUkAddressPage, JsValue)] ::
    arbitrary[(ChildScottishBirthCertificateDetailsPage, JsValue)] ::
    arbitrary[(ChildPreviousNamePage, JsValue)] ::
    arbitrary[(ChildNameChangedByDeedPollPage, JsValue)] ::
    arbitrary[(ChildNamePage, JsValue)] ::
    arbitrary[(ChildHasPreviousNamePage, JsValue)] ::
    arbitrary[(ChildDateOfBirthPage, JsValue)] ::
    arbitrary[(ChildBirthRegistrationCountryPage, JsValue)] ::
    arbitrary[(ChildBirthCertificateSystemNumberPage, JsValue)] ::
    arbitrary[(ChildBiologicalSexPage, JsValue)] ::
    arbitrary[(ApplicantRelationshipToChildPage, JsValue)] ::
    arbitrary[(AnyoneClaimedForChildBeforePage, JsValue)] ::
    arbitrary[(AddChildPreviousNamePage, JsValue)] ::
    arbitrary[(AddChildPage.type, JsValue)] ::
    arbitrary[(PartnerNinoKnownPage.type, JsValue)] ::
    arbitrary[(PartnerNinoPage.type, JsValue)] ::
    arbitrary[(PartnerNationalityPage.type, JsValue)] ::
    arbitrary[(PartnerNamePage.type, JsValue)] ::
    arbitrary[(PartnerClaimingChildBenefitPage.type, JsValue)] ::
    arbitrary[(PartnerEmploymentStatusPage.type, JsValue)] ::
    arbitrary[(PartnerEldestChildNamePage.type, JsValue)] ::
    arbitrary[(PartnerEldestChildDateOfBirthPage.type, JsValue)] ::
    arbitrary[(PartnerDateOfBirthPage.type, JsValue)] ::
    arbitrary[(ApplicantPreviousFamilyNamePage, JsValue)] ::
    arbitrary[(ApplicantPreviousUkAddressPage.type, JsValue)] ::
    arbitrary[(ApplicantPhoneNumberPage.type, JsValue)] ::
    arbitrary[(ApplicantNinoKnownPage.type, JsValue)] ::
    arbitrary[(ApplicantNinoPage.type, JsValue)] ::
    arbitrary[(ApplicantNationalityPage.type, JsValue)] ::
    arbitrary[(ApplicantNamePage.type, JsValue)] ::
    arbitrary[(ApplicantLivedAtCurrentAddressOneYearPage.type, JsValue)] ::
    arbitrary[(ApplicantHasPreviousFamilyNamePage.type, JsValue)] ::
    arbitrary[(ApplicantEmploymentStatusPage.type, JsValue)] ::
    arbitrary[(ApplicantDateOfBirthPage.type, JsValue)] ::
    arbitrary[(ApplicantCurrentUkAddressPage.type, JsValue)] ::
    arbitrary[(AddApplicantPreviousFamilyNamePage.type, JsValue)] ::
    arbitrary[(PaymentFrequencyPage.type, JsValue)] ::
    arbitrary[(WantToBePaidToExistingAccountPage.type, JsValue)] ::
    arbitrary[(WantToBePaidPage.type, JsValue)] ::
    arbitrary[(EldestChildNamePage.type, JsValue)] ::
    arbitrary[(EldestChildDateOfBirthPage.type, JsValue)] ::
    arbitrary[(CurrentlyReceivingChildBenefitPage.type, JsValue)] ::
    arbitrary[(BankAccountDetailsPage.type, JsValue)] ::
    arbitrary[(ApplicantOrPartnerBenefitsPage.type, JsValue)] ::
    arbitrary[(ApplicantBenefitsPage.type, JsValue)] ::
    arbitrary[(RelationshipStatusPage.type, JsValue)] ::
    arbitrary[(CohabitationDatePage.type, JsValue)] ::
    arbitrary[(AlwaysLivedInUkPage.type, JsValue)] ::
    arbitrary[(AnyChildLivedWithOthersPage.type, JsValue)] ::
    Nil

  implicit lazy val arbitraryUserData: Arbitrary[UserAnswers] = {

    import models._

    Arbitrary {
      for {
        id      <- nonEmptyString
        data    <- generators match {
          case Nil => Gen.const(Map[QuestionPage[_], JsValue]())
          case _   => Gen.mapOf(oneOf(generators))
        }
      } yield UserAnswers (
        id = id,
        data = data.foldLeft(Json.obj()) {
          case (obj, (path, value)) =>
            obj.setObject(path.path, value).get
        }
      )
    }
  }
}
