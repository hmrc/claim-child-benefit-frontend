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
import models.ApplicantRelationshipToChild.{AdoptedChild, AdoptingChild}
import models.ChildBirthRegistrationCountry._
import models.IncludedDocuments.AdoptionCertificate
import models.{ApplicantRelationshipToChild, Index, UserAnswers}
import pages.{NonEmptyWaypoints, Page, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

final case class ApplicantRelationshipToChildPage(index: Index) extends ChildQuestionPage[ApplicantRelationshipToChild] {

  override def path: JsPath = JsPath \ "children" \ index.position \ toString

  override def toString: String = "applicantRelationshipToChild"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantRelationshipToChildController.onPageLoad(waypoints, index)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    AdoptingThroughLocalAuthorityPage(index)

  override def cleanup(value: Option[ApplicantRelationshipToChild], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case AdoptedChild =>
        userAnswers.remove(AdoptingThroughLocalAuthorityPage(index))

      case AdoptingChild =>
        userAnswers.get(IncludedDocumentsPage(index)).map {
          documents =>
            userAnswers.set(IncludedDocumentsPage(index), documents - AdoptionCertificate)
        }.getOrElse(super.cleanup(value, userAnswers))

      case _ =>
        userAnswers.get(IncludedDocumentsPage(index)).map {
          documents =>
            userAnswers
              .set(IncludedDocumentsPage(index), documents - AdoptionCertificate)
              .flatMap(_.remove(AdoptingThroughLocalAuthorityPage(index)))
        }.getOrElse(userAnswers.remove(AdoptingThroughLocalAuthorityPage(index)))

    }.getOrElse(super.cleanup(value, userAnswers))
}
