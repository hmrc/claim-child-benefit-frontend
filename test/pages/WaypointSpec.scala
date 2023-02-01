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

package pages

import models.{CheckMode, Index, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.applicant.{AddApplicantNationalityPage, AddApplicantPreviousFamilyNamePage, AddCountryApplicantReceivedBenefitsPage, AddCountryApplicantWorkedPage, CheckApplicantDetailsPage}
import pages.child.{AddChildPage, AddChildPreviousNamePage, CheckChildDetailsPage}
import pages.partner.{AddPartnerNationalityPage, CheckPartnerDetailsPage}
import pages.payments.CheckPaymentDetailsPage

class WaypointSpec extends AnyFreeSpec with Matchers with OptionValues {

  ".fromString" - {

    "must return Add Applicant Previous Family Name when given its Normal mode waypoint" in {

      Waypoint.fromString("add-other-name").value mustEqual AddApplicantPreviousFamilyNamePage().waypoint(NormalMode)
    }

    "must return Add Applicant Previous Family Name when given its check mode waypoint" in {

      Waypoint.fromString("change-other-name").value mustEqual AddApplicantPreviousFamilyNamePage().waypoint(CheckMode)
    }

    "must return Add Applicant Nationality when given its Normal mode waypoint" in {

      Waypoint.fromString("add-nationality").value mustEqual AddApplicantNationalityPage().waypoint(NormalMode)
    }

    "must return Add Applicant Nationality when given its check mode waypoint" in {

      Waypoint.fromString("change-nationality").value mustEqual AddApplicantNationalityPage().waypoint(CheckMode)
    }

    "must return Add Country Applicant Worked when given its Normal mode waypoint" in {

      Waypoint.fromString("add-country-you-worked").value mustEqual AddCountryApplicantWorkedPage().waypoint(NormalMode)
    }

    "must return Add Country Applicant Worked when given its check mode waypoint" in {

      Waypoint.fromString("change-country-you-worked").value mustEqual AddCountryApplicantWorkedPage().waypoint(CheckMode)
    }

    "must return Add Country Applicant Received Benefits when given its Normal mode waypoint" in {

      Waypoint.fromString("add-country-you-received-benefits").value mustEqual AddCountryApplicantReceivedBenefitsPage().waypoint(NormalMode)
    }

    "must return Add Country Applicant Received Benefits when given its check mode waypoint" in {

      Waypoint.fromString("change-country-you-received-benefits").value mustEqual AddCountryApplicantReceivedBenefitsPage().waypoint(CheckMode)
    }

    "must return Add Partner Nationality when given its Normal mode waypoint" in {

      Waypoint.fromString("add-partners-nationality").value mustEqual AddPartnerNationalityPage().waypoint(NormalMode)
    }

    "must return Add Partner Nationality when given its check mode waypoint" in {

      Waypoint.fromString("change-partners-nationality").value mustEqual AddPartnerNationalityPage().waypoint(CheckMode)
    }

    "must return Add Child when given its normal mode waypoints" in {

      Waypoint.fromString("add-child").value mustEqual AddChildPage().waypoint(NormalMode)
    }

    "must return Add Child when given its check mode waypoints" in {

      Waypoint.fromString("change-child").value mustEqual AddChildPage().waypoint(CheckMode)
    }

    "must return Check Child Details when given its waypoint" in {

      Waypoint.fromString("check-child-1").value mustEqual CheckChildDetailsPage(Index(0)).waypoint
      Waypoint.fromString("check-child-2").value mustEqual CheckChildDetailsPage(Index(1)).waypoint
    }

    "must return Add Child Previous Name when given its normal mode waypoint" in {

      Waypoint.fromString("add-child-name-1").value mustEqual AddChildPreviousNamePage(Index(0)).waypoint(NormalMode)
      Waypoint.fromString("add-child-name-2").value mustEqual AddChildPreviousNamePage(Index(1)).waypoint(NormalMode)
    }

    "must return Add Child Previous Name when given its check mode waypoint" in {

      Waypoint.fromString("change-child-name-1").value mustEqual AddChildPreviousNamePage(Index(0)).waypoint(CheckMode)
      Waypoint.fromString("change-child-name-2").value mustEqual AddChildPreviousNamePage(Index(1)).waypoint(CheckMode)
    }

    "must return Check Applicant Details when given its waypoint" in {

      Waypoint.fromString("check-your-details").value mustEqual CheckApplicantDetailsPage.waypoint
    }

    "must return Check Partner Details when given its waypoint" in {

      Waypoint.fromString("check-partners-details").value mustEqual CheckPartnerDetailsPage.waypoint
    }

    "must return Check Payment Details when given its waypoint" in {

      Waypoint.fromString("check-payment-details").value mustEqual CheckPaymentDetailsPage.waypoint
    }
  }
}
