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

package pages

import models.{CheckMode, NormalMode}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.applicant.AddApplicantPreviousFamilyNamePage

class WaypointSpec extends AnyFreeSpec with Matchers with OptionValues {

  ".fromString" - {

    "must return CheckYourAnswers when given its waypoint" in {

      Waypoint.fromString("check-answers").value mustEqual CheckYourAnswersPage.waypoint
    }

    "must return Add Applicant Previous Family Name when given its Normal mode waypoint" in {

      Waypoint.fromString("add-other-name").value mustEqual AddApplicantPreviousFamilyNamePage.waypoint(NormalMode)
    }

    "must return Add Applicant Previous Family Name when given its check mode waypoint" in {

      Waypoint.fromString("change-other-name").value mustEqual AddApplicantPreviousFamilyNamePage.waypoint(CheckMode)
    }
  }
}
