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

import generators.ModelGenerators
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import pages._
import uk.gov.hmrc.domain.Nino

class InitialSectionJourneySpec extends AnyFreeSpec with JourneyHelpers with ModelGenerators {

  "users who have recently claimed must go to the Already Claimed page" in {

    startingFrom(RecentlyClaimedPage)
      .run(
        submitAnswer(RecentlyClaimedPage, true),
        pageMustBe(AlreadyClaimedPage)
      )
  }

  "users who have not recently claimed" - {

    "who are already authenticated must go to the task list" in {

      val nino = arbitrary[Nino].sample.value
      val authenticatedAnswers = UserAnswers("id", nino = Some(nino.nino))

      startingFrom(RecentlyClaimedPage, answers = authenticatedAnswers)
        .run(
          submitAnswer(RecentlyClaimedPage, false),
          pageMustBe(TaskListPage)
        )
    }

    "who are not already authenticated must go to Sign In" in {

      startingFrom(RecentlyClaimedPage)
        .run(
          submitAnswer(RecentlyClaimedPage, false),
          pageMustBe(SignInPage)
        )
    }
  }
}
