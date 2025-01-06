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

package services

import connectors.ClaimChildBenefitConnector
import models.journey.JourneyModel
import models.{AdditionalArchiveDetails, Done, SupplementaryMetadata}
import org.apache.fop.apps.FOUserAgent
import play.api.i18n._
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.xml.xml.archive.ArchiveTemplate

import java.time.Clock
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import utils.FutureOps._

trait SupplementaryDataService {

  def submit(nino: String, model: JourneyModel, correlationId: UUID, additionalDetails: AdditionalArchiveDetails)(implicit request: RequestHeader): Future[Done]
}

@Singleton
class SupplementaryDataServiceImpl @Inject() (
                                               clock: Clock,
                                               connector: ClaimChildBenefitConnector,
                                               template: ArchiveTemplate,
                                               fop: FopService,
                                               override val messagesApi: MessagesApi
                                             )(implicit ec: ExecutionContext) extends SupplementaryDataService with I18nSupport {

  private val userAgentBlock: FOUserAgent => Unit = { foUserAgent =>
    foUserAgent.setAccessibility(true)
    foUserAgent.setPdfUAEnabled(true)
    foUserAgent.setAuthor("HMRC forms service")
    foUserAgent.setProducer("HMRC forms services")
    foUserAgent.setCreator("HMRC forms services")
    foUserAgent.setSubject("Claim Child Benefit submission")
    foUserAgent.setTitle("Claim Child Benefit submission")
  }

  override def submit(nino: String, model: JourneyModel, correlationId: UUID, additionalDetails: AdditionalArchiveDetails)
                     (implicit request: RequestHeader): Future[Done] = {

    val metadata = SupplementaryMetadata(
      nino = model.applicant.nationalInsuranceNumber.get,
      submissionDate = clock.instant(),
      correlationId = correlationId.toString
    )

    val englishMessages: Messages = MessagesImpl(Lang("en"), messagesApi)

    fop.render(template.render(model, additionalDetails, englishMessages).body, userAgentBlock).flatMap { pdf =>
      connector.submitSupplementaryData(pdf, metadata)(HeaderCarrierConverter.fromRequestAndSession(request, request.session))
    }.logFailure("SupplementaryDataService failure.")
  }
}

@Singleton
class NoOpSupplementaryDataService @Inject() () extends SupplementaryDataService {

  override def submit(nino: String, model: JourneyModel, correlationId: UUID, additionalDetails: AdditionalArchiveDetails)
                     (implicit request: RequestHeader): Future[Done] =
    Future.successful(Done)
}
