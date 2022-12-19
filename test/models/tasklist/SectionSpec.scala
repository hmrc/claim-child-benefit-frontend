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

package models.tasklist

import models.UserAnswers
import models.tasklist.SectionStatus.{CannotStart, Completed, InProgress, NotStarted}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.{Page, Waypoints}
import play.api.mvc.Call

class SectionSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  implicit def dontShrink[A]: Shrink[A] = Shrink.shrinkAny

  case class TestSection(progress: SectionStatus, prerequisites: Seq[Section], continue: Option[Page] = None) extends Section {
    override val name: String = "foo"
    override def continue(answers: UserAnswers): Option[Page] = continue
    override def progress(answers: UserAnswers): SectionStatus = progress
    override def prerequisiteSections(answers: UserAnswers): Set[Section] = prerequisites.toSet
  }

  private implicit val arbitraryProgress: Arbitrary[SectionStatus] = Arbitrary {
    Gen.oneOf(Completed, InProgress, NotStarted)
  }

  private implicit val arbitrarySection: Arbitrary[Section] = Arbitrary {
    arbitrary[SectionStatus].map(TestSection(_, Nil))
  }

  ".status" - {

    "must be Completed when this section's progress is Completed" in {

      val answers = UserAnswers("id")

      forAll(Gen.listOf(arbitrary[Section])) {
        prerequisites =>
          val section = TestSection(Completed, prerequisites)
          section.status(answers) mustEqual Completed
      }
    }

    "must be In Progress when this section's progress is In Progress" in {

      val answers = UserAnswers("id")

      forAll(Gen.listOf(arbitrary[Section])) {
        prerequisites =>
          val section = TestSection(InProgress, prerequisites)
          section.status(answers) mustEqual InProgress
      }
    }

    "must be Cannot Start when this section's progress is Cannot Start" in {

      val answers = UserAnswers("id")

      forAll(Gen.listOf(arbitrary[Section])) {
        prerequisites =>
          val section = TestSection(CannotStart, prerequisites)
          section.status(answers) mustEqual CannotStart
      }
    }

    "must be Not Started when this section's progress is Not Started and all prerequisite sections are Completed" in {

      val answers = UserAnswers("id")

      forAll(Gen.listOf(TestSection(Completed, Nil))) {
        prerequisites =>
          val section = TestSection(NotStarted, prerequisites)
          section.status(answers) mustEqual NotStarted
      }
    }

    "must be Cannot Start when this section's progress is Not Started and any prerequisite section is Not Started or In Progress" in {

      val answers = UserAnswers("id")

      val gen = for {
        incompleteSection <- Gen.oneOf(NotStarted, InProgress).map(TestSection(_, Nil))
        otherSections <- Gen.listOf(arbitrary[Section])
      } yield otherSections :+ incompleteSection

      forAll(gen) {
        prerequisites =>
          val section = TestSection(NotStarted, prerequisites)
          section.status(answers) mustEqual CannotStart
      }
    }
  }

  "asViewModel" - {

    "must return a view model with this section's continue URL and status" in {

      object TestPage extends Page {
        override def route(waypoints: Waypoints): Call = Call("", "")
      }

      val answers = UserAnswers("id")
      val section = TestSection(Completed, Nil, Some(TestPage))
      val result = section.asViewModel(answers)

      result mustEqual SectionViewModel("foo", Some(Call("", "")), Completed)
    }

    "must return a view model with this section's status and no URL" in {

      object TestPage extends Page {
        override def route(waypoints: Waypoints): Call = Call("", "")
      }

      val answers = UserAnswers("id")
      val section = TestSection(CannotStart, Nil, None)
      val result = section.asViewModel(answers)

      result mustEqual SectionViewModel("foo", None, CannotStart)
    }
  }
}
