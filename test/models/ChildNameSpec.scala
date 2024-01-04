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

package models

import base.SpecBase

class ChildNameSpec extends SpecBase {

  ".displayName" - {

    "must be correct when there is a middle name" in {

      val childName = ChildName("first", Some("middle"), "last")

      childName.fullName mustEqual "first middle last"
    }

    "must be correct when there is no middle name" in {

      val childName = ChildName("first", None, "last")

      childName.fullName mustEqual "first last"
    }
  }
}
