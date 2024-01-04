/*
 * Copyright 2024 HM Revenue & Customs
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

package models.journey

import generators.ModelGenerators
import models.PartnerClaimingChildBenefit._
import models.{ChildName, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.applicant.{EldestChildDateOfBirthPage, EldestChildNamePage}
import pages.partner.{PartnerClaimingChildBenefitPage, PartnerEldestChildDateOfBirthPage, PartnerEldestChildNamePage}

import java.time.LocalDate

class EldestChildSpec extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues {

  private val childName = arbitrary[ChildName].sample.value

  ".buildPartnerEldestChild" - {

    "when the partner is claiming Child Benefit or waiting to hear if they are eligible" - {

      val claiming = Gen.oneOf(GettingPayments, NotGettingPayments, WaitingToHear).sample.value

      "must return an Eldest Child" in {

        val answers =
          UserAnswers("id")
            .set(PartnerClaimingChildBenefitPage, claiming).success.value
            .set(PartnerEldestChildNamePage, childName).success.value
            .set(PartnerEldestChildDateOfBirthPage, LocalDate.now).success.value

        val (errors, data) = EldestChild.buildPartnerEldestChild(answers).pad

        data.value.value mustEqual EldestChild(childName, LocalDate.now)
        errors must not be defined
      }

      "must return errors when any details are missing" in {

        val answers = UserAnswers("id").set(PartnerClaimingChildBenefitPage, claiming).success.value

        val (errors, data) = EldestChild.buildPartnerEldestChild(answers).pad

        data must not be defined
        errors.value.toChain.toList must contain theSameElementsAs Seq(
          PartnerEldestChildNamePage,
          PartnerEldestChildDateOfBirthPage
        )
      }
    }

    "when the partner is not claiming Child Benefit" - {

      "must return None" in {

        val answers = UserAnswers("id").set(PartnerClaimingChildBenefitPage, NotClaiming).success.value

        val (errors, data) = EldestChild.buildPartnerEldestChild(answers).pad

        data.value must not be defined
        errors must not be defined
      }
    }

    "must return an error when whether the partner is claiming Child Benefit is missing" in {

      val answers = UserAnswers("id")

      val (errors, data) = EldestChild.buildPartnerEldestChild(answers).pad

      data must not be defined
      errors.value.toChain.toList must contain only PartnerClaimingChildBenefitPage
    }
  }

  ".buildApplicantEldestChild" - {

    "must return an Eldest Child when all details have been answered" in {

      val answers =
        UserAnswers("id")
          .set(EldestChildNamePage, childName).success.value
          .set(EldestChildDateOfBirthPage, LocalDate.now).success.value

      val (errors, data) = EldestChild.buildApplicantEldestChild(answers).pad

      data.value mustEqual EldestChild(childName, LocalDate.now)
      errors must not be defined
    }

    "must return errors when any details are missing" in {

      val answers = UserAnswers("id")

      val (errors, data) = EldestChild.buildApplicantEldestChild(answers).pad

      data must not be defined
      errors.value.toChain.toList must contain theSameElementsAs Seq(
        EldestChildNamePage,
        EldestChildDateOfBirthPage
      )
    }
  }
}
