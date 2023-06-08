/*
 * Copyright 2023 HM Revenue & Customs
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

package models.domain

import cats.data.NonEmptyList
import generators.Generators
import models._
import models.journey
import models.journey.JourneyModel
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import uk.gov.hmrc.domain.Nino

import java.time.LocalDate

class ClaimantSpec extends AnyFreeSpec with Matchers with Generators with OptionValues with ScalaCheckPropertyChecks {

  private val nationalitiesThatResolveToEea = for {
    eeaNationalities <- Gen.nonEmptyListOf(genEeaNationality)
    ukCtaNationalities <- Gen.listOf(genUkCtaNationality)
    nonEeaNationalities <- Gen.listOf(genNonEeaNationality)
  } yield NonEmptyList.fromListUnsafe(eeaNationalities ++ ukCtaNationalities ++ nonEeaNationalities)

  private val nationalitiesThatResolveToNonEea = for {
    nonEeaNationalities <- Gen.nonEmptyListOf(genNonEeaNationality)
    ukCtaNationalities  <- Gen.listOf(genUkCtaNationality)
  } yield NonEmptyList.fromListUnsafe(nonEeaNationalities ++ ukCtaNationalities)

  private val nationalitiesThatResolveToUkCta =
    Gen.nonEmptyListOf(genUkCtaNationality).map(NonEmptyList.fromListUnsafe)

  ".build" - {

    val hmfAbroad = arbitrary[Boolean].sample.value
    val nino = arbitrary[Nino].sample.value.nino

    def paymentPreference: journey.PaymentPreference = {
      Gen.oneOf(
        Gen.const(journey.PaymentPreference.Weekly(None, None)),
        Gen.const(journey.PaymentPreference.EveryFourWeeks(None, None)),
        Gen.const(journey.PaymentPreference.ExistingAccount(None))
      )
    }.sample.value

    def alwaysAbroad: journey.Residency.AlwaysLivedAbroad = {
      for {
        country <- arbitrary[Country]
        employment <- Gen.listOf(arbitrary[EmploymentStatus])
        countriesWorked <- Gen.listOf(arbitrary[Country])
        countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
      } yield journey.Residency.AlwaysLivedAbroad(country, employment.toSet, countriesWorked, countriesReceivedBenefits)
    }.sample.value

    val basicJourneyModel = JourneyModel(
      applicant = journey.Applicant(
        name = arbitrary[AdultName].sample.value,
        previousFamilyNames = Nil,
        dateOfBirth = LocalDate.now,
        nationalInsuranceNumber = None,
        currentAddress = arbitrary[models.UkAddress].sample.value,
        previousAddress = None, telephoneNumber = "0777777777",
        nationalities = nationalitiesThatResolveToUkCta.sample.value,
        residency = journey.Residency.AlwaysLivedInUk,
        memberOfHMForcesOrCivilServantAbroad = hmfAbroad,
        currentlyReceivingChildBenefit = CurrentlyReceivingChildBenefit.NotClaiming,
        changedDesignatoryDetails = Some(false),
        correspondenceAddress = None
      ),
      relationship = journey.Relationship(RelationshipStatus.Single, None, None),
      children = NonEmptyList(
        journey.Child(
          name = arbitrary[models.ChildName].sample.value,
          nameChangedByDeedPoll = None,
          previousNames = Nil,
          biologicalSex = Gen.oneOf(models.ChildBiologicalSex.values).sample.value,
          dateOfBirth = LocalDate.now,
          countryOfRegistration = Gen.oneOf(models.ChildBirthRegistrationCountry.values).sample.value,
          birthCertificateNumber = None,
          birthCertificateDetailsMatched = models.BirthRegistrationMatchingResult.NotAttempted,
          relationshipToApplicant = arbitrary[models.ApplicantRelationshipToChild].sample.value,
          adoptingThroughLocalAuthority = false,
          previousClaimant = None,
          guardian = None,
          previousGuardian = None,
          dateChildStartedLivingWithApplicant = None
        ),
        Nil
      ),
      benefits = None,
      paymentPreference = journey.PaymentPreference.DoNotPay(None),
      userAuthenticated = true,
      reasonsNotToSubmit = Nil,
      otherEligibilityFailureReasons = Nil
    )

    "must return a UK/CTA claimant who has always been resident in the UK" - {

      "when the claimant opts out of HICBC" in {

        val result = Claimant.build(nino, basicJourneyModel, None)

        result mustEqual UkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = hmfAbroad,
          hicbcOptOut = true
        )
      }

      "when the claimant does not opt out of HICBC" in {

        val journeyModel = basicJourneyModel.copy(paymentPreference = paymentPreference)

        val result = Claimant.build(nino, journeyModel, None)

        result mustEqual UkCtaClaimantAlwaysResident(
          nino = nino,
          hmfAbroad = hmfAbroad,
          hicbcOptOut = false
        )
      }
    }

    "must return a UK/CTA claimant who has not always been resident in the UK" - {

      "when the claimant has always lived abroad" - {

        "and opts out of HICBC" in {

          val journeyModel = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(residency = alwaysAbroad))

          val result = Claimant.build(nino, journeyModel, None)

          result mustEqual UkCtaClaimantNotAlwaysResident(
            nino = nino,
            hmfAbroad = hmfAbroad,
            last3MonthsInUK = false,
            hicbcOptOut = true
          )
        }

        "and does not opt out of HICBC" in {

          val journeyModel = basicJourneyModel.copy(
            applicant = basicJourneyModel.applicant.copy(residency = alwaysAbroad),
            paymentPreference = paymentPreference
          )
          val result = Claimant.build(nino, journeyModel, None)

          result mustEqual UkCtaClaimantNotAlwaysResident(
            nino = nino,
            hmfAbroad = hmfAbroad,
            last3MonthsInUK = false,
            hicbcOptOut = false
          )
        }
      }

      "when the claimant has lived in the UK and abroad" - {

        "and does not currently live in the UK" - {

          val residency = {
            for {
              country <- Gen.option(arbitrary[Country])
              employment <- Gen.listOf(arbitrary[EmploymentStatus])
              countriesWorked <- Gen.listOf(arbitrary[Country])
              countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
            } yield journey.Residency.LivedInUkAndAbroad(country, None, employment.toSet, countriesWorked, countriesReceivedBenefits)
          }.sample.value

          "and opts out of HICBC" in {

            val journeyModel = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(residency = residency))
            val result = Claimant.build(nino, journeyModel, None)

            result mustEqual UkCtaClaimantNotAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              last3MonthsInUK = false,
              hicbcOptOut = true
            )
          }

          "and does not opt out of HICBC" in {

            val journeyModel = basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(residency = residency),
              paymentPreference = paymentPreference
            )
            val result = Claimant.build(nino, journeyModel, None)

            result mustEqual UkCtaClaimantNotAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              last3MonthsInUK = false,
              hicbcOptOut = false
            )
          }
        }

        "and currently lives in the UK" - {

          "and arrived in the last 3 months" - {

            val residency = {
              for {
                country <- Gen.option(arbitrary[Country])
                arrival <- datesBetween(LocalDate.now.minusMonths(3), LocalDate.now)
                employment <- Gen.listOf(arbitrary[EmploymentStatus])
                countriesWorked <- Gen.listOf(arbitrary[Country])
                countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
              } yield journey.Residency.LivedInUkAndAbroad(country, Some(arrival), employment.toSet, countriesWorked, countriesReceivedBenefits)
            }.sample.value

            "and opts out of HICBC" in {

              val journeyModel = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(residency = residency))
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual UkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                last3MonthsInUK = false,
                hicbcOptOut = true
              )
            }

            "and does not opt out of HICBC" in {

              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(residency = residency),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual UkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                last3MonthsInUK = false,
                hicbcOptOut = false
              )
            }
          }

          "and arrived more than 3 months ago" - {

            val residency = {
              for {
                country <- Gen.option(arbitrary[Country])
                arrival <- datesBetween(LocalDate.now.minusYears(3), LocalDate.now.minusMonths(3).minusDays(1))
                employment <- Gen.listOf(arbitrary[EmploymentStatus])
                countriesWorked <- Gen.listOf(arbitrary[Country])
                countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
              } yield journey.Residency.LivedInUkAndAbroad(country, Some(arrival), employment.toSet, countriesWorked, countriesReceivedBenefits)
            }.sample.value

            "and opts out of HICBC" in {

              val journeyModel = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(residency = residency))
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual UkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                last3MonthsInUK = true,
                hicbcOptOut = true
              )
            }

            "and does not opt out of HICBC" in {

              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(residency = residency),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual UkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                last3MonthsInUK = true,
                hicbcOptOut = false
              )
            }
          }
        }
      }
    }

    "must return a non-UK/CTA claimant who has always been resident in the UK" - {

      "when the claimant is an EEA national" - {

        "and opts out of HICBC" in {

          forAll(nationalitiesThatResolveToEea) { nationalities =>
            val journeyModel = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(nationalities = nationalities))
            val result = Claimant.build(nino, journeyModel, None)

            result mustEqual NonUkCtaClaimantAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              hicbcOptOut = true,
              nationality = models.domain.Nationality.Eea,
              rightToReside = false
            )
          }
        }

        "and does not opt out of HICBC" in {

          forAll(nationalitiesThatResolveToEea) { nationalities =>
            val journeyModel = basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(nationalities = nationalities),
              paymentPreference = paymentPreference
            )
            val result = Claimant.build(nino, journeyModel, None)

            result mustEqual NonUkCtaClaimantAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              hicbcOptOut = false,
              nationality = models.domain.Nationality.Eea,
              rightToReside = false
            )
          }
        }

        "when they have settled status" in {

          forAll(nationalitiesThatResolveToEea) { nationalities =>
            val journeyModel = basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(nationalities = nationalities),
              paymentPreference = paymentPreference
            )
            val result = Claimant.build(nino, journeyModel, Some(true))

            result mustEqual NonUkCtaClaimantAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              hicbcOptOut = false,
              nationality = models.domain.Nationality.Eea,
              rightToReside = true
            )
          }
        }

        "when they do not have settled status" in {

          forAll(nationalitiesThatResolveToEea) { nationalities =>
            val journeyModel = basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(nationalities = nationalities),
              paymentPreference = paymentPreference
            )
            val result = Claimant.build(nino, journeyModel, Some(false))

            result mustEqual NonUkCtaClaimantAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              hicbcOptOut = false,
              nationality = models.domain.Nationality.Eea,
              rightToReside = false
            )
          }
        }
      }

      "when the claimant is a non-EEA national" - {

        "and opts out of HICBC" in {

          forAll(nationalitiesThatResolveToNonEea) { nationalities =>
            val journeyModel = basicJourneyModel.copy(applicant = basicJourneyModel.applicant.copy(nationalities = nationalities))
            val result = Claimant.build(nino, journeyModel, None)

            result mustEqual NonUkCtaClaimantAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              hicbcOptOut = true,
              nationality = models.domain.Nationality.NonEea,
              rightToReside = false
            )
          }
        }

        "and does not opt out of HICBC" in {

          forAll(nationalitiesThatResolveToNonEea) { nationalities =>
            val journeyModel = basicJourneyModel.copy(
              applicant = basicJourneyModel.applicant.copy(nationalities = nationalities),
              paymentPreference = paymentPreference
            )
            val result = Claimant.build(nino, journeyModel, None)

            result mustEqual NonUkCtaClaimantAlwaysResident(
              nino = nino,
              hmfAbroad = hmfAbroad,
              hicbcOptOut = false,
              nationality = models.domain.Nationality.NonEea,
              rightToReside = false
            )
          }
        }
      }
    }

    "must return a non-UK/CTA claimant who has not always been resident in the UK" - {

      "when the claimant has always lived abroad" - {

        "and is an EEA national" - {

          "and opts out of HICBC" in {

            forAll(nationalitiesThatResolveToEea) { nationalities =>

              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ))
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = true,
                nationality = models.domain.Nationality.Eea,
                rightToReside = false,
                last3MonthsInUK = false
              )
            }
          }

          "and does not opt out of HICBC" in {

            forAll(nationalitiesThatResolveToEea) { nationalities =>

              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = false,
                nationality = models.domain.Nationality.Eea,
                rightToReside = false,
                last3MonthsInUK = false
              )
            }
          }

          "and has settled status" in {

            forAll(nationalitiesThatResolveToEea) { nationalities =>
              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, Some(true))

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = false,
                nationality = models.domain.Nationality.Eea,
                rightToReside = true,
                last3MonthsInUK = false
              )
            }
          }

          "and does not have settled status" in {

            forAll(nationalitiesThatResolveToEea) { nationalities =>
              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, Some(false))

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = false,
                nationality = models.domain.Nationality.Eea,
                rightToReside = false,
                last3MonthsInUK = false
              )
            }
          }
        }

        "and is a non-EEA national" - {

          "and opts out of HICBC" in {

            forAll(nationalitiesThatResolveToNonEea) { nationalities =>

              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ))
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = true,
                nationality = models.domain.Nationality.NonEea,
                rightToReside = false,
                last3MonthsInUK = false
              )
            }
          }

          "and does not opt out of HICBC" in {

            forAll(nationalitiesThatResolveToNonEea) { nationalities =>

              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, None)

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = false,
                nationality = models.domain.Nationality.NonEea,
                rightToReside = false,
                last3MonthsInUK = false
              )
            }
          }

          "and has settled status" in {

            forAll(nationalitiesThatResolveToNonEea) { nationalities =>
              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, Some(true))

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = false,
                nationality = models.domain.Nationality.NonEea,
                rightToReside = true,
                last3MonthsInUK = false
              )
            }
          }

          "and does not have settled status" in {

            forAll(nationalitiesThatResolveToNonEea) { nationalities =>
              val journeyModel = basicJourneyModel.copy(
                applicant = basicJourneyModel.applicant.copy(
                  nationalities = nationalities,
                  residency = alwaysAbroad
                ),
                paymentPreference = paymentPreference
              )
              val result = Claimant.build(nino, journeyModel, Some(false))

              result mustEqual NonUkCtaClaimantNotAlwaysResident(
                nino = nino,
                hmfAbroad = hmfAbroad,
                hicbcOptOut = false,
                nationality = models.domain.Nationality.NonEea,
                rightToReside = false,
                last3MonthsInUK = false
              )
            }
          }
        }
      }

      "when the claimant has lived in the UK and abroad" - {

        "and is an EEA national" - {

          "add arrived in the last 3 months" - {

            val residency = {
              for {
                country <- Gen.option(arbitrary[Country])
                arrival <- datesBetween(LocalDate.now.minusMonths(3), LocalDate.now)
                employment <- Gen.listOf(arbitrary[EmploymentStatus])
                countriesWorked <- Gen.listOf(arbitrary[Country])
                countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
              } yield journey.Residency.LivedInUkAndAbroad(country, Some(arrival), employment.toSet, countriesWorked, countriesReceivedBenefits)
            }.sample.value

            "and opts out of HICBC" in {

              forAll(nationalitiesThatResolveToEea) { nationalities =>
                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ))
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = true,
                  nationality = models.domain.Nationality.Eea,
                  rightToReside = false,
                  last3MonthsInUK = false
                )
              }
            }

            "and does not opt out of HICBC" in {

              forAll(nationalitiesThatResolveToEea) { nationalities =>
                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ),
                  paymentPreference = paymentPreference
                )
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = false,
                  nationality = models.domain.Nationality.Eea,
                  rightToReside = false,
                  last3MonthsInUK = false
                )
              }
            }
          }

          "add arrived more than 3 months ago" - {

            val residency = {
              for {
                country <- Gen.option(arbitrary[Country])
                arrival <- datesBetween(LocalDate.now.minusYears(3), LocalDate.now.minusMonths(3).minusDays(1))
                employment <- Gen.listOf(arbitrary[EmploymentStatus])
                countriesWorked <- Gen.listOf(arbitrary[Country])
                countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
              } yield journey.Residency.LivedInUkAndAbroad(country, Some(arrival), employment.toSet, countriesWorked, countriesReceivedBenefits)
            }.sample.value

            "and opts out of HICBC" in {

              forAll(nationalitiesThatResolveToEea) { nationalities =>

                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ))
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = true,
                  nationality = models.domain.Nationality.Eea,
                  rightToReside = false,
                  last3MonthsInUK = true
                )
              }
            }

            "and does not opt out of HICBC" in {

              forAll(nationalitiesThatResolveToEea) { nationalities =>

                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ),
                  paymentPreference = paymentPreference
                )
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = false,
                  nationality = models.domain.Nationality.Eea,
                  rightToReside = false,
                  last3MonthsInUK = true
                )
              }
            }
          }
        }

        "and is a non-EEA national" - {

          "add arrived in the last 3 months" - {

            val residency = {
              for {
                country <- Gen.option(arbitrary[Country])
                arrival <- datesBetween(LocalDate.now.minusMonths(3), LocalDate.now)
                employment <- Gen.listOf(arbitrary[EmploymentStatus])
                countriesWorked <- Gen.listOf(arbitrary[Country])
                countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
              } yield journey.Residency.LivedInUkAndAbroad(country, Some(arrival), employment.toSet, countriesWorked, countriesReceivedBenefits)
            }.sample.value

            "and opts out of HICBC" in {

              forAll(nationalitiesThatResolveToNonEea) { nationalities =>

                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ))
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = true,
                  nationality = models.domain.Nationality.NonEea,
                  rightToReside = false,
                  last3MonthsInUK = false
                )
              }
            }

            "and does not opt out of HICBC" in {

              forAll(nationalitiesThatResolveToNonEea) { nationalities =>

                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ),
                  paymentPreference = paymentPreference
                )
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = false,
                  nationality = models.domain.Nationality.NonEea,
                  rightToReside = false,
                  last3MonthsInUK = false
                )
              }
            }
          }

          "add arrived more than 3 months ago" - {

            val residency = {
              for {
                country <- Gen.option(arbitrary[Country])
                arrival <- datesBetween(LocalDate.now.minusYears(3), LocalDate.now.minusMonths(3).minusDays(1))
                employment <- Gen.listOf(arbitrary[EmploymentStatus])
                countriesWorked <- Gen.listOf(arbitrary[Country])
                countriesReceivedBenefits <- Gen.listOf(arbitrary[Country])
              } yield journey.Residency.LivedInUkAndAbroad(country, Some(arrival), employment.toSet, countriesWorked, countriesReceivedBenefits)
            }.sample.value

            "and opts out of HICBC" in {

              forAll(nationalitiesThatResolveToNonEea) { nationalities =>

                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ))
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = true,
                  nationality = models.domain.Nationality.NonEea,
                  rightToReside = false,
                  last3MonthsInUK = true
                )
              }
            }

            "and does not opt out of HICBC" in {

              forAll(nationalitiesThatResolveToNonEea) { nationalities =>

                val journeyModel = basicJourneyModel.copy(
                  applicant = basicJourneyModel.applicant.copy(
                    nationalities = nationalities,
                    residency = residency
                  ),
                  paymentPreference = paymentPreference
                )
                val result = Claimant.build(nino, journeyModel, None)

                result mustEqual NonUkCtaClaimantNotAlwaysResident(
                  nino = nino,
                  hmfAbroad = hmfAbroad,
                  hicbcOptOut = false,
                  nationality = models.domain.Nationality.NonEea,
                  rightToReside = false,
                  last3MonthsInUK = true
                )
              }
            }
          }
        }
      }
    }
  }

  ".writes" - {

    "must write a UK/CTA claimant who has always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = UkCtaClaimantAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "UK_OR_CTA",
        "alwaysLivedInUK" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a UK/CTA claimant who has not always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = UkCtaClaimantNotAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        last3MonthsInUK = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "UK_OR_CTA",
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a non-UK/CTA claimant who has always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = NonUkCtaClaimantAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        nationality = models.domain.Nationality.Eea,
        rightToReside = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "EEA",
        "alwaysLivedInUK" -> true,
        "rightToReside" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }

    "must write a non-UK/CTA claimant who has not always been resident in the UK" in {

      val nino = arbitrary[Nino].sample.value.nino
      val claimant = NonUkCtaClaimantNotAlwaysResident(
        nino = nino,
        hmfAbroad = false,
        hicbcOptOut = true,
        last3MonthsInUK = true,
        nationality = models.domain.Nationality.Eea,
        rightToReside = true
      )

      val expectedJson = Json.obj(
        "nino" -> nino,
        "hmfAbroad" -> false,
        "hicbcOptOut" -> true,
        "nationality" -> "EEA",
        "alwaysLivedInUK" -> false,
        "last3MonthsInUK" -> true,
        "rightToReside" -> true
      )

      Json.toJson[Claimant](claimant) mustEqual expectedJson
    }
  }
}
