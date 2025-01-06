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
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.applicant._
import queries.{AllCountriesApplicantReceivedBenefits, AllCountriesApplicantWorked}
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ResidencySpec extends AnyFreeSpec with Matchers with ModelGenerators with TryValues with OptionValues {

  private val index = Index(0)
  private val nino = arbitrary[Nino].sample.value
  private val country = Gen.oneOf(Country.internationalCountries).sample.value
  private val ukDesignatoryDetails =
    DesignatoryDetails(None, None, Some(NPSAddress("line1", None, None, None, None, None, None)), None, LocalDate.now)
  private val intDesignatoryDetails = DesignatoryDetails(
    None,
    None,
    Some(NPSAddress("line1", None, None, None, None, None, Some(country))),
    None,
    LocalDate.now
  )

  ".build" - {

    "when the user has always lived in the UK" - {

      "must return Always Lived In UK" in {

        val answers = UserAnswers("id").set(ApplicantResidencePage, ApplicantResidence.AlwaysUk).success.value

        val (errors, data) = Residency.build(answers).pad

        data.value `mustEqual` Residency.AlwaysLivedInUk
        errors `must` `not` `be` defined
      }
    }

    "when the user has lived in the UK and abroad" - {

      "and is authenticated" - {

        "and changed their designatory address to a UK address" - {

          val baseAnswers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(ukDesignatoryDetails))

          "must return UK and Abroad when the user usually lives outside the UK and has worked and received benefits abroad (fullest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, false)
                .success
                .value
                .set(ApplicantUsualCountryOfResidencePage, country)
                .success
                .value
                .set(DesignatoryAddressInUkPage, true)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, true)
                .success
                .value
                .set(CountryApplicantWorkedPage(index), country)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, true)
                .success
                .value
                .set(CountryApplicantReceivedBenefitsPage(index), country)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = Some(country),
              arrivalDate = Some(LocalDate.now),
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = List(country),
              countriesReceivedBenefits = List(country)
            )
            errors `must` `not` `be` defined
          }

          "must return UK and Abroad when the user usually lives in the UK and has not worked or received benefits abroad (sparsest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, true)
                .success
                .value
                .set(DesignatoryAddressInUkPage, true)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, false)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, false)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = None,
              arrivalDate = Some(LocalDate.now),
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = Nil,
              countriesReceivedBenefits = Nil
            )
            errors `must` `not` `be` defined
          }

          "must return errors when the arrival date is missing" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, true)
                .success
                .value
                .set(DesignatoryAddressInUkPage, true)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, false)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, false)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data `must` `not` `be` defined
            errors.value.toChain.toList `must` contain `only` ApplicantArrivedInUkPage
          }
        }

        "and changed their designatory address to a non-UK address" - {

          val baseAnswers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(intDesignatoryDetails))

          "must return UK and Abroad when the user usually lives outside the UK and has worked and received benefits abroad (fullest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, false)
                .success
                .value
                .set(ApplicantUsualCountryOfResidencePage, country)
                .success
                .value
                .set(DesignatoryAddressInUkPage, false)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, true)
                .success
                .value
                .set(CountryApplicantWorkedPage(index), country)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, true)
                .success
                .value
                .set(CountryApplicantReceivedBenefitsPage(index), country)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = Some(country),
              arrivalDate = None,
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = List(country),
              countriesReceivedBenefits = List(country)
            )
            errors `must` `not` `be` defined
          }

          "must return UK and Abroad when the user usually lives in the UK and has not worked or received benefits abroad (sparsest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, true)
                .success
                .value
                .set(DesignatoryAddressInUkPage, false)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, false)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, false)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = None,
              arrivalDate = None,
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = Nil,
              countriesReceivedBenefits = Nil
            )
            errors `must` `not` `be` defined
          }
        }

        "and have a UK designatory address" - {

          val baseAnswers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(ukDesignatoryDetails))

          "must return UK and Abroad when the user usually lives outside the UK and has worked and received benefits abroad (fullest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, false)
                .success
                .value
                .set(ApplicantUsualCountryOfResidencePage, country)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, true)
                .success
                .value
                .set(CountryApplicantWorkedPage(index), country)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, true)
                .success
                .value
                .set(CountryApplicantReceivedBenefitsPage(index), country)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = Some(country),
              arrivalDate = Some(LocalDate.now),
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = List(country),
              countriesReceivedBenefits = List(country)
            )
            errors `must` `not` `be` defined
          }

          "must return UK and Abroad when the user usually lives in the UK and has not worked or received benefits abroad (sparsest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, true)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, false)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, false)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = None,
              arrivalDate = Some(LocalDate.now),
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = Nil,
              countriesReceivedBenefits = Nil
            )
            errors `must` `not` `be` defined
          }

          "must return errors when the arrival date is missing" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, true)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, false)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, false)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data `must` `not` `be` defined
            errors.value.toChain.toList `must` contain `only` ApplicantArrivedInUkPage
          }
        }

        "and have a non-UK designatory address" - {

          val baseAnswers = UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(intDesignatoryDetails))

          "must return UK and Abroad when the user usually lives outside the UK and has worked and received benefits abroad (fullest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, false)
                .success
                .value
                .set(ApplicantUsualCountryOfResidencePage, country)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, true)
                .success
                .value
                .set(CountryApplicantWorkedPage(index), country)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, true)
                .success
                .value
                .set(CountryApplicantReceivedBenefitsPage(index), country)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = Some(country),
              arrivalDate = None,
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = List(country),
              countriesReceivedBenefits = List(country)
            )
            errors `must` `not` `be` defined
          }

          "must return UK and Abroad when the user usually lives in the UK and has not worked or received benefits abroad (sparsest case)" in {

            val answers =
              baseAnswers
                .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
                .success
                .value
                .set(ApplicantUsuallyLivesInUkPage, true)
                .success
                .value
                .set(ApplicantArrivedInUkPage, LocalDate.now)
                .success
                .value
                .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
                .success
                .value
                .set(ApplicantWorkedAbroadPage, false)
                .success
                .value
                .set(ApplicantReceivedBenefitsAbroadPage, false)
                .success
                .value

            val (errors, data) = Residency.build(answers).pad

            data.value `mustEqual` Residency.LivedInUkAndAbroad(
              usualCountryOfResidence = None,
              arrivalDate = None,
              employmentStatus = EmploymentStatus.activeStatuses,
              countriesWorked = Nil,
              countriesReceivedBenefits = Nil
            )
            errors `must` `not` `be` defined
          }
        }

        "must return errors when the user say they worked abroad and received benefits abroad, but there are no countries" in {

          val answers =
            UserAnswers("id", nino = Some(nino.nino), designatoryDetails = Some(ukDesignatoryDetails))
              .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
              .success
              .value
              .set(ApplicantUsuallyLivesInUkPage, true)
              .success
              .value
              .set(ApplicantArrivedInUkPage, LocalDate.now)
              .success
              .value
              .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
              .success
              .value
              .set(ApplicantWorkedAbroadPage, true)
              .success
              .value
              .set(ApplicantReceivedBenefitsAbroadPage, true)
              .success
              .value

          val (errors, data) = Residency.build(answers).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList must contain theSameElementsAs Seq(
            AllCountriesApplicantWorked,
            AllCountriesApplicantReceivedBenefits
          )
        }
      }

      "and is unauthenticated" - {

        "must return UK and Abroad when the user usually lives outside the UK, currently lives in the UK, and has worked and received benefits abroad (fullest case)" in {

          val answers =
            UserAnswers("id")
              .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
              .success
              .value
              .set(ApplicantUsuallyLivesInUkPage, false)
              .success
              .value
              .set(ApplicantUsualCountryOfResidencePage, country)
              .success
              .value
              .set(ApplicantCurrentAddressInUkPage, true)
              .success
              .value
              .set(ApplicantArrivedInUkPage, LocalDate.now)
              .success
              .value
              .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
              .success
              .value
              .set(ApplicantWorkedAbroadPage, true)
              .success
              .value
              .set(CountryApplicantWorkedPage(index), country)
              .success
              .value
              .set(ApplicantReceivedBenefitsAbroadPage, true)
              .success
              .value
              .set(CountryApplicantReceivedBenefitsPage(index), country)
              .success
              .value

          val (errors, data) = Residency.build(answers).pad

          data.value `mustEqual` Residency.LivedInUkAndAbroad(
            usualCountryOfResidence = Some(country),
            arrivalDate = Some(LocalDate.now),
            employmentStatus = EmploymentStatus.activeStatuses,
            countriesWorked = List(country),
            countriesReceivedBenefits = List(country)
          )
          errors `must` `not` `be` defined
        }

        "must return UK and Abroad when the user usually lives in the UK, currently lives outside the UK, and has not worked or received benefits abroad (sparsest case)" in {

          val answers =
            UserAnswers("id")
              .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
              .success
              .value
              .set(ApplicantUsuallyLivesInUkPage, true)
              .success
              .value
              .set(ApplicantCurrentAddressInUkPage, false)
              .success
              .value
              .set(ApplicantArrivedInUkPage, LocalDate.now)
              .success
              .value
              .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
              .success
              .value
              .set(ApplicantWorkedAbroadPage, false)
              .success
              .value
              .set(ApplicantReceivedBenefitsAbroadPage, false)
              .success
              .value

          val (errors, data) = Residency.build(answers).pad

          data.value `mustEqual` Residency.LivedInUkAndAbroad(
            usualCountryOfResidence = None,
            arrivalDate = None,
            employmentStatus = EmploymentStatus.activeStatuses,
            countriesWorked = Nil,
            countriesReceivedBenefits = Nil
          )
          errors `must` `not` `be` defined
        }

        "must return errors when whether the user currently lives in the UK is missing" in {

          val answers =
            UserAnswers("id")
              .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
              .success
              .value
              .set(ApplicantUsuallyLivesInUkPage, true)
              .success
              .value
              .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
              .success
              .value
              .set(ApplicantWorkedAbroadPage, false)
              .success
              .value
              .set(ApplicantReceivedBenefitsAbroadPage, false)
              .success
              .value

          val (errors, data) = Residency.build(answers).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList `must` contain `only` ApplicantCurrentAddressInUkPage
        }

        "must return errors when the user say they worked and received benefits abroad, but there are no countries" in {

          val answers =
            UserAnswers("id")
              .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
              .success
              .value
              .set(ApplicantUsuallyLivesInUkPage, true)
              .success
              .value
              .set(ApplicantCurrentAddressInUkPage, true)
              .success
              .value
              .set(ApplicantArrivedInUkPage, LocalDate.now)
              .success
              .value
              .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
              .success
              .value
              .set(ApplicantWorkedAbroadPage, true)
              .success
              .value
              .set(ApplicantReceivedBenefitsAbroadPage, true)
              .success
              .value

          val (errors, data) = Residency.build(answers).pad

          data `must` `not` `be` defined
          errors.value.toChain.toList must contain theSameElementsAs Seq(
            AllCountriesApplicantWorked,
            AllCountriesApplicantReceivedBenefits
          )
        }
      }

      "must return errors when any details are missing" in {

        val answers = UserAnswers("id").set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad).success.value

        val (errors, data) = Residency.build(answers).pad

        data `must` `not` `be` defined
        errors.value.toChain.toList must contain theSameElementsAs Seq(
          ApplicantUsuallyLivesInUkPage,
          ApplicantCurrentAddressInUkPage,
          ApplicantEmploymentStatusPage,
          ApplicantWorkedAbroadPage,
          ApplicantReceivedBenefitsAbroadPage
        )
      }

      "must return errors when the user does not usually live in the UK, but there is no usual country of residence" in {

        val answers =
          UserAnswers("id")
            .set(ApplicantResidencePage, ApplicantResidence.UkAndAbroad)
            .success
            .value
            .set(ApplicantUsuallyLivesInUkPage, false)
            .success
            .value
            .set(ApplicantCurrentAddressInUkPage, true)
            .success
            .value
            .set(ApplicantArrivedInUkPage, LocalDate.now)
            .success
            .value
            .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
            .success
            .value
            .set(ApplicantWorkedAbroadPage, false)
            .success
            .value
            .set(ApplicantReceivedBenefitsAbroadPage, false)
            .success
            .value

        val (errors, data) = Residency.build(answers).pad

        data `must` `not` `be` defined
        errors.value.toChain.toList `must` contain `only` ApplicantUsualCountryOfResidencePage
      }
    }

    "when the user has always lived abroad" - {

      "must return Always Abroad when the user has worked and received benefits abroad (fullest case)" in {

        val answers =
          UserAnswers("id")
            .set(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad)
            .success
            .value
            .set(ApplicantUsualCountryOfResidencePage, country)
            .success
            .value
            .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
            .success
            .value
            .set(ApplicantWorkedAbroadPage, true)
            .success
            .value
            .set(CountryApplicantWorkedPage(index), country)
            .success
            .value
            .set(ApplicantReceivedBenefitsAbroadPage, true)
            .success
            .value
            .set(CountryApplicantReceivedBenefitsPage(index), country)
            .success
            .value

        val (errors, data) = Residency.build(answers).pad

        data.value `mustEqual` Residency.AlwaysLivedAbroad(
          usualCountryOfResidence = country,
          employmentStatus = EmploymentStatus.activeStatuses,
          countriesWorked = List(country),
          countriesReceivedBenefits = List(country)
        )
        errors `must` `not` `be` defined
      }

      "must return Always Abroad when the user has not worked or received benefits abroad (sparsest case)" in {

        val answers =
          UserAnswers("id")
            .set(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad)
            .success
            .value
            .set(ApplicantUsualCountryOfResidencePage, country)
            .success
            .value
            .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
            .success
            .value
            .set(ApplicantWorkedAbroadPage, false)
            .success
            .value
            .set(ApplicantReceivedBenefitsAbroadPage, false)
            .success
            .value

        val (errors, data) = Residency.build(answers).pad

        data.value `mustEqual` Residency.AlwaysLivedAbroad(
          usualCountryOfResidence = country,
          employmentStatus = EmploymentStatus.activeStatuses,
          countriesWorked = Nil,
          countriesReceivedBenefits = Nil
        )
        errors `must` `not` `be` defined
      }

      "must return errors when any details are missing" in {

        val answers =
          UserAnswers("id")
            .set(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad)
            .success
            .value

        val (errors, data) = Residency.build(answers).pad

        data `must` `not` `be` defined
        errors.value.toChain.toList must contain theSameElementsAs Seq(
          ApplicantUsualCountryOfResidencePage,
          ApplicantEmploymentStatusPage,
          ApplicantWorkedAbroadPage,
          ApplicantReceivedBenefitsAbroadPage
        )
      }

      "must return errors when the user say they worked and received benefits abroad, but there are no countries" in {

        val answers =
          UserAnswers("id")
            .set(ApplicantResidencePage, ApplicantResidence.AlwaysAbroad)
            .success
            .value
            .set(ApplicantUsualCountryOfResidencePage, country)
            .success
            .value
            .set(ApplicantEmploymentStatusPage, EmploymentStatus.activeStatuses)
            .success
            .value
            .set(ApplicantWorkedAbroadPage, true)
            .success
            .value
            .set(ApplicantReceivedBenefitsAbroadPage, true)
            .success
            .value

        val (errors, data) = Residency.build(answers).pad

        data `must` `not` `be` defined
        errors.value.toChain.toList must contain theSameElementsAs Seq(
          AllCountriesApplicantWorked,
          AllCountriesApplicantReceivedBenefits
        )
      }
    }

    "must return errors when where the user has lived is not answered" in {

      val answers = UserAnswers("id")

      val (errors, data) = Residency.build(answers).pad

      data `must` `not` `be` defined
      errors.value.toChain.toList `must` contain `only` ApplicantResidencePage
    }
  }
}
