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

package journey

import org.scalatest.freespec.AnyFreeSpec
import pages.{AdditionalInformationPage, IncludeAdditionalInformationPage, TaskListPage}

class ChangingAdditionalInformationSectionJourneySpec extends AnyFreeSpec with JourneyHelpers {

  "when the user originally gave information" - {

    "changing to say they haven't must remove the information" in {

      startingFrom(IncludeAdditionalInformationPage)
        .run(
          setUserAnswerTo(IncludeAdditionalInformationPage, true),
          setUserAnswerTo(AdditionalInformationPage, "foo"),
          submitAnswer(IncludeAdditionalInformationPage, false),
          pageMustBe(TaskListPage),
          answersMustNotContain(AdditionalInformationPage)
        )
    }
  }
}
