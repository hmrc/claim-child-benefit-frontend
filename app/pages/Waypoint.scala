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

package pages

import models.{CheckMode, Mode, NormalMode}
import pages.applicant.{AddApplicantNationalityPage, AddApplicantPreviousFamilyNamePage, AddCountryApplicantReceivedBenefitsPage, AddCountryApplicantWorkedPage, CheckApplicantDetailsPage}
import pages.child.{AddChildPage, AddChildPreviousNamePage, CheckChildDetailsPage}
import pages.partner.{AddCountryPartnerReceivedBenefitsPage, AddCountryPartnerWorkedPage, AddPartnerNationalityPage, CheckPartnerDetailsPage}
import pages.payments.CheckPaymentDetailsPage

case class Waypoint (
                      page: WaypointPage,
                      mode: Mode,
                      urlFragment: String
                    )

object Waypoint {

  private val fragments: Map[String, Waypoint] =
    Map(
      AddApplicantPreviousFamilyNamePage().normalModeUrlFragment      -> AddApplicantPreviousFamilyNamePage().waypoint(NormalMode),
      AddApplicantPreviousFamilyNamePage().checkModeUrlFragment       -> AddApplicantPreviousFamilyNamePage().waypoint(CheckMode),
      AddApplicantNationalityPage().normalModeUrlFragment             -> AddApplicantNationalityPage().waypoint(NormalMode),
      AddApplicantNationalityPage().checkModeUrlFragment              -> AddApplicantNationalityPage().waypoint(CheckMode),
      AddCountryApplicantWorkedPage().normalModeUrlFragment           -> AddCountryApplicantWorkedPage().waypoint(NormalMode),
      AddCountryApplicantWorkedPage().checkModeUrlFragment            -> AddCountryApplicantWorkedPage().waypoint(CheckMode),
      AddCountryApplicantReceivedBenefitsPage().normalModeUrlFragment -> AddCountryApplicantReceivedBenefitsPage().waypoint(NormalMode),
      AddCountryApplicantReceivedBenefitsPage().checkModeUrlFragment  -> AddCountryApplicantReceivedBenefitsPage().waypoint(CheckMode),
      AddPartnerNationalityPage().normalModeUrlFragment               -> AddPartnerNationalityPage().waypoint(NormalMode),
      AddPartnerNationalityPage().checkModeUrlFragment                -> AddPartnerNationalityPage().waypoint(CheckMode),
      AddCountryPartnerWorkedPage().normalModeUrlFragment             -> AddCountryPartnerWorkedPage().waypoint(NormalMode),
      AddCountryPartnerWorkedPage().checkModeUrlFragment              -> AddCountryPartnerWorkedPage().waypoint(CheckMode),
      AddCountryPartnerReceivedBenefitsPage().normalModeUrlFragment   -> AddCountryPartnerReceivedBenefitsPage().waypoint(NormalMode),
      AddCountryPartnerReceivedBenefitsPage().checkModeUrlFragment    -> AddCountryPartnerReceivedBenefitsPage().waypoint(CheckMode),
      AddChildPage().normalModeUrlFragment                            -> AddChildPage().waypoint(NormalMode),
      AddChildPage().checkModeUrlFragment                             -> AddChildPage().waypoint(CheckMode),
      CheckApplicantDetailsPage.urlFragment                           -> CheckApplicantDetailsPage.waypoint,
      CheckPartnerDetailsPage.urlFragment                             -> CheckPartnerDetailsPage.waypoint,
      CheckPaymentDetailsPage.urlFragment                             -> CheckPaymentDetailsPage.waypoint
    )

  def fromString(s: String): Option[Waypoint] =
    fragments.get(s)
      .orElse(CheckChildDetailsPage.waypointFromString(s))
      .orElse(AddChildPreviousNamePage.waypointFromString(s))
}
