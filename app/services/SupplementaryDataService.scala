package services

import com.dmanchester.playfop.sapi.PlayFop
import connectors.ClaimChildBenefitConnector
import models.{Done, JourneyModel, SupplementaryMetadata}
import org.apache.fop.apps.FOUserAgent
import org.apache.xmlgraphics.util.MimeConstants
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.xml.xml.PrintTemplate

import java.time.Clock
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

trait SupplementaryDataService {

  def submit(nino: String, model: JourneyModel, correlationId: UUID)(implicit request: RequestHeader): Future[Done]
}

@Singleton
class SupplementaryDataServiceImpl @Inject() (
                                               clock: Clock,
                                               connector: ClaimChildBenefitConnector,
                                               template: PrintTemplate,
                                               fop: PlayFop,
                                               override val messagesApi: MessagesApi
                                             ) extends SupplementaryDataService with I18nSupport {

  private val userAgentBlock: FOUserAgent => Unit = { foUserAgent: FOUserAgent =>
    foUserAgent.setAccessibility(true)
    foUserAgent.setPdfUAEnabled(true)
    foUserAgent.setAuthor("HMRC forms service")
    foUserAgent.setProducer("HMRC forms services")
    foUserAgent.setCreator("HMRC forms services")
    foUserAgent.setSubject("Claim Child Benefit by post form")
    foUserAgent.setTitle("Claim Child Benefit by post form")
  }

  override def submit(nino: String, model: JourneyModel, correlationId: UUID)(implicit request: RequestHeader): Future[Done] = {

    val metadata = SupplementaryMetadata(
      nino = model.applicant.nationalInsuranceNumber.get,
      submissionDate = clock.instant(),
      correlationId = correlationId.toString
    )

    val pdf = fop.processTwirlXml(template(model), MimeConstants.MIME_PDF, foUserAgentBlock = userAgentBlock)

    connector.submitSupplementaryData(pdf, metadata)(HeaderCarrierConverter.fromRequestAndSession(request, request.session))
  }
}

@Singleton
class NoOpSupplementaryDataService @Inject() () extends SupplementaryDataService {

  override def submit(nino: String, model: JourneyModel, correlationId: UUID)(implicit request: RequestHeader): Future[Done] =
    Future.successful(Done)
}
