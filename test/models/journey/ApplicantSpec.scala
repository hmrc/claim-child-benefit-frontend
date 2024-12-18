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

import cats.data.NonEmptyList
import generators.ModelGenerators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.applicant._
import queries.{AllApplicantNationalities, AllPreviousFamilyNames}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ApplicantSpec extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues {

  private val nino = arbitrary[Nino].sample.value
  private val adultName = arbitrary[AdultName].sample.value
  private val ukAddress = arbitrary[UkAddress].sample.value
  private val internationalAddress = arbitrary[InternationalAddress].sample.value
  private val previousName = ApplicantPreviousName(arbitrary[String].sample.value)
  private val index = Index(0)
  private val phoneNumber = "07777 777777"
  private val nationality = Gen.oneOf(Nationality.allNationalities).sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value
  private val npsAddress = arbitrary[NPSAddress].sample.value
  private val designatoryName = arbitrary[AdultName].sample.value
  private val fullDesignatoryDetails =
    DesignatoryDetails(Some(designatoryName), Some(designatoryName), Some(npsAddress), Some(npsAddress), LocalDate.now)
  private val minimalDesignatoryDetails =
    DesignatoryDetails(Some(designatoryName), None, Some(npsAddress), None, LocalDate.now)

  private def setResidenceToUkAndAbroad(answers: UserAnswers): UserAnswers =
    answers
      .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
      .success
      .value
      .set(ApplicantUsuallyLivesInUkPage, true)
      .success
      .value
      .set(ApplicantArrivedInUkPage, LocalDate.now)
      .success
      .value
      .set(ApplicantWorkedAbroadPage, false)
      .success
      .value
      .set(ApplicantReceivedBenefitsAbroadPage, false)
      .success
      .value
      .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
      .success
      .value

  private def setResidenceToAlwaysAbroad(answers: UserAnswers): UserAnswers =
    answers
      .set(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad)
      .success
      .value
      .set(ApplicantUsualCountryOfResidencePage, country)
      .success
      .value
      .set(ApplicantWorkedAbroadPage, false)
      .success
      .value
      .set(ApplicantReceivedBenefitsAbroadPage, false)
      .success
      .value
      .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
      .success
      .value

  ".build" - {

    "when the user is authenticated" - {

      val fullAnswers =
        UserAnswers("id", nino = Some(nino.value), designatoryDetails = Some(fullDesignatoryDetails))
          .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk)
          .success
          .value
          .set(ApplicantPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(ApplicantNationalityPage(index), nationality)
          .success
          .value
          .set(ApplicantIsHmfOrCivilServantPage, false)
          .success
          .value
          .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming)
          .success
          .value

      val minimalAnswers =
        UserAnswers("id", nino = Some(nino.value), designatoryDetails = Some(minimalDesignatoryDetails))
          .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk)
          .success
          .value
          .set(ApplicantPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(ApplicantNationalityPage(index), nationality)
          .success
          .value
          .set(ApplicantIsHmfOrCivilServantPage, false)
          .success
          .value
          .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming)
          .success
          .value

      "must return an Applicant (fullest case)" in {

        val (errors, data) = Applicant.build(fullAnswers).pad

        data.value `mustEqual` Applicant(
          name = designatoryName,
          previousFamilyNames = Nil,
          dateOfBirth = LocalDate.now,
          nationalInsuranceNumber = Some(nino.value),
          currentAddress = npsAddress,
          previousAddress = None,
          telephoneNumber = phoneNumber,
          nationalities = NonEmptyList(nationality, Nil),
          residency = Residency.AlwaysLivedInUk,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
          changedDesignatoryDetails = Some(false),
          correspondenceAddress = Some(npsAddress)
        )

        errors `must` `not` `be` defined
      }

      "must return an Applicant (sparsest case)" in {

        val (errors, data) = Applicant.build(minimalAnswers).pad

        data.value `mustEqual` Applicant(
          name = designatoryName,
          previousFamilyNames = Nil,
          dateOfBirth = LocalDate.now,
          nationalInsuranceNumber = Some(nino.value),
          currentAddress = npsAddress,
          previousAddress = None,
          telephoneNumber = phoneNumber,
          nationalities = NonEmptyList(nationality, Nil),
          residency = Residency.AlwaysLivedInUk,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
          changedDesignatoryDetails = Some(false),
          correspondenceAddress = None
        )

        errors `must` `not` `be` defined
      }

      "when the user has changed their designatory details" - {

        "must use the new name and address when they are UK addresses" in {

          val answers =
            fullAnswers
              .set(DesignatoryNamePage, adultName)
              .success
              .value
              .set(DesignatoryAddressInUkPage, true)
              .success
              .value
              .set(DesignatoryUkAddressPage, ukAddress)
              .success
              .value
              .set(CorrespondenceAddressInUkPage, true)
              .success
              .value
              .set(CorrespondenceUkAddressPage, ukAddress)
              .success
              .value
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming)
              .success
              .value

          val (errors, data) = Applicant.build(answers).pad

          data.value `mustEqual` Applicant(
            name = adultName,
            previousFamilyNames = Nil,
            dateOfBirth = LocalDate.now,
            nationalInsuranceNumber = Some(nino.value),
            currentAddress = ukAddress,
            previousAddress = None,
            telephoneNumber = phoneNumber,
            nationalities = NonEmptyList(nationality, Nil),
            residency = Residency.AlwaysLivedInUk,
            memberOfHMForcesOrCivilServantAbroad = false,
            currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
            changedDesignatoryDetails = Some(true),
            correspondenceAddress = Some(ukAddress)
          )

          errors `must` `not` `be` defined
        }

        "must use the new name and address when they are non-UK addresses" in {

          val answers =
            fullAnswers
              .set(DesignatoryNamePage, adultName)
              .success
              .value
              .set(DesignatoryAddressInUkPage, false)
              .success
              .value
              .set(DesignatoryInternationalAddressPage, internationalAddress)
              .success
              .value
              .set(CorrespondenceAddressInUkPage, false)
              .success
              .value
              .set(CorrespondenceInternationalAddressPage, internationalAddress)
              .success
              .value
              .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming)
              .success
              .value

          val (errors, data) = Applicant.build(answers).pad

          data.value `mustEqual` Applicant(
            name = adultName,
            previousFamilyNames = Nil,
            dateOfBirth = LocalDate.now,
            nationalInsuranceNumber = Some(nino.value),
            currentAddress = internationalAddress,
            previousAddress = None,
            telephoneNumber = phoneNumber,
            nationalities = NonEmptyList(nationality, Nil),
            residency = Residency.AlwaysLivedInUk,
            memberOfHMForcesOrCivilServantAbroad = false,
            currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
            changedDesignatoryDetails = Some(true),
            correspondenceAddress = Some(internationalAddress)
          )

          errors `must` `not` `be` defined
        }

        "must mark designatory details as changed when only the name has changed" in {

          val answers = fullAnswers.set(DesignatoryNamePage, adultName).success.value

          val (errors, data) = Applicant.build(answers).pad

          data.value.changedDesignatoryDetails.value `mustEqual` true
          errors `must` `not` `be` defined
        }

        "must mark designatory details as changed when only the residential address has changed" in {

          val answers =
            fullAnswers
              .set(DesignatoryAddressInUkPage, false)
              .success
              .value
              .set(DesignatoryInternationalAddressPage, internationalAddress)
              .success
              .value

          val (errors, data) = Applicant.build(answers).pad

          data.value.changedDesignatoryDetails.value `mustEqual` true
          errors `must` `not` `be` defined
        }

        "must mark designatory details as changed when only the correspondence address has changed" in {

          val answers =
            fullAnswers
              .set(CorrespondenceAddressInUkPage, false)
              .success
              .value
              .set(CorrespondenceInternationalAddressPage, internationalAddress)
              .success
              .value

          val (errors, data) = Applicant.build(answers).pad

          data.value.changedDesignatoryDetails.value `mustEqual` true
          errors `must` `not` `be` defined
        }

        "must return errors when the user said the addresses were UK but addresses are not present" in {

          val answers =
            fullAnswers
              .set(DesignatoryNamePage, adultName)
              .success
              .value
              .set(DesignatoryAddressInUkPage, true)
              .success
              .value
              .set(CorrespondenceAddressInUkPage, true)
              .success
              .value

          val (errors, data) = Applicant.build(answers).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList `must` contain theSameElementsAs Seq(
            DesignatoryUkAddressPage,
            CorrespondenceUkAddressPage
          )
        }

        "must return errors when the user said the addresses were non-UK but addresses are not present" in {

          val answers =
            fullAnswers
              .set(DesignatoryNamePage, adultName)
              .success
              .value
              .set(DesignatoryAddressInUkPage, false)
              .success
              .value
              .set(CorrespondenceAddressInUkPage, false)
              .success
              .value

          val (errors, data) = Applicant.build(answers).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList must contain theSameElementsAs Seq(
            DesignatoryInternationalAddressPage,
            CorrespondenceInternationalAddressPage
          )
        }
      }
    }

    "when the user is unauthenticated" - {

      val fullAnswers =
        UserAnswers("id")
          .set(ApplicantNinoKnownPage, true)
          .success
          .value
          .set(ApplicantNinoPage, nino)
          .success
          .value
          .set(ApplicantNamePage, adultName)
          .success
          .value
          .set(ApplicantHasPreviousFamilyNamePage, true)
          .success
          .value
          .set(ApplicantPreviousFamilyNamePage(index), previousName)
          .success
          .value
          .set(ApplicantDateOfBirthPage, LocalDate.now)
          .success
          .value
          .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk)
          .success
          .value
          .set(ApplicantCurrentUkAddressPage, ukAddress)
          .success
          .value
          .set(ApplicantLivedAtCurrentAddressOneYearPage, false)
          .success
          .value
          .set(ApplicantPreviousUkAddressPage, ukAddress)
          .success
          .value
          .set(ApplicantPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(ApplicantNationalityPage(index), nationality)
          .success
          .value
          .set(ApplicantIsHmfOrCivilServantPage, false)
          .success
          .value
          .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming)
          .success
          .value

      val minimalAnswers =
        UserAnswers("id")
          .set(ApplicantNinoKnownPage, false)
          .success
          .value
          .set(ApplicantNamePage, adultName)
          .success
          .value
          .set(ApplicantHasPreviousFamilyNamePage, false)
          .success
          .value
          .set(ApplicantDateOfBirthPage, LocalDate.now)
          .success
          .value
          .set(ApplicantResidencePage, ApplicantResidence.AlwaysUk)
          .success
          .value
          .set(ApplicantCurrentUkAddressPage, ukAddress)
          .success
          .value
          .set(ApplicantLivedAtCurrentAddressOneYearPage, true)
          .success
          .value
          .set(ApplicantPhoneNumberPage, phoneNumber)
          .success
          .value
          .set(ApplicantNationalityPage(index), nationality)
          .success
          .value
          .set(ApplicantIsHmfOrCivilServantPage, false)
          .success
          .value
          .set(CurrentlyReceivingChildBenefitPage, CurrentlyReceivingChildBenefit.NotClaiming)
          .success
          .value

      "must return an Applicant (fullest case)" in {

        val (errors, data) = Applicant.build(fullAnswers).pad

        data.value `mustEqual` Applicant(
          name = adultName,
          previousFamilyNames = List(previousName),
          dateOfBirth = LocalDate.now,
          nationalInsuranceNumber = Some(nino.value),
          currentAddress = ukAddress,
          previousAddress = Some(ukAddress),
          telephoneNumber = phoneNumber,
          nationalities = NonEmptyList(nationality, Nil),
          residency = Residency.AlwaysLivedInUk,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
          changedDesignatoryDetails = None,
          correspondenceAddress = None
        )

        errors `must` `not` `be` defined
      }

      "must return an Applicant (sparsest case)" in {

        val (errors, data) = Applicant.build(minimalAnswers).pad

        data.value `mustEqual` Applicant(
          name = adultName,
          previousFamilyNames = Nil,
          dateOfBirth = LocalDate.now,
          nationalInsuranceNumber = None,
          currentAddress = ukAddress,
          previousAddress = None,
          telephoneNumber = phoneNumber,
          nationalities = NonEmptyList(nationality, Nil),
          residency = Residency.AlwaysLivedInUk,
          memberOfHMForcesOrCivilServantAbroad = false,
          currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
          changedDesignatoryDetails = None,
          correspondenceAddress = None
        )

        errors `must` `not` `be` defined
      }

      "must return errors" - {

        "when mandatory details are missing" in {

          val answers = UserAnswers("id")

          val (errors, data) = Applicant.build(answers).pad

          data `must` not `be` defined
          errors.value.toChain.toList.distinct must contain theSameElementsAs Seq(
            ApplicantNamePage,
            ApplicantHasPreviousFamilyNamePage,
            ApplicantDateOfBirthPage,
            ApplicantNinoKnownPage,
            ApplicantResidencePage,
            ApplicantLivedAtCurrentAddressOneYearPage,
            ApplicantPhoneNumberPage,
            AllApplicantNationalities,
            ApplicantIsHmfOrCivilServantPage,
            CurrentlyReceivingChildBenefitPage
          )
        }

        "when the user says they know their NINO but it is missing" in {

          val answers = fullAnswers.remove(ApplicantNinoPage).success.value

          val (errors, data) = Applicant.build(answers).pad

          data `must` not `be` defined
          errors.value.toChain.toList `must` contain `only` ApplicantNinoPage
        }

        "when the user says they have a previous address" - {

          "and where they have lived is missing" in {

            val answers = fullAnswers.remove(ApplicantResidencePage).success.value

            val (errors, data) = Applicant.build(answers).pad

            data `must` not `be` defined
            errors.value.toChain.toList `must` contain `only` ApplicantResidencePage
          }

          "and they have always lived in the UK but their previous address is missing" in {

            val answers = fullAnswers.remove(ApplicantPreviousUkAddressPage).success.value

            val (errors, data) = Applicant.build(answers).pad

            data `must` not `be` defined
            errors.value.toChain.toList `must` contain `only` ApplicantPreviousUkAddressPage
          }

          "and they have lived in the UK and abroad" - {

            "and whether their previous address is in the UK is missing" in {

              val answers =
                setResidenceToUkAndAbroad(fullAnswers)
                  .set(ApplicantCurrentAddressInUkPage, true)
                  .success
                  .value
                  .set(ApplicantCurrentUkAddressPage, ukAddress)
                  .success
                  .value
                  .set(ApplicantLivedAtCurrentAddressOneYearPage, false)
                  .success
                  .value
                  .remove(ApplicantPreviousAddressInUkPage)
                  .success
                  .value

              val (errors, data) = Applicant.build(answers).pad

              data `must` not `be` defined
              errors.value.toChain.toList `must` contain `only` ApplicantPreviousAddressInUkPage
            }

            "and they say their previous address is in the UK, but it is missing" in {

              val answers =
                setResidenceToUkAndAbroad(fullAnswers)
                  .set(ApplicantCurrentAddressInUkPage, true)
                  .success
                  .value
                  .set(ApplicantCurrentUkAddressPage, ukAddress)
                  .success
                  .value
                  .set(ApplicantLivedAtCurrentAddressOneYearPage, false)
                  .success
                  .value
                  .set(ApplicantPreviousAddressInUkPage, true)
                  .success
                  .value
                  .remove(ApplicantPreviousUkAddressPage)
                  .success
                  .value

              val (errors, data) = Applicant.build(answers).pad

              data `must` not `be` defined
              errors.value.toChain.toList `must` contain `only` ApplicantPreviousUkAddressPage
            }

            "and they say their previous address is notin the UK, but it is missing" in {

              val answers =
                setResidenceToUkAndAbroad(fullAnswers)
                  .set(ApplicantCurrentAddressInUkPage, true)
                  .success
                  .value
                  .set(ApplicantCurrentUkAddressPage, ukAddress)
                  .success
                  .value
                  .set(ApplicantLivedAtCurrentAddressOneYearPage, false)
                  .success
                  .value
                  .set(ApplicantPreviousAddressInUkPage, false)
                  .success
                  .value
                  .remove(ApplicantPreviousInternationalAddressPage)
                  .success
                  .value

              val (errors, data) = Applicant.build(answers).pad

              data `must` not `be` defined
              errors.value.toChain.toList `must` contain `only` ApplicantPreviousInternationalAddressPage
            }
          }

          "and they have always lived abroad but their previous address is missing" in {

            val answers =
              setResidenceToAlwaysAbroad(fullAnswers)
                .set(ApplicantCurrentInternationalAddressPage, internationalAddress)
                .success
                .value
                .set(ApplicantLivedAtCurrentAddressOneYearPage, false)
                .success
                .value

            val (errors, data) = Applicant.build(answers).pad

            data `must` not `be` defined
            errors.value.toChain.toList `must` contain `only` ApplicantPreviousInternationalAddressPage
          }
        }

        "when the user has always lived in the UK but their address is missing" in {

          val answers = fullAnswers.remove(ApplicantCurrentUkAddressPage).success.value

          val (errors, data) = Applicant.build(answers).pad

          data `must` not `be` defined
          errors.value.toChain.toList `must` contain `only` ApplicantCurrentUkAddressPage
        }

        "when the user has lived in the UK and abroad" - {

          "and whether their address is in the UK is missing" in {

            val answers =
              setResidenceToUkAndAbroad(fullAnswers)

            val (errors, data) = Applicant.build(answers).pad

            data `must` not `be` defined
            errors.value.toChain.toList must contain(ApplicantCurrentAddressInUkPage)
          }

          "and they say their address is in the UK, but it is missing" in {

            val answers =
              setResidenceToUkAndAbroad(fullAnswers)
                .set(ApplicantCurrentAddressInUkPage, true)
                .success
                .value

            val (errors, data) = Applicant.build(answers).pad

            data `must` not `be` defined
            errors.value.toChain.toList must contain(ApplicantCurrentUkAddressPage)
          }

          "and they say their address is notin the UK, but it is missing" in {

            val answers =
              setResidenceToUkAndAbroad(fullAnswers)
                .set(ApplicantCurrentAddressInUkPage, false)
                .success
                .value

            val (errors, data) = Applicant.build(answers).pad

            data `must` not `be` defined
            errors.value.toChain.toList must contain(ApplicantCurrentInternationalAddressPage)
          }
        }

        "when the user has always lived abroad but their address is missing" in {

          val answers =
            setResidenceToAlwaysAbroad(fullAnswers)
              .remove(ApplicantCurrentInternationalAddressPage)
              .success
              .value

          val (errors, data) = Applicant.build(answers).pad

          data `must` not `be` defined
          errors.value.toChain.toList must contain(ApplicantCurrentInternationalAddressPage)
        }

        "when the user says they have previous names, but they are missing" in {

          val answers = fullAnswers.remove(AllPreviousFamilyNames).success.value

          val (errors, data) = Applicant.build(answers).pad

          data `must` not `be` defined
          errors.value.toChain.toList must contain(AllPreviousFamilyNames)
        }
      }
    }
  }
}
