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

package pages

import cats.data.State
import cats.implicits._
import models.UserAnswers
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.JourneyState.JourneyStep
import play.api.libs.json.Writes
import queries.Settable

final case class JourneyState(page: Page, waypoints: Waypoints, answers: UserAnswers) {

  def next: JourneyState = {
    val PageAndWaypoints(nextPage, newWaypoints) = page.navigate(waypoints, answers)
    JourneyState(nextPage, newWaypoints, answers)
  }

  def steps(steps: JourneyStep[Unit]*): Unit =
    steps.fold(State.pure(())) { _ >> _ }.run(this).value
}

object JourneyState {

  type JourneyStep[A] = State[JourneyState, A]

  def startingFrom(page: Page): JourneyState =
    JourneyState(page, EmptyWaypoints, UserAnswers("id"))
}

class JourneySpec extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  def next: JourneyStep[Unit] =
    State.modify(_.next)

  def next(times: Int): JourneyStep[Unit] =
    (0 until times).foldLeft(State.pure[JourneyState, Unit](())) { (m, _) => m >> next }

  def getPage: JourneyStep[Page] =
    State.inspect(_.page)

  def getWaypoints: JourneyStep[Waypoints] =
    State.inspect(_.waypoints)

  def answer[A](page: Page with Settable[A], answer: A)(implicit writes: Writes[A]): JourneyStep[Unit] =
    State.modify { journeyState =>
      journeyState.copy(answers = journeyState.answers.set(page, answer).success.value)
    }

  def pageMustBe(expectedPage: Page): JourneyStep[Unit] =
    getPage.map { page =>
      page mustEqual expectedPage
    }

  def waypointsMustBe(expectedWaypoints: Waypoints): JourneyStep[Unit] =
    getWaypoints.map { waypoints =>
      waypoints mustEqual expectedWaypoints
    }

  def singlePageAssertion[A](page: Page with Settable[A], value: A, expectedPage: Page)(implicit writes: Writes[A]): JourneyStep[Unit] =
    answer(page, value) >> next >> pageMustBe(expectedPage)

  def goTo(page: Page): JourneyStep[Unit] =
    State.modify(_.copy(page = page))

  def goToCheckMode(page: Page, sourcePage: CheckAnswersPage): JourneyStep[Unit] =
    State.modify { journeyState =>
      val PageAndWaypoints(nextPage, waypoints) = page.changeLink(journeyState.waypoints, sourcePage)
      journeyState.copy(page = nextPage, waypoints = waypoints)
    }

  def goToCheckMode(page: Page): JourneyStep[Unit] =
    for {
      currentPage <- getPage
      _           <- goToCheckMode(page, currentPage.asInstanceOf[CheckAnswersPage])
    } yield ()

  "foo" in {

    JourneyState.startingFrom(EverLivedOrWorkedAbroadPage).steps(
      answer(EverLivedOrWorkedAbroadPage, false),
      next(1),
      pageMustBe(AnyChildLivedWithOthersPage)
    )
  }
}
