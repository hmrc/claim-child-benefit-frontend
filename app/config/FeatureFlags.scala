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

package config

import play.api.Configuration

import javax.inject.Inject

class FeatureFlags @Inject()(configuration: Configuration) {

  val verifyBankDetails: Boolean             = configuration.get[Boolean]("features.verify-bank-details")
  val auditDownload: Boolean                 = configuration.get[Boolean]("features.audit-download")
  val matchBirthRegistrationDetails: Boolean = configuration.get[Boolean]("features.match-birth-registration-details")
  val allowAuthenticatedSessions: Boolean    = configuration.get[Boolean]("features.allow-authenticated-sessions")
  val allowSubmissionToCbs: Boolean          = configuration.get[Boolean]("features.allow-submission-to-cbs")
  val showSignInPage: Boolean                = configuration.get[Boolean]("features.show-sign-in-page")
  val callBankAccountInsights: Boolean       = configuration.get[Boolean]("features.call-bank-account-insights")
  val submitOlderChildrenToCbs: Boolean      = configuration.get[Boolean]("features.submit-older-children-to-cbs")
}
