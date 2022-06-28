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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.JourneyState.JourneyStep
import play.api.libs.json.{Reads, Writes}
import queries.{Gettable, Settable}

trait JourneyHelpers extends AnyFreeSpec with Matchers with TryValues with OptionValues {

  def next: JourneyStep[Unit] =
    State.modify(_.next)

  def next(times: Int): JourneyStep[Unit] =
    (0 until times).foldLeft(State.pure[JourneyState, Unit](())) { (m, _) => m >> next }

  def getPage: JourneyStep[Page] =
    State.inspect(_.page)

  def getWaypoints: JourneyStep[Waypoints] =
    State.inspect(_.waypoints)

  def getAnswers: JourneyStep[UserAnswers] =
    State.inspect(_.answers)

  def answer[A](page: Page with Settable[A], answer: A)(implicit writes: Writes[A]): JourneyStep[Unit] =
    State.modify { journeyState =>
      journeyState.copy(answers = journeyState.answers.set(page, answer).success.value)
    }

  def remove[A](page: Page with Settable[A]): JourneyStep[Unit] =
    State.modify { journeyState =>
      journeyState.copy(answers = journeyState.answers.remove(page).success.value)
    }

  def pageMustBe(expectedPage: Page): JourneyStep[Unit] =
    getPage.map { page =>
      page mustEqual expectedPage
    }

  def waypointsMustBe(expectedWaypoints: Waypoints): JourneyStep[Unit] =
    getWaypoints.map { waypoints =>
      waypoints mustEqual expectedWaypoints
    }

  def answersMustContain[A](gettable: Gettable[A])(implicit reads: Reads[A]): JourneyStep[Unit] =
    getAnswers.map { answers =>
      answers.get(gettable) mustBe defined
    }

  def answersMustNotContain[A](gettable: Gettable[A])(implicit reads: Reads[A]): JourneyStep[Unit] =
    getAnswers.map { answers =>
      answers.get(gettable) must not be defined
    }

  def answerPage[A](page: Page with Settable[A], value: A, expectedDestination: Page)(implicit writes: Writes[A]): JourneyStep[Unit] =
    answer(page, value) >> next >> pageMustBe(expectedDestination)

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
}
