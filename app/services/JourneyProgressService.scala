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

import models.UserAnswers
import pages.{AddItemPage, CheckAnswersPage, EmptyWaypoints, Page, QuestionPage, Terminus}

import javax.inject.Singleton
import scala.annotation.tailrec

@Singleton
class JourneyProgressService {

  def continue(startFrom: Page, answers: UserAnswers): Page = {

    @tailrec
    def nextPage(currentPage: Page): Page = currentPage match {
      case t: Terminus =>
        t

      case c: CheckAnswersPage =>
        nextPage(c.navigate(EmptyWaypoints, answers, answers).page)

      case a: AddItemPage =>
        val answersWithYes = answers.set(a, true).getOrElse(throw new Exception(s"Unable to set ${a.path} to true"))
        val answersWithNo = answers.set(a, false).getOrElse(throw new Exception(s"Unable to set ${a.path} to false"))

        a.navigate(EmptyWaypoints, answersWithYes, answersWithYes).page match {
          case q: QuestionPage[_] =>
            if (answers.isDefined(q)) {
              nextPage(q)
            }
            else {
              nextPage(a.navigate(EmptyWaypoints, answersWithNo, answersWithNo).page)
            }

          case p: Page =>
            nextPage(p)
        }

      case q: QuestionPage[_] =>
        if (answers.isDefined(q)) {
          nextPage(q.navigate(EmptyWaypoints, answers, answers).page)
        }
        else {
          q
        }

      case p: Page =>
        nextPage(p.navigate(EmptyWaypoints, answers, answers).page)
    }

    nextPage(startFrom)
  }
}
