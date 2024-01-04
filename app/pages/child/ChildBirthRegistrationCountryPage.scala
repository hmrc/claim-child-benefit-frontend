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
        BirthCertificateHasSystemNumberPage(index)

      case Scotland =>
        ScottishBirthCertificateHasNumbersPage(index)

      case NorthernIreland =>
        BirthCertificateHasNorthernIrishNumberPage(index)

      case OtherCountry | UnknownCountry =>
        AdoptingThroughLocalAuthorityPage(index)
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case England | Wales =>
        answers.get(BirthCertificateHasSystemNumberPage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(BirthCertificateHasSystemNumberPage(index))

      case Scotland =>
        answers.get(ScottishBirthCertificateHasNumbersPage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(ScottishBirthCertificateHasNumbersPage(index))

      case NorthernIreland =>
        answers.get(BirthCertificateHasNorthernIrishNumberPage(index))
          .map(_ => waypoints.next.page)
          .getOrElse(BirthCertificateHasNorthernIrishNumberPage(index))

      case OtherCountry | UnknownCountry =>
        waypoints.next.page
    }.orRecover

  override def cleanup(value: Option[ChildBirthRegistrationCountry], userAnswers: UserAnswers): Try[UserAnswers] = {
    value.map {
      case England | Wales =>
        userAnswers
          .remove(ScottishBirthCertificateHasNumbersPage(index))
          .flatMap(_.remove(ChildScottishBirthCertificateDetailsPage(index)))
          .flatMap(_.remove(BirthCertificateHasNorthernIrishNumberPage(index)))
          .flatMap(_.remove(ChildNorthernIrishBirthCertificateNumberPage(index)))

      case Scotland =>
        userAnswers
          .remove(BirthCertificateHasSystemNumberPage(index))
          .flatMap(_.remove(ChildBirthCertificateSystemNumberPage(index)))
          .flatMap(_.remove(BirthCertificateHasNorthernIrishNumberPage(index)))
          .flatMap(_.remove(ChildNorthernIrishBirthCertificateNumberPage(index)))

      case NorthernIreland =>
        userAnswers
          .remove(BirthCertificateHasSystemNumberPage(index))
          .flatMap(_.remove(ChildBirthCertificateSystemNumberPage(index)))
          .flatMap(_.remove(ScottishBirthCertificateHasNumbersPage(index)))
          .flatMap(_.remove(ChildScottishBirthCertificateDetailsPage(index)))

      case OtherCountry | UnknownCountry =>
        userAnswers
          .remove(BirthCertificateHasSystemNumberPage(index))
          .flatMap(_.remove(ChildBirthCertificateSystemNumberPage(index)))
          .flatMap(_.remove(ScottishBirthCertificateHasNumbersPage(index)))
          .flatMap(_.remove(ChildScottishBirthCertificateDetailsPage(index)))
          .flatMap(_.remove(BirthCertificateHasNorthernIrishNumberPage(index)))
          .flatMap(_.remove(ChildNorthernIrishBirthCertificateNumberPage(index)))

    }.getOrElse(super.cleanup(value, userAnswers))
  }
}
