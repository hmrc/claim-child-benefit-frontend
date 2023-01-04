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

package journey

import models.RelationshipStatus._
import models.{AdultName, RelationshipStatus}
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import pages.income.{ApplicantIncomePage, ApplicantOrPartnerIncomePage}
import pages._
import pages.applicant.ApplicantIsHmfOrCivilServantPage
import pages.partner.PartnerIsHmfOrCivilServantPage

import java.time.LocalDate

class InitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers {
  
  private val applicantName = AdultName(None, "first", None, "last")

  "eligible Married users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Married),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(ApplicantOrPartnerIncomePage)
      )
  }

  "eligible Cohabiting users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Cohabiting),
        submitAnswer(CohabitationDatePage, LocalDate.now.minusDays(1)),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(ApplicantOrPartnerIncomePage)
      )
  }

  "eligible Single users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Single),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "eligible Separated users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Separated),
        submitAnswer(SeparationDatePage, LocalDate.now.minusDays(1)),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "eligible Divorced users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Divorced),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "eligible Widowed users must continue to the income section" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, false),
        submitAnswer(ApplicantNamePage, applicantName),
        submitAnswer(RelationshipStatusPage, RelationshipStatus.Widowed),
        submitAnswer(AlwaysLivedInUkPage, true),
        pageMustBe(ApplicantIncomePage)
      )
  }

  "users who have recently claimed must go to the Already Claimed page" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, true),
        pageMustBe(AlreadyClaimedPage)
      )
  }

  "users who have not always lived in the UK" - {

    "who are HM Forces or a civil servant abroad" - {

      "and are Single, Divorced or Widowed" - {

        "must continue to the income section" in {

          val relationship = Gen.oneOf(Single, Divorced, Widowed).sample.value

          startingFrom(RecentlyClaimedPage)
            .run(
              submitAnswer(RecentlyClaimedPage, false),
              submitAnswer(ApplicantNamePage, applicantName),
              submitAnswer(RelationshipStatusPage, relationship),
              submitAnswer(AlwaysLivedInUkPage, false),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
              pageMustBe(ApplicantIncomePage)
            )
        }
      }

      "and are Separated" - {

        "must continue to the income section" in {

          startingFrom(RecentlyClaimedPage)
            .run(
              submitAnswer(RecentlyClaimedPage, false),
              submitAnswer(ApplicantNamePage, applicantName),
              submitAnswer(RelationshipStatusPage, Separated),
              submitAnswer(SeparationDatePage, LocalDate.now),
              submitAnswer(AlwaysLivedInUkPage, false),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
              pageMustBe(ApplicantIncomePage)
            )
        }
      }

      "and are Married" - {

        "must continue to the income section" in {

          startingFrom(RecentlyClaimedPage)
            .run(
              submitAnswer(RecentlyClaimedPage, false),
              submitAnswer(ApplicantNamePage, applicantName),
              submitAnswer(RelationshipStatusPage, Married),
              submitAnswer(AlwaysLivedInUkPage, false),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
              pageMustBe(ApplicantOrPartnerIncomePage)
            )
        }
      }

      "and are Cohabiting" - {

        "must continue to the income section" in {

          startingFrom(RecentlyClaimedPage)
            .run(
              submitAnswer(RecentlyClaimedPage, false),
              submitAnswer(ApplicantNamePage, applicantName),
              submitAnswer(RelationshipStatusPage, Cohabiting),
              submitAnswer(CohabitationDatePage, LocalDate.now),
              submitAnswer(AlwaysLivedInUkPage, false),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, true),
              pageMustBe(ApplicantOrPartnerIncomePage)
            )
        }
      }
    }

    "who are not HM Forces or a civil servant abroad" - {

      "and are Single, Divorced or Widowed" - {

        "must go to the Use Print and Post Form page" in {

          val relationshipStatus =
            Gen.oneOf(RelationshipStatus.Single, RelationshipStatus.Divorced, RelationshipStatus.Widowed).sample.value

          startingFrom(RecentlyClaimedPage)
            .run(
              submitAnswer(RecentlyClaimedPage, false),
              submitAnswer(ApplicantNamePage, applicantName),
              submitAnswer(RelationshipStatusPage, relationshipStatus),
              submitAnswer(AlwaysLivedInUkPage, false),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
              pageMustBe(UsePrintAndPostFormPage)
            )
        }
      }

      "and are Separated" - {

        "must go to the Use Print and Post Form page" in {

          startingFrom(RecentlyClaimedPage)
            .run(
              submitAnswer(RecentlyClaimedPage, false),
              submitAnswer(ApplicantNamePage, applicantName),
              submitAnswer(RelationshipStatusPage, RelationshipStatus.Separated),
              submitAnswer(SeparationDatePage, LocalDate.now),
              submitAnswer(AlwaysLivedInUkPage, false),
              submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
              pageMustBe(UsePrintAndPostFormPage)
            )
        }
      }

      "and are Married" - {

        "and their partner is HM Forces or a civil servant abroad" - {

          "must continue to the income section" in {

            startingFrom(RecentlyClaimedPage)
              .run(
                submitAnswer(RecentlyClaimedPage, false),
                submitAnswer(ApplicantNamePage, applicantName),
                submitAnswer(RelationshipStatusPage, RelationshipStatus.Married),
                submitAnswer(AlwaysLivedInUkPage, false),
                submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
                submitAnswer(PartnerIsHmfOrCivilServantPage, true),
                pageMustBe(ApplicantOrPartnerIncomePage)
              )
          }
        }

        "and their partner is not HM Forces or a civil servant abroad" - {

          "must go to the Use Print and Post Form page" in {

            startingFrom(RecentlyClaimedPage)
              .run(
                submitAnswer(RecentlyClaimedPage, false),
                submitAnswer(ApplicantNamePage, applicantName),
                submitAnswer(RelationshipStatusPage, RelationshipStatus.Married),
                submitAnswer(AlwaysLivedInUkPage, false),
                submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
                submitAnswer(PartnerIsHmfOrCivilServantPage, false),
                pageMustBe(UsePrintAndPostFormPage)
              )
          }
        }
      }

      "and are Cohabiting" - {

        "and their partner is HM Forces or a civil servant abroad" - {

          "must continue to the income section" in {

            startingFrom(RecentlyClaimedPage)
              .run(
                submitAnswer(RecentlyClaimedPage, false),
                submitAnswer(ApplicantNamePage, applicantName),
                submitAnswer(RelationshipStatusPage, RelationshipStatus.Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(AlwaysLivedInUkPage, false),
                submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
                submitAnswer(PartnerIsHmfOrCivilServantPage, true),
                pageMustBe(ApplicantOrPartnerIncomePage)
              )
          }
        }

        "and their partner is not HM Forces or a civil servant abroad" - {

          "must go to the Use Print and Post Form page" in {

            startingFrom(RecentlyClaimedPage)
              .run(
                submitAnswer(RecentlyClaimedPage, false),
                submitAnswer(ApplicantNamePage, applicantName),
                submitAnswer(RelationshipStatusPage, RelationshipStatus.Cohabiting),
                submitAnswer(CohabitationDatePage, LocalDate.now),
                submitAnswer(AlwaysLivedInUkPage, false),
                submitAnswer(ApplicantIsHmfOrCivilServantPage, false),
                submitAnswer(PartnerIsHmfOrCivilServantPage, false),
                pageMustBe(UsePrintAndPostFormPage)
              )
          }
        }
      }
    }
  }
}
