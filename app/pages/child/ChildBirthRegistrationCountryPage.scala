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
import models.{ChildBirthRegistrationCountry, Index, UserAnswers}
import pages.{NonEmptyWaypoints, Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

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

      case NorthernIreland | Other | Unknown =>
        ApplicantRelationshipToChildPage(index)
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case England | Wales =>
        answers.get(ChildBirthCertificateSystemNumberPage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(ChildBirthCertificateSystemNumberPage(index))

      case Scotland =>
        answers.get(ChildScottishBirthCertificateDetailsPage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(ChildScottishBirthCertificateDetailsPage(index))

      case NorthernIreland | Other | Unknown =>
        answers.get(IncludedDocumentsPage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(IncludedDocumentsPage(index))
    }.orRecover

  override def cleanup(value: Option[ChildBirthRegistrationCountry], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case England | Wales =>
        userAnswers
          .remove(ChildScottishBirthCertificateDetailsPage(index))
          .flatMap(_.remove(IncludedDocumentsPage(index)))

      case Scotland =>
        userAnswers
          .remove(ChildBirthCertificateSystemNumberPage(index))
          .flatMap(_.remove(IncludedDocumentsPage(index)))

      case NorthernIreland | Other | Unknown =>
        userAnswers
          .remove(ChildBirthCertificateSystemNumberPage(index))
          .flatMap(_.remove(ChildScottishBirthCertificateDetailsPage(index)))

    }.getOrElse(super.cleanup(value, userAnswers))
  }
}
