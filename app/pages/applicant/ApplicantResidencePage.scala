/*
 * Copyright 2023 HM Revenue & Customs
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

package pages.applicant

import controllers.applicant.routes
import models.{ApplicantResidence, UserAnswers}
import pages.{Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.{AllCountriesApplicantReceivedBenefits, AllCountriesApplicantWorked}

import scala.util.Try

case object ApplicantResidencePage extends QuestionPage[ApplicantResidence] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "applicantResidence"

  override def route(waypoints: Waypoints): Call =
    routes.ApplicantResidenceController.onPageLoad(waypoints)

  override protected def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case ApplicantResidence.AlwaysUk     => ApplicantCurrentUkAddressPage
      case ApplicantResidence.UkAndAbroad  => ApplicantUsuallyLivesInUkPage
      case ApplicantResidence.AlwaysAbroad => ApplicantUsualCountryOfResidencePage
    }.orRecover

  override def cleanup(value: Option[ApplicantResidence], previousAnswers: UserAnswers, currentAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      newResidence =>
        if (previousAnswers.get(ApplicantResidencePage).contains(newResidence)) {
          super.cleanup(value, previousAnswers, currentAnswers)
        } else {
          currentAnswers
            .remove(ApplicantUsuallyLivesInUkPage)
            .flatMap(_.remove(ApplicantUsualCountryOfResidencePage))
            .flatMap(_.remove(ApplicantCurrentAddressInUkPage))
            .flatMap(_.remove(ApplicantArrivedInUkPage))
            .flatMap(_.remove(ApplicantCurrentUkAddressPage))
            .flatMap(_.remove(ApplicantCurrentInternationalAddressPage))
            .flatMap(_.remove(ApplicantLivedAtCurrentAddressOneYearPage))
            .flatMap(_.remove(ApplicantPreviousAddressInUkPage))
            .flatMap(_.remove(ApplicantPreviousUkAddressPage))
            .flatMap(_.remove(ApplicantPreviousInternationalAddressPage))
            .flatMap(_.remove(ApplicantEmploymentStatusPage))
            .flatMap(_.remove(ApplicantWorkedAbroadPage))
            .flatMap(_.remove(AllCountriesApplicantWorked))
            .flatMap(_.remove(AddCountryApplicantWorkedPage()))
            .flatMap(_.remove(ApplicantReceivedBenefitsAbroadPage))
            .flatMap(_.remove(AllCountriesApplicantReceivedBenefits))
            .flatMap(_.remove(AddCountryApplicantReceivedBenefitsPage()))
        }
    }.getOrElse(super.cleanup(value, previousAnswers, currentAnswers))
}
