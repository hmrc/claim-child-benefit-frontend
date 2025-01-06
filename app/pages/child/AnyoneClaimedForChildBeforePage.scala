/*
 * Copyright 2024 HM Revenue & Customs
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
import models.{Index, UserAnswers}
import pages.{Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import pages.RecoveryOps

import scala.util.Try

final case class AnyoneClaimedForChildBeforePage(index: Index) extends ChildQuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "anyoneClaimedForChildBefore"

  override def route(waypoints: Waypoints): Call =
    routes.AnyoneClaimedForChildBeforeController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true  => PreviousClaimantNameKnownPage(index)
      case false => ChildLivesWithApplicantPage(index)
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case true =>
        super.cleanup(value, userAnswers)

      case false =>
        userAnswers
          .remove(PreviousClaimantNameKnownPage(index))
          .flatMap(_.remove(PreviousClaimantNamePage(index)))
          .flatMap(_.remove(PreviousClaimantAddressKnownPage(index)))
          .flatMap(_.remove(PreviousClaimantAddressInUkPage(index)))
          .flatMap(_.remove(PreviousClaimantUkAddressPage(index)))
        .flatMap(_.remove(PreviousClaimantInternationalAddressPage(index)))
    }.getOrElse {
      super.cleanup(value, userAnswers)
    }
}
