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

package pages.child

import controllers.child.routes
import models.AnyoneClaimedForChildBefore.{Applicant, No, Partner, SomeoneElse}
import models.{AnyoneClaimedForChildBefore, Index, UserAnswers}
import pages.{NonEmptyWaypoints, Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

final case class AnyoneClaimedForChildBeforePage(index: Index) extends ChildQuestionPage[AnyoneClaimedForChildBefore] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "anyoneClaimedForChildBefore"

  override def route(waypoints: Waypoints): Call =
    routes.AnyoneClaimedForChildBeforeController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Applicant | Partner | No =>
        AdoptingChildPage(index)

      case SomeoneElse =>
        PreviousClaimantNamePage(index)
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case Applicant | Partner | No =>
        waypoints.next.page

      case SomeoneElse =>
        answers.get(PreviousClaimantNamePage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(PreviousClaimantNamePage(index))
    }.orRecover

  override def cleanup(value: Option[AnyoneClaimedForChildBefore], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case Applicant | Partner | No =>
        userAnswers
          .remove(PreviousClaimantNamePage(index))
        .flatMap(_.remove(PreviousClaimantAddressPage(index)))

      case SomeoneElse =>
        super.cleanup(value, userAnswers)
    }.getOrElse {
      super.cleanup(value, userAnswers)
    }
}
