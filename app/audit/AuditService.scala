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

package audit

import com.google.inject.{Inject, Singleton}
import models.JourneyModel
import models.domain.Claim
import play.api.{Configuration, Logging}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.util.UUID
import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject() (connector: AuditConnector, configuration: Configuration)(implicit ec: ExecutionContext) extends Logging {

  private val downloadEventName                 = configuration.get[String]("auditing.downloadEventName")
  private val verifyBankDetailsEventName        = configuration.get[String]("auditing.verifyBankDetailsEventName")
  private val submitToCbsAuditEventName         = configuration.get[String]("auditing.submitToCbsEventName")
  private val checkBankAccountInsightsEventName = configuration.get[String]("auditing.checkBankAccountInsightsEventName")

  def auditDownload(model: JourneyModel)(implicit hc: HeaderCarrier): Unit = {
    val data = DownloadAuditEvent.from(model)
    connector.sendExplicitAudit(downloadEventName, data)
  }

  def auditVerifyBankDetails(auditEvent: VerifyBankDetailsAuditEvent)(implicit hc: HeaderCarrier): Unit =
    connector.sendExplicitAudit(verifyBankDetailsEventName, auditEvent)

  def auditSubmissionToCbs(model: JourneyModel, claim: Claim, correlationId: UUID)(implicit hc: HeaderCarrier): Unit = {
    val data = SubmitToCbsAuditEvent.from(model, claim, correlationId)
    connector.sendExplicitAudit(submitToCbsAuditEventName, data)
  }

  def auditCheckBankAccountInsights(auditEvent: CheckBankAccountInsightsAuditEvent)(implicit headerCarrier: HeaderCarrier): Unit =
    connector.sendExplicitAudit(checkBankAccountInsightsEventName, auditEvent)
}
