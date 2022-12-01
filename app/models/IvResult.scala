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

import play.api.libs.json.{Reads, __}

sealed trait IvResult

object IvResult {

  case object Success extends IvResult
  case object Incomplete extends IvResult
  case object FailedMatching extends IvResult
  case object FailedIdentityVerification extends IvResult
  case object InsufficientEvidence extends IvResult
  case object LockedOut extends IvResult
  case object UserAborted extends IvResult
  case object Timeout extends IvResult
  case object TechnicalIssue extends IvResult
  case object PreconditionFailed extends IvResult

  implicit lazy val reads: Reads[IvResult] =
    (__ \ "progress" \ "result").read[String].flatMap {
      case "Success"                    => Reads.pure(Success)
      case "Incomplete"                 => Reads.pure(Incomplete)
      case "FailedMatching"             => Reads.pure(FailedMatching)
      case "FailedIdentityVerification" => Reads.pure(FailedIdentityVerification)
      case "InsufficientEvidence"       => Reads.pure(InsufficientEvidence)
      case "LockedOut"                  => Reads.pure(LockedOut)
      case "UserAborted"                => Reads.pure(UserAborted)
      case "Timeout"                    => Reads.pure(Timeout)
      case "TechnicalIssue"             => Reads.pure(TechnicalIssue)
      case "PreconditionFailed"         => Reads.pure(PreconditionFailed)
    }
}
