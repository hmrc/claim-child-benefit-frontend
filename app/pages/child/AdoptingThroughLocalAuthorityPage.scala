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
import models.ChildBirthRegistrationCountry._
import models.{Index, UserAnswers}
import pages.{NonEmptyWaypoints, Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

final case class AdoptingThroughLocalAuthorityPage(index: Index) extends ChildQuestionPage[Boolean] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "adoptingThroughLocalAuthority"

  override def route(waypoints: Waypoints): Call =
    routes.AdoptingThroughLocalAuthorityController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    AnyoneClaimedForChildBeforePage(index)

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(ChildBirthRegistrationCountryPage(index)).map {
      case England | Scotland | Wales =>
        answers.get(AnyoneClaimedForChildBeforePage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(AnyoneClaimedForChildBeforePage(index))

      case NorthernIreland | Other | Unknown =>
        answers.get(AnyoneClaimedForChildBeforePage(index))
          .map { _ =>
            answers.get(IncludedDocumentsPage(index)).map {
              case docs if docs.nonEmpty =>
                waypoints.next.page

              case _ =>
                IncludedDocumentsPage(index)
            }.orRecover
          }
          .getOrElse(AnyoneClaimedForChildBeforePage(index))
    }.orRecover
}
