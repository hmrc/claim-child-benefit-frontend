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
import models.UserAnswers
import pages.{NonEmptyWaypoints, Page, QuestionPage, Waypoints}
import play.api.libs.json.JsPath
import play.api.mvc.Call
import queries.Settable

import scala.util.{Success, Try}

case object AlwaysLivedInUkPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "alwaysLivedInUk"

  override def route(waypoints: Waypoints): Call =
    routes.AlwaysLivedInUkController.onPageLoad(waypoints)

  override def nextPageNormalMode(waypoints: Waypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true  => ApplicantCurrentUkAddressPage
      case false => ApplicantUsuallyLivesInUkPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, answers: UserAnswers): Page =
    answers.get(this).map {
      case true =>
        answers.get(ApplicantCurrentUkAddressPage)
          .map { _ =>
            answers.get(ApplicantLivedAtCurrentAddressOneYearPage).map {
              case true =>
                waypoints.next.page

              case false =>
                answers.get(ApplicantPreviousUkAddressPage)
                  .map(_ => waypoints.next.page)
                  .getOrElse(ApplicantPreviousUkAddressPage)
            }.getOrElse {
              answers.get(ApplicantNinoPage)
                .map(_ => waypoints.next.page)
                .getOrElse(ApplicantLivedAtCurrentAddressOneYearPage)
            }
          }.getOrElse(ApplicantCurrentUkAddressPage)

      case false =>
        answers.get(ApplicantUsuallyLivesInUkPage)
          .map(_ => waypoints.next.page)
          .getOrElse(ApplicantUsuallyLivesInUkPage)
    }.orRecover

  override def cleanup(value: Option[Boolean], userAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case true =>

        val maybeRemoveLivedHereAYear: Option[Settable[_]] =
          userAnswers
            .get(ApplicantPreviousInternationalAddressPage)
            .map(_ => ApplicantLivedAtCurrentAddressOneYearPage)

        userAnswers
          .remove(ApplicantUsuallyLivesInUkPage)
          .flatMap(_.remove(ApplicantUsualCountryOfResidencePage))
          .flatMap(_.remove(ApplicantArrivedInUkPage))
          .flatMap(_.remove(ApplicantCurrentAddressInUkPage))
          .flatMap(_.remove(ApplicantCurrentInternationalAddressPage))
          .flatMap(_.remove(ApplicantPreviousAddressInUkPage))
          .flatMap(_.remove(ApplicantPreviousInternationalAddressPage))
          .flatMap(x => removePages(x, maybeRemoveLivedHereAYear.toSeq))

      case false =>
        val currentAddressPage = userAnswers.get(ApplicantCurrentUkAddressPage).map(_ => ApplicantCurrentAddressInUkPage)
        val previousAddressPage = userAnswers.get(ApplicantPreviousUkAddressPage).map(_ => ApplicantPreviousAddressInUkPage)

        val pagesToSet = Seq(currentAddressPage, previousAddressPage).flatten

        setPagesToTrue(userAnswers, pagesToSet)
    }.getOrElse(super.cleanup(value, userAnswers))

  private def removePages(answers: UserAnswers, pages: Seq[Settable[_]]): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(answers))((acc, page) => acc.flatMap(_.remove(page)))

  private def setPagesToTrue(answers: UserAnswers, pages: Seq[Settable[Boolean]]): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(answers))((acc, page) => acc.flatMap(_.set(page, true)))
}
