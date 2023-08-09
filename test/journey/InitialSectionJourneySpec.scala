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

import config.FrontendAppConfig
import generators.ModelGenerators
import models.{ServiceType, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.mockito.MockitoSugar
import pages._
import pages.utils.ExternalPage
import uk.gov.hmrc.domain.Nino

class InitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators with TableDrivenPropertyChecks with MockitoSugar {
  private val defaultServiceType = ServiceType.values.head

  val mockAppConfig = mock[FrontendAppConfig]

  val journeyScenarios = Table(
    ("selectedServiceType", "userIsAuthenticated", "pageName", "expectedPage"),
    (ServiceType.NewClaim,            false, "Sign In",               SignInPage),
    (ServiceType.AddClaim,            false, "Sign In",               SignInPage),
    (ServiceType.CheckClaim,          false, "Already Claimed",       AlreadyClaimedPage),
    (ServiceType.StopChildBenefit,    false, "Stop Child Benefit",    ExternalPage("")),
    (ServiceType.RestartChildBenefit, false, "Restart Child Benefit", ExternalPage(""))
  )

  forAll (journeyScenarios) { (selectedServiceType, userIsAuthenticated, pageName, expectedPage) => {
    s"users who select the ${selectedServiceType.toString} and are ${if(userIsAuthenticated) "" else "not "}authenticated must go to the $pageName" in {
      val selectedUserAnswers = UserAnswers("id").set(RecentlyClaimedPage,selectedServiceType).get
      startingFrom(RecentlyClaimedPage)
        .run(
          submitAnswer[ServiceType, RecentlyClaimedPage.type](
            RecentlyClaimedPage,
            selectedServiceType,
            p => p.navigate(EmptyWaypoints, UserAnswers("id"), selectedUserAnswers, mockAppConfig)
          ),
          pageMustBe(expectedPage)
        )
    }
  }}

  "users who have recently claimed must go to the Already Claimed page" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, ServiceType.AddClaim),
        pageMustBe(AlreadyClaimedPage)
      )
  }

  "users who have not recently claimed" - {

    "who are already authenticated must go to the task list" in {

      val nino = arbitrary[Nino].sample.value
      val authenticatedAnswers = UserAnswers("id", nino = Some(nino.nino))

      startingFrom(RecentlyClaimedPage, answers = authenticatedAnswers)
        .run(
          submitAnswer(RecentlyClaimedPage, defaultServiceType),
          pageMustBe(TaskListPage)
        )
    }

    "who are not already authenticated must go to Sign In" in {

      startingFrom(RecentlyClaimedPage)
        .run(
          submitAnswer(RecentlyClaimedPage, defaultServiceType),
          pageMustBe(SignInPage)
        )
    }
  }
}
