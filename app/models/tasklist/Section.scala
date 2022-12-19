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
import pages.Page

trait Section {

  def continue(answers: UserAnswers): Page

  def progress(answers: UserAnswers): SectionStatus

  def prerequisiteSections(answers: UserAnswers): Set[Section] =
    Set.empty

  def status(answers: UserAnswers): SectionStatus =
    progress(answers) match {
      case Completed => Completed
      case InProgress => InProgress
      case NotStarted if anyIncompletePrerequisites(answers) => CannotStart
      case _ => NotStarted
    }

  private def anyIncompletePrerequisites(answers: UserAnswers): Boolean =
    prerequisiteSections(answers).exists(_.progress(answers) != Completed)
}