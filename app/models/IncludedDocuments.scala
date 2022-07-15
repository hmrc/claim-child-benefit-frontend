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

package models

import models.ApplicantRelationshipToChild.AdoptedChild
import play.api.i18n.Messages
import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait IncludedDocuments {
  val name: String
}

object IncludedDocuments {

  case object BirthCertificate extends WithName("birthCertificate") with IncludedDocuments
  case object Passport extends WithName("passport") with IncludedDocuments
  case object TravelDocuments extends WithName("travelDocuments") with IncludedDocuments
  case object AdoptionCertificate extends WithName("adoptionCertificate") with IncludedDocuments

  case class OtherDocument(name: String) extends IncludedDocuments {
    override def toString: String = "otherDocument"
  }

  private val allStandardDocuments: Seq[IncludedDocuments] = Seq(
    BirthCertificate,
    Passport,
    TravelDocuments,
    AdoptionCertificate
  )

  def standardDocuments(relationshipToChild: ApplicantRelationshipToChild): Seq[IncludedDocuments] = {
    relationshipToChild match {
      case AdoptedChild =>
        allStandardDocuments

      case _ =>
        allStandardDocuments.filterNot(_ == AdoptionCertificate)
    }
  }

  implicit val reads: Reads[IncludedDocuments] = Reads {
    case JsString(BirthCertificate.toString)    => JsSuccess(BirthCertificate)
    case JsString(Passport.toString)            => JsSuccess(Passport)
    case JsString(TravelDocuments.toString)     => JsSuccess(TravelDocuments)
    case JsString(AdoptionCertificate.toString) => JsSuccess(AdoptionCertificate)
    case JsString(name)                         => JsSuccess(OtherDocument(name))
    case _                                      => JsError("error.invalid")
  }

  implicit val writes: Writes[IncludedDocuments] =
    Writes(value => JsString(value.name))
}
