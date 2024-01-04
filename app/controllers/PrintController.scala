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

package controllers

import audit.AuditService
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import logging.Logging
import models.journey.JourneyModel
import models.requests.DataRequest
import org.apache.fop.apps.FOUserAgent
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesImpl}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{FopService, JourneyModelService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{PrintDocumentsRequiredView, PrintNoDocumentsRequiredView}
import views.xml.xml.download.PrintTemplate

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PrintController @Inject()(
                                 val controllerComponents: MessagesControllerComponents,
                                 identify: IdentifierAction,
                                 getData: DataRetrievalAction,
                                 requireData: DataRequiredAction,
                                 auditService: AuditService,
                                 fop: FopService,
                                 template: PrintTemplate,
                                 noDocumentsView: PrintNoDocumentsRequiredView,
                                 documentsView: PrintDocumentsRequiredView,
                                 journeyModelService: JourneyModelService
                               )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport with Logging {

  private val userAgentBlock: FOUserAgent => Unit = { foUserAgent: FOUserAgent =>
    foUserAgent.setAccessibility(true)
    foUserAgent.setPdfUAEnabled(true)
    foUserAgent.setAuthor("HMRC forms service")
    foUserAgent.setProducer("HMRC forms services")
    foUserAgent.setCreator("HMRC forms services")
    foUserAgent.setSubject("Claim Child Benefit by post form")
    foUserAgent.setTitle("Claim Child Benefit by post form")
  }

  private def withJourneyModel(request: DataRequest[AnyContent])(f: JourneyModel => Future[Result]): Future[Result] = {
    val (maybeErrors, maybeModel) = journeyModelService.build(request.userAnswers)(request2Messages(request)).pad

    val errors = maybeErrors.map { errors =>
      val message = errors.toChain.toList.map(_.path).mkString(", ")
      s" at: $message"
    }.getOrElse("")

    maybeModel.map { model =>
      f(model)
    }.getOrElse {
      logger.warn(s"Journey model creation failed.")
      logger.debug(s"Journey model creation failed $errors")
      Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  }


  def onDownload: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      withJourneyModel(request) {
        journeyModel =>
          val englishMessages: Messages = MessagesImpl(Lang("en"), implicitly)

          auditService.auditDownload(journeyModel)

          fop.render(template.render(journeyModel, englishMessages).body, userAgentBlock).map { pdf =>
            Ok(pdf).as("application/octet-stream")
              .withHeaders(CONTENT_DISPOSITION -> "attachment; filename=claim-child-benefit-by-post.pdf")
          }
      }
  }

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      withJourneyModel(request) {
        journeyModel =>
          if (journeyModel.children.exists(_.requiredDocuments.nonEmpty)) {
            Future.successful(Ok(documentsView(journeyModel)))
          } else {
            val wantToBePaid = journeyModel.paymentPreference match {
              case _: models.journey.PaymentPreference.DoNotPay => false
              case _ => true
            }

            Future.successful(Ok(noDocumentsView(wantToBePaid)))
          }
      }
  }
}
