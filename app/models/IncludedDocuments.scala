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
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.CheckboxItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.checkbox._

sealed trait IncludedDocuments

object IncludedDocuments extends Enumerable.Implicits {

  case object BirthCertificate extends WithName("birthCertificate") with IncludedDocuments
  case object Passport extends WithName("passport") with IncludedDocuments
  case object TravelDocuments extends WithName("travelDocuments") with IncludedDocuments
  case object AdoptionCertificate extends WithName("adoptionCertificate") with IncludedDocuments

  private val allValues: Seq[IncludedDocuments] = Seq(
    BirthCertificate,
    Passport,
    TravelDocuments,
    AdoptionCertificate
  )

  def values(relationshipToChild: ApplicantRelationshipToChild): Seq[IncludedDocuments] = {
    relationshipToChild match {
      case AdoptedChild =>
        allValues

      case _ =>
        allValues.filterNot(_ == AdoptionCertificate)
    }
  }

  def checkboxItems(relationshipToChild: ApplicantRelationshipToChild)(implicit messages: Messages): Seq[CheckboxItem] =
    values(relationshipToChild).zipWithIndex.map {
      case (value, index) =>
        CheckboxItemViewModel(
          content = Text(messages(s"includedDocuments.${value.toString}")),
          fieldId = "value",
          index   = index,
          value   = value.toString
        )
    }

  implicit val enumerable: Enumerable[IncludedDocuments] =
    Enumerable(allValues.map(v => v.toString -> v): _*)
}
