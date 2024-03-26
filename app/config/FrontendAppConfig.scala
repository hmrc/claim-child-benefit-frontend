/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import play.api.mvc.RequestHeader

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  val host: String    = configuration.get[String]("host")
  val appName: String = configuration.get[String]("appName")

  private val contactHost = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "claim-child-benefit-frontend"

  def feedbackUrl(implicit request: RequestHeader): String =
   s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${host + request.uri}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signedInUrl: String      = configuration.get[String]("urls.signedIn")
  val signOutUrl: String       = configuration.get[String]("urls.signOut")
  val registerUrl: String      = configuration.get[String]("urls.register")
  val origin: String           = configuration.get[String]("origin")
  val upliftMfaUrl: String     = configuration.get[String]("urls.upliftMfa")
  val upliftIvUrl: String      = configuration.get[String]("urls.upliftIv")
  val childBenefitTaxChargeRestartUrl: String = configuration.get[String]("urls.childBenefitTaxChargeRestart")
  val childBenefitTaxChargeStopUrl: String    = configuration.get[String]("urls.childBenefitTaxChargeStop")
  val pegaClaimChildBenefit: String           = configuration.get[String]("urls.pegaClaimChildBenefit")

  val homeOfficeImmigrationStatusUrl: String = configuration.get[Service]("microservice.services.home-office-immigration-status-proxy").baseUrl

  private val exitSurveyBaseUrl: String = configuration.get[String]("feedback-frontend.host")
  val exitSurveyUrl: String             = s"$exitSurveyBaseUrl/feedback/claim-child-benefit"

  val internalAuthToken: String = configuration.get[String]("internal-auth.token")

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map(
    "en" -> Lang("en"),
    "cy" -> Lang("cy")
  )

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val brmsCacheTtl: Int   = configuration.get[Int]("mongodb.brmsTimeToLiveInSeconds")
}
