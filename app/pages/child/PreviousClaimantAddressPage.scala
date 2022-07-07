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
import models.{Address, Index, UserAnswers}
import pages.{Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

final case class PreviousClaimantAddressPage(index: Index) extends ChildQuestionPage[Address] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "previousClaimantAddress"

  override def route(waypoints: Waypoints): Call =
    routes.PreviousClaimantAddressController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(ChildBirthRegistrationCountryPage(index)).map {
      case England | Wales | Scotland =>
        CheckChildDetailsPage(index)

      case Other | Unknown =>
        IncludedDocumentsPage(index)
    }.orRecover
}
