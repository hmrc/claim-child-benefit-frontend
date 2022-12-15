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

package services

import controllers.applicant.{routes => applicantRoutes}
import models.UserAnswers
import models.tasklist.Section
import models.tasklist.SectionStatus._
import pages.EmptyWaypoints
import pages.applicant.ApplicantNinoKnownPage
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class TaskListService {

  def sections(answers: UserAnswers)(implicit hc: HeaderCarrier): Future[Seq[Section]] =
    Future.successful(Seq(
      Section("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), NotStarted),
      Section("taskList.maritalDetails", None, CannotStart),
      Section("taskList.childDetails", None, CannotStart),
      Section("taskList.paymentDetails", None, CannotStart),
      Section("taskList.furtherDetails", None, CannotStart)
    ))

  private[services] def applicantSection(answers: UserAnswers): Future[Section] = {
    val status = if(answers.isDefined(ApplicantNinoKnownPage)) InProgress else NotStarted
    Future.successful(
      Section("taskList.yourDetails", Some(applicantRoutes.ApplicantNinoKnownController.onPageLoad(EmptyWaypoints)), status)
    )
  }
}
