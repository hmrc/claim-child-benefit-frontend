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

package viewmodels.checkAnswers.applicant

import models.{AdultName, DesignatoryDetails, UserAnswers}
import org.scalatest.{OptionValues, TryValues}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import pages.EmptyWaypoints
import pages.applicant.DesignatoryNamePage
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

import java.time.LocalDate

class DesignatoryNameSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".row" - {

    val originalName = AdultName(None, "original first", None, "original last")
    val newName = AdultName(None, "new first", None, "new last")
    val designatoryDetails = DesignatoryDetails(Some(originalName), None, None, None, LocalDate.now)

    "when designatory details exist" - {

      "and a new name has been provided" - {

        "must return a row using the new name" in {

          val answers =
            UserAnswers("id", designatoryDetails = Some(designatoryDetails))
              .set(DesignatoryNamePage, newName).success.value

          DesignatoryNameSummary.row(answers, EmptyWaypoints).value.value.content mustEqual Text("new first new last")
        }
      }

      "and a new name has not been provided" - {

        "must return a row using the original preferred name" in {

          val answers =
            UserAnswers("id", designatoryDetails = Some(designatoryDetails))

          DesignatoryNameSummary.row(answers, EmptyWaypoints).value.value.content mustEqual Text("original first original last")
        }
      }
    }

    "when designatory details do not exist" - {

      "must not return a row" in {

        val answers = UserAnswers("id").set(DesignatoryNamePage, newName).success.value

        DesignatoryNameSummary.row(answers, EmptyWaypoints) must not be defined
      }
    }
  }
}
