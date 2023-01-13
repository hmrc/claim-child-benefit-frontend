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

package pages

import controllers.routes
import models.TaskListSectionChange.AddressDetailsRemoved
import models.{TaskListSectionChange, UserAnswers}
import pages.applicant._
import pages.partner.PartnerIsHmfOrCivilServantPage
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
      case true  => CheckRelationshipDetailsPage
      case false => ApplicantIsHmfOrCivilServantPage
    }.orRecover

  override protected def nextPageCheckMode(waypoints: NonEmptyWaypoints, originalAnswers: UserAnswers, updatedAnswers: UserAnswers): Page =
    updatedAnswers.get(this).map {
      case true =>
        originalAnswers.get(ApplicantCurrentAddressInUkPage)
          .map(_ => AlwaysLivedInUkChangesTaskListPage)
          .getOrElse(waypoints.next.page)

      case false =>
        originalAnswers.get(ApplicantIsHmfOrCivilServantPage)
          .map(_ => waypoints.next.page)
          .getOrElse(ApplicantIsHmfOrCivilServantPage)
    }.orRecover

  override def cleanup(value: Option[Boolean], previousAnswers: UserAnswers, currentAnswers: UserAnswers): Try[UserAnswers] =
    value.map {
      case true =>
        val maybeSectionChange: Option[TaskListSectionChange] =
          previousAnswers.get(ApplicantCurrentAddressInUkPage).map(_ => AddressDetailsRemoved)

        val pagesToAlwaysRemove = Seq(
          ApplicantIsHmfOrCivilServantPage,
          PartnerIsHmfOrCivilServantPage
        )

        val pagesToRemove = if(maybeSectionChange.isEmpty) pagesToAlwaysRemove else pagesToAlwaysRemove ++ addressPages

        currentAnswers
          .set(AlwaysLivedInUkChangesTaskListPage, maybeSectionChange.toSet)
          .flatMap(x => removePages(x, pagesToRemove))

      case false =>
        val pagesToSet: Seq[Settable[Boolean]] = Seq(
          previousAnswers.get(ApplicantCurrentUkAddressPage).map(_ => ApplicantCurrentAddressInUkPage),
          previousAnswers.get(ApplicantPreviousUkAddressPage).map(_ => ApplicantPreviousAddressInUkPage)
        ).flatten

        currentAnswers
          .set(AlwaysLivedInUkChangesTaskListPage, Set.empty[TaskListSectionChange])
          .flatMap(x => setPagesToTrue(x, pagesToSet))
    }.getOrElse(super.cleanup(value, previousAnswers, currentAnswers))


  private val addressPages: Seq[Settable[_]] = Seq(
    ApplicantCurrentAddressInUkPage,
    ApplicantCurrentUkAddressPage,
    ApplicantCurrentInternationalAddressPage,
    ApplicantLivedAtCurrentAddressOneYearPage,
    ApplicantPreviousAddressInUkPage,
    ApplicantPreviousUkAddressPage,
    ApplicantPreviousInternationalAddressPage
  )

  private def removePages(answers: UserAnswers, pages: Seq[Settable[_]]): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(answers))((acc, page) => acc.flatMap(_.remove(page)))

  private def setPagesToTrue(answers: UserAnswers, pages: Seq[Settable[Boolean]]): Try[UserAnswers] =
    pages.foldLeft[Try[UserAnswers]](Success(answers))((acc, page) => acc.flatMap(_.set(page, true)))
}
