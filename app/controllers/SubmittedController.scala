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

package controllers

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import logging.Logging
import models.Income.BelowLowerThreshold
import models.IncomeOrdering._
import models.RelationshipStatus.{Cohabiting, Married}
import models.TaxChargePayer
import pages.partner.RelationshipStatusPage
import pages.payments.{ApplicantIncomePage, PartnerIncomePage, WantToBePaidPage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{SubmittedNoTaxChargeView, SubmittedWithTaxChargeView}

import javax.inject.Inject

class SubmittedController @Inject()(
                                     val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierAction,
                                     getData: DataRetrievalAction,
                                     requireData: DataRequiredAction,
                                     noTaxChargeView: SubmittedNoTaxChargeView,
                                     withTaxChargeView: SubmittedWithTaxChargeView
                                   ) extends FrontendBaseController with I18nSupport with Logging with AnswerExtractor {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      getAnswer(WantToBePaidPage) { wantToBePaid =>
        getAnswer(RelationshipStatusPage) {
          case Married | Cohabiting =>
            getAnswers(ApplicantIncomePage, PartnerIncomePage) {
              case (applicantIncome, partnerIncome) =>
                if (applicantIncome == BelowLowerThreshold && partnerIncome == BelowLowerThreshold) {
                  Ok(noTaxChargeView(hasPartner = true))
                } else {
                  val taxChargePayer = {
                    if      (applicantIncome < partnerIncome)  TaxChargePayer.Partner
                    else if (applicantIncome == partnerIncome) TaxChargePayer.ApplicantOrPartner
                    else                                       TaxChargePayer.Applicant
                  }

                  Ok(withTaxChargeView(wantToBePaid, hasPartner = true, taxChargePayer))
                }
            }

          case _ =>
            getAnswer(ApplicantIncomePage) {
              case BelowLowerThreshold =>
                Ok(noTaxChargeView(hasPartner = false))

              case _ =>
                Ok(withTaxChargeView(wantToBePaid, hasPartner = false, TaxChargePayer.Applicant))
            }
        }
      }
  }
}
