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
import models.ChildBirthRegistrationCountry.{England, Other, Scotland, Unknown, Wales}
import models.{ChildBirthRegistrationCountry, Index, UserAnswers}
import pages.{Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

final case class ChildBirthRegistrationCountryPage(index: Index) extends ChildQuestionPage[ChildBirthRegistrationCountry] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "childBirthRegistrationCountry"

  override def route(waypoints: Waypoints): Call =
    routes.ChildBirthRegistrationCountryController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case England | Wales =>
        ChildBirthCertificateSystemNumberPage(index)

      case Scotland =>
        ChildScottishBirthCertificateDetailsPage(index)

      case Other | Unknown =>
        ApplicantRelationshipToChildPage(index)
    }.orRecover
}
