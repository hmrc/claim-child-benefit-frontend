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

package services

import models.{Index, UserAnswers}
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.{AddItemPage, CheckAnswersPage, Page, QuestionPage, Terminus, Waypoints}
import play.api.libs.json.{JsObject, JsPath}
import play.api.mvc.Call
import queries.Derivable

class JourneyProgressServiceSpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  case object QuestionPage1 extends QuestionPage[String] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "page1"
    override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = QuestionPage2
  }

  case object QuestionPage2 extends QuestionPage[String] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "page2"
    override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = BranchingPage3
  }

  case object BranchingPage3 extends QuestionPage[Boolean] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "page3"
    override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
      answers.get(this).map {
        case true => YesPage4
        case false => NoPage5
      }.orRecover
  }

  case object YesPage4 extends QuestionPage[String] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "page4"
    override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = Page6
  }

  case object NoPage5 extends QuestionPage[String] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "page5"
    override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = Page6
  }

  case object Page6 extends Page {
    override def route(waypoints: Waypoints): Call = ???
    override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = AddToListQuestionPage7(Index(0))
  }

  final case class AddToListQuestionPage7(index: Index) extends QuestionPage[String] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "addToList" \ index.position \ "page7"
    override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = AddToListQuestionPage8(index)
  }

  final case class AddToListQuestionPage8(index: Index) extends QuestionPage[String] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "addToList" \ index.position \ "page8"
    override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = CheckAddToListAnswersPage9(index)
  }

  final case class CheckAddToListAnswersPage9(index: Index) extends CheckAnswersPage {
    override val urlFragment: String = "foo"
    override def route(waypoints: Waypoints): Call = ???
    override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = AddAnotherPage10(Some(index))

    override def isTheSamePage(other: Page): Boolean = other match {
      case p: CheckAddToListAnswersPage9 => true
      case _ => false
    }
  }

  case object DeriveNumberOfItems extends Derivable[Seq[JsObject], Int] {
    override val derive: Seq[JsObject] => Int = _.size
    override def path: JsPath = JsPath \ "addToList"
  }

  final case class AddAnotherPage10(override val index: Option[Index] = None) extends AddItemPage(index) with QuestionPage[Boolean] {
    override val normalModeUrlFragment: String = "bar"
    override val checkModeUrlFragment: String = "baz"
    override def route(waypoints: Waypoints): Call = ???

    override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = {
      answers.get(this).map {
        case true =>
          index.map {
            x =>
              AddToListQuestionPage7(Index(x.position + 1))
          }.getOrElse {
            answers
              .get(DeriveNumberOfItems)
              .map(x => AddToListQuestionPage7(Index(x)))
              .getOrElse(AddToListQuestionPage7(Index(0)))
          }

        case false =>
          QuestionPage11
      }.orRecover
    }

    override def path: JsPath = JsPath \ "addItem"

    override def isTheSamePage(other: Page): Boolean = other match {
      case _: AddAnotherPage10 => true
      case _ => false
    }

    override def deriveNumberOfItems: Derivable[Seq[JsObject], Int] = DeriveNumberOfItems
  }

  case object QuestionPage11 extends QuestionPage[String] {
    override def route(waypoints: Waypoints): Call = ???
    override def path: JsPath = JsPath \ "page11"

    override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page = CheckAnswersPage12
  }

  case object CheckAnswersPage12 extends CheckAnswersPage with Terminus {
    override val urlFragment: String = "bax"
    override def route(waypoints: Waypoints): Call = ???

    override def isTheSamePage(other: Page): Boolean = other match {
      case CheckAnswersPage12 => true
      case _ => false
    }
  }

  private val emptyAnswers = UserAnswers("id")
  private val service = new JourneyProgressService

  ".continue" - {

    "must stop at an unanswered question page" in {

      val answers = emptyAnswers.set(QuestionPage1, "foo").success.value
      val result = service.continue(QuestionPage1, answers)
      result mustEqual QuestionPage2
    }

    "must follow branches" in {

      val answers =
        emptyAnswers
          .set(QuestionPage1, "foo").success.value
          .set(QuestionPage2, "bar").success.value
          .set(BranchingPage3, true).success.value

      val result = service.continue(QuestionPage1, answers)
      result mustEqual YesPage4
    }

    "must continue past a standard (non-question) page" in {

      val answers =
        emptyAnswers
          .set(QuestionPage1, "foo").success.value
          .set(QuestionPage2, "bar").success.value
          .set(BranchingPage3, true).success.value
          .set(YesPage4, "baz").success.value

      val result = service.continue(QuestionPage1, answers)
      result mustEqual AddToListQuestionPage7(Index(0))
    }

    "must stop at the first unanswered question in a partially-completed add-to-list item" in {

      val answers =
        emptyAnswers
          .set(AddToListQuestionPage7(Index(0)), "foo").success.value
          .set(AddToListQuestionPage8(Index(0)), "bar").success.value
          .set(AddToListQuestionPage7(Index(1)), "baz").success.value

      val result = service.continue(AddToListQuestionPage7(Index(0)), answers)
      result mustEqual AddToListQuestionPage8(Index(1))
    }

    "must continue from an add-item page as if the user answered no when all add-to-list items are complete" in {

      val answers =
        emptyAnswers
          .set(AddToListQuestionPage7(Index(0)), "foo").success.value
          .set(AddToListQuestionPage8(Index(0)), "bar").success.value

      val result = service.continue(AddToListQuestionPage7(Index(0)), answers)
      result mustEqual QuestionPage11
    }

    "must stop at a terminus" in {

      val answers =
        emptyAnswers
          .set(AddToListQuestionPage7(Index(0)), "foo").success.value
          .set(AddToListQuestionPage8(Index(0)), "bar").success.value
          .set(QuestionPage11, "baz").success.value

      val result = service.continue(AddToListQuestionPage7(Index(0)), answers)
      result mustEqual CheckAnswersPage12
    }
  }
}
