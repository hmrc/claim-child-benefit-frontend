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

import models.{Country, DesignatoryDetails, InternationalAddress, NPSAddress, UkAddress, UserAnswers}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.EmptyWaypoints
import pages.applicant.{CorrespondenceInternationalAddressPage, CorrespondenceUkAddressPage}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent

import java.time.LocalDate

class CorrespondenceAddressSummarySpec extends AnyFreeSpec with Matchers with OptionValues with TryValues {

  private implicit val messages: Messages = stubMessages()

  ".row" - {

    val originalAddress = NPSAddress("original line 1", None, None, None, None, None, None)
    val newUkAddress = UkAddress("line1", None, "town", None, "postcode")
    val newInternationalAddress = InternationalAddress("line1", None, "town", None, None, Country("ES", "Spain"))
    val designatoryDetails = DesignatoryDetails(None, None, None, Some(originalAddress), LocalDate.now)

    "when designatory details exist" - {

      "and a new UK address has been provided" - {

        "must return a row using the new address" in {

          val answers =
            UserAnswers("id", designatoryDetails = Some(designatoryDetails))
              .set(CorrespondenceUkAddressPage, newUkAddress)
              .success
              .value

          CorrespondenceAddressSummary.row(answers, EmptyWaypoints).value.value.content `mustEqual` HtmlContent(
            newUkAddress.lines.mkString("<br/>")
          )
        }
      }

      "and a new international address has been provided" - {

        "must return a row using the new address" in {

          val answers =
            UserAnswers("id", designatoryDetails = Some(designatoryDetails))
              .set(CorrespondenceInternationalAddressPage, newInternationalAddress)
              .success
              .value

          CorrespondenceAddressSummary.row(answers, EmptyWaypoints).value.value.content `mustEqual` HtmlContent(
            newInternationalAddress.lines.mkString("<br/>")
          )
        }
      }

      "and no new address has been provided" - {

        "must return a row using the original address" in {

          val answers = UserAnswers("id", designatoryDetails = Some(designatoryDetails))

          CorrespondenceAddressSummary.row(answers, EmptyWaypoints).value.value.content `mustEqual` HtmlContent(
            originalAddress.lines.mkString("<br/>")
          )
        }
      }
    }

    "when no designatory details exist" - {

      "must not return a row" in {

        val answers = UserAnswers("id").set(CorrespondenceUkAddressPage, newUkAddress).success.value

        CorrespondenceAddressSummary.row(answers, EmptyWaypoints) `must` not `be` defined
      }
    }
  }
}
