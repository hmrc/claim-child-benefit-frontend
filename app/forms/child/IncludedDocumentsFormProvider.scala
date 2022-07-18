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

package forms.child

import forms.mappings.Mappings
import models.ApplicantRelationshipToChild.AdoptedChild
import models.IncludedDocuments._
import models.{ApplicantRelationshipToChild, ChildName, IncludedDocuments}
import play.api.data.Form
import play.api.data.Forms.{mapping, set}
import play.api.i18n.Messages
import uk.gov.voa.play.form.ConditionalMappings.mandatoryIf

import javax.inject.Inject

class IncludedDocumentsFormProvider @Inject() extends Mappings {

  def apply(childName: ChildName, relationshipToChild: ApplicantRelationshipToChild)(implicit messages: Messages): Form[Set[IncludedDocuments]] =
    Form(
      mapping(
        "value" -> set(
          text("includedDocuments.error.required", args = Seq(childName.safeFirstName))
            .verifying(messages("includedDocuments.error.required", childName.safeFirstName), validValues(relationshipToChild).contains(_))
          ).verifying(messages("includedDocuments.error.required", childName.safeFirstName), _.nonEmpty),
        "otherDocument" -> mandatoryIf(otherDocumentSelected, text("includedDocuments.error.otherDocument.required", args = Seq(childName.safeFirstName)))
      )(a)(u)
    )

  private def otherDocumentSelected: Map[String, String] => Boolean =
    _.values.toList.contains("otherDocument")

  private def a(values: Set[String], otherDoc: Option[String]): Set[IncludedDocuments] = {

    val standardDocuments = values.map(fromString)
    val otherDocument     = otherDoc.map(OtherDocument)

    (standardDocuments + otherDocument).flatten
  }

  private def u(docs: Set[IncludedDocuments]): Option[(Set[String], Option[String])] = {

    val values   = docs.map(_.toString)
    val otherDoc = docs.flatMap {
      case OtherDocument(name) => Some(name)
      case _                   => None
    }.headOption

    Some((values, otherDoc))
  }

  private def fromString(string: String): Option[IncludedDocuments] =
    string match {
      case BirthCertificate.toString    => Some(BirthCertificate)
      case Passport.toString            => Some(Passport)
      case TravelDocuments.toString     => Some(TravelDocuments)
      case AdoptionCertificate.toString => Some(AdoptionCertificate)
      case _                            => None
    }

  private def validValues(relationshipToChild: ApplicantRelationshipToChild): Set[String] = {
    val allValues = Set(
      BirthCertificate.toString,
      Passport.toString,
      TravelDocuments.toString,
      AdoptionCertificate.toString,
      "otherDocument"
    )

    if (relationshipToChild == AdoptedChild) allValues else allValues - AdoptionCertificate.toString
  }
}
