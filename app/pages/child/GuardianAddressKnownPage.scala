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

final case class GuardianAddressKnownPage(index: Index) extends ChildQuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "guardianAddressKnown"

  override def route(waypoints: Waypoints): Call =
    routes.GuardianAddressKnownController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true  => GuardianAddressInUkPage(index)
      case false => CheckChildDetailsPage(index)
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    if (value.contains(false)) {
      userAnswers
        .remove(GuardianAddressInUkPage(index))
        .flatMap(_.remove(GuardianUkAddressPage(index)))
        .flatMap(_.remove(GuardianInternationalAddressPage(index)))
    } else {
      super.cleanup(value, userAnswers)
    }
}
