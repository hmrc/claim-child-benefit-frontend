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

import config.FeatureFlags
import generators.ModelGenerators
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages._
import uk.gov.hmrc.domain.Nino

class InitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators with MockitoSugar with BeforeAndAfterEach {

  private val mockFeatureFlags = mock[FeatureFlags]
  private val recentlyClaimedPage = new RecentlyClaimedPage(mockFeatureFlags)

  override def beforeEach(): Unit = {
    Mockito.reset(mockFeatureFlags)
    super.beforeEach()
  }

  "users who have recently claimed must go to the Already Claimed page" in {

    startingFrom(recentlyClaimedPage)
      .run(
        submitAnswer(recentlyClaimedPage, true),
        pageMustBe(AlreadyClaimedPage)
      )
  }

  "users who have not recently claimed" - {

    "when the sign in feature flag is disabled" - {

      when(mockFeatureFlags.showSignInPage) thenReturn false

      "must go to the task list" in {

        startingFrom(recentlyClaimedPage)
          .run(
            submitAnswer(recentlyClaimedPage, false),
            pageMustBe(TaskListPage)
          )
      }
    }

    "when the sign in feature flag is enabled" - {

      "who are already authenticated must go to the task list" in {

        when(mockFeatureFlags.showSignInPage) thenReturn true

        val nino = arbitrary[Nino].sample.value
        val authenticatedAnswers = UserAnswers("id", nino = Some(nino.nino))

        startingFrom(recentlyClaimedPage, answers = authenticatedAnswers)
          .run(
            submitAnswer(recentlyClaimedPage, false),
            pageMustBe(TaskListPage)
          )
      }

      "who are not already authenticated must go to Sign In" in {

        when(mockFeatureFlags.showSignInPage) thenReturn true

        startingFrom(recentlyClaimedPage)
          .run(
            submitAnswer(recentlyClaimedPage, false),
            pageMustBe(SignInPage)
          )
      }
    }
  }
}